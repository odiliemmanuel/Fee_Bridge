package com.feebridge.payments;

import com.feebridge.billing.BillingService;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.dto.BillingDtos.InvoiceDto;
import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.money.Money;
import com.feebridge.common.web.PageResponse;
import com.feebridge.nomba.NombaGateway;
import com.feebridge.nomba.NombaProperties;
import com.feebridge.notification.NotificationService;
import com.feebridge.payments.domain.OfflineMethod;
import com.feebridge.payments.domain.OfflinePayment;
import com.feebridge.payments.domain.PaymentAllocation;
import com.feebridge.payments.domain.PaymentChannel;
import com.feebridge.payments.domain.PaymentOrder;
import com.feebridge.payments.domain.PaymentOrderStatus;
import com.feebridge.payments.domain.PaymentTransaction;
import com.feebridge.payments.domain.TransactionStatus;
import com.feebridge.payments.domain.VirtualAccount;
import com.feebridge.payments.domain.VirtualAccountStatus;
import com.feebridge.payments.dto.PaymentDtos.AllocationDto;
import com.feebridge.payments.dto.PaymentDtos.AllocationInput;
import com.feebridge.payments.dto.PaymentDtos.CreateOrderRequest;
import com.feebridge.payments.dto.PaymentDtos.OrderDto;
import com.feebridge.payments.dto.PaymentDtos.RecordOfflineRequest;
import com.feebridge.payments.dto.PaymentDtos.TransactionDto;
import com.feebridge.payments.dto.PaymentDtos.VirtualAccountDto;
import com.feebridge.payments.repo.OfflinePaymentRepository;
import com.feebridge.payments.repo.PaymentAllocationRepository;
import com.feebridge.payments.repo.PaymentOrderRepository;
import com.feebridge.payments.repo.PaymentTransactionRepository;
import com.feebridge.payments.repo.VirtualAccountRepository;
import com.feebridge.people.domain.Student;
import com.feebridge.people.repo.GuardianRepository;
import com.feebridge.people.repo.StudentRepository;
import com.feebridge.school.repo.SchoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String PROVIDER = "NOMBA";

    private final PaymentOrderRepository orderRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final OfflinePaymentRepository offlinePaymentRepository;
    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final SchoolRepository schoolRepository;
    private final BillingService billingService;
    private final NombaGateway nombaGateway;
    private final NombaProperties nombaProperties;
    private final NotificationService notificationService;

    public PaymentService(PaymentOrderRepository orderRepository, PaymentAllocationRepository allocationRepository,
                          VirtualAccountRepository virtualAccountRepository,
                          PaymentTransactionRepository transactionRepository,
                          OfflinePaymentRepository offlinePaymentRepository, StudentRepository studentRepository,
                          GuardianRepository guardianRepository, SchoolRepository schoolRepository,
                          BillingService billingService, NombaGateway nombaGateway,
                          NombaProperties nombaProperties, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.allocationRepository = allocationRepository;
        this.virtualAccountRepository = virtualAccountRepository;
        this.transactionRepository = transactionRepository;
        this.offlinePaymentRepository = offlinePaymentRepository;
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.schoolRepository = schoolRepository;
        this.billingService = billingService;
        this.nombaGateway = nombaGateway;
        this.nombaProperties = nombaProperties;
        this.notificationService = notificationService;
    }

    // ---- Order creation (multi-child) + Nomba virtual account -------------

    @Transactional
    public OrderDto createOrder(Long schoolId, Long userId, CreateOrderRequest req) {
        if (req.payerGuardianId() != null) {
            guardianRepository.findByIdAndSchoolId(req.payerGuardianId(), schoolId)
                    .orElseThrow(() -> NotFoundException.of("Guardian", req.payerGuardianId()));
        }

        List<PaymentAllocation> allocations = new ArrayList<>();
        Money total = Money.ZERO;
        for (AllocationInput item : req.allocations()) {
            Student student = studentRepository.findByIdAndSchoolId(item.studentId(), schoolId)
                    .orElseThrow(() -> NotFoundException.of("Student", item.studentId()));
            Long invoiceId = resolveInvoiceId(schoolId, student.getId(), item.invoiceId());
            Money amount = Money.ofNaira(item.amountNaira());
            PaymentAllocation alloc = new PaymentAllocation();
            alloc.setSchoolId(schoolId);
            alloc.setStudentId(student.getId());
            alloc.setInvoiceId(invoiceId);
            alloc.setAmountKobo(amount.kobo());
            allocations.add(alloc);
            total = total.plus(amount);
        }
        if (!total.isPositive()) {
            throw new BadRequestException("Order total must be positive");
        }

        PaymentOrder order = new PaymentOrder();
        order.setSchoolId(schoolId);
        order.setReference(newReference());
        order.setPayerGuardianId(req.payerGuardianId());
        order.setChannel(PaymentChannel.ONLINE_NOMBA);
        order.setTotalAmountKobo(total.kobo());
        order.setStatus(PaymentOrderStatus.AWAITING_PAYMENT);
        order.setNote(req.note());
        order.setCreatedByUserId(userId);
        order = orderRepository.save(order);

        for (PaymentAllocation alloc : allocations) {
            alloc.setOrderId(order.getId());
            allocationRepository.save(alloc);
        }

        VirtualAccount va = mintVirtualAccount(schoolId, order, total);
        return toOrderDto(order, va, allocations);
    }

    private VirtualAccount mintVirtualAccount(Long schoolId, PaymentOrder order, Money total) {
        String accountName = schoolRepository.findById(schoolId)
                .map(s -> "FeeBridge/" + s.getName()).orElse("FeeBridge Payment");
        if (accountName.length() > 120) {
            accountName = accountName.substring(0, 120);
        }
        Instant expiry = Instant.now().plus(nombaProperties.virtualAccountTtlMinutes(), ChronoUnit.MINUTES);
        NombaGateway.VirtualAccountResult result =
                nombaGateway.createVirtualAccount(order.getReference(), accountName, total, expiry);

        VirtualAccount va = new VirtualAccount();
        va.setSchoolId(schoolId);
        va.setOrderId(order.getId());
        va.setAccountRef(order.getReference());
        va.setAccountNumber(result.accountNumber());
        va.setAccountName(result.accountName());
        va.setBankName(result.bankName());
        va.setExpectedAmountKobo(total.kobo());
        va.setExpiryAt(result.expiryAt());
        va.setStatus(VirtualAccountStatus.ACTIVE);
        return virtualAccountRepository.save(va);
    }

    // ---- Webhook settlement ----------------------------------------------

    /** Idempotently records and settles a Nomba transaction against its payment order. */
    @Transactional
    public void processSettlement(String providerReference, String accountRef, Money amount, Instant paidAt,
                                  String rawPayload, boolean success) {
        if (transactionRepository.existsByProviderAndProviderReference(PROVIDER, providerReference)) {
            log.info("Duplicate Nomba webhook ignored: {}", providerReference);
            return;
        }
        PaymentTransaction txn = new PaymentTransaction();
        txn.setProvider(PROVIDER);
        txn.setProviderReference(providerReference);
        txn.setAccountRef(accountRef);
        txn.setAmountKobo(amount.kobo());
        txn.setStatus(success ? TransactionStatus.SUCCESS : TransactionStatus.FAILED);
        txn.setPaidAt(paidAt);
        txn.setRawPayload(rawPayload);

        VirtualAccount va = accountRef == null ? null : virtualAccountRepository.findByAccountRef(accountRef).orElse(null);
        if (va == null) {
            txn.setMatched(false);
            transactionRepository.save(txn);
            log.warn("Unmatched Nomba transaction {} for accountRef {}", providerReference, accountRef);
            return;
        }
        PaymentOrder order = orderRepository.findById(va.getOrderId()).orElseThrow();
        txn.setSchoolId(order.getSchoolId());
        txn.setOrderId(order.getId());
        if (!success) {
            txn.setMatched(false);
            transactionRepository.save(txn);
            return;
        }
        txn.setMatched(true);
        transactionRepository.save(txn);

        settleOrder(order, amount);
        va.setStatus(VirtualAccountStatus.CLOSED);
    }

    private void settleOrder(PaymentOrder order, Money amount) {
        long remaining = amount.kobo();
        List<PaymentAllocation> allocations = allocationRepository.findByOrderIdOrderByIdAsc(order.getId());
        for (PaymentAllocation alloc : allocations) {
            if (remaining <= 0) {
                break;
            }
            if (alloc.isApplied() || alloc.getInvoiceId() == null) {
                continue;
            }
            long applyAmt = Math.min(remaining, alloc.getAmountKobo());
            billingService.applyPayment(order.getSchoolId(), alloc.getInvoiceId(), Money.ofKobo(applyAmt),
                    true, order.getReference(), null);
            notificationService.paymentReceived(order.getSchoolId(), alloc.getStudentId(),
                    Money.ofKobo(applyAmt), order.getReference(), false);
            if (applyAmt == alloc.getAmountKobo()) {
                alloc.setApplied(true);
            }
            remaining -= applyAmt;
        }
        // Any amount beyond the order total becomes surplus credit on the first student.
        if (remaining > 0 && !allocations.isEmpty() && allocations.get(0).getInvoiceId() != null) {
            billingService.applyPayment(order.getSchoolId(), allocations.get(0).getInvoiceId(),
                    Money.ofKobo(remaining), true, order.getReference(), null);
        }
        boolean allApplied = allocations.stream().allMatch(PaymentAllocation::isApplied);
        order.setStatus(allApplied ? PaymentOrderStatus.PAID : PaymentOrderStatus.PARTIALLY_SETTLED);
    }

    // ---- Offline payments -------------------------------------------------

    @Transactional
    public InvoiceDto recordOffline(Long schoolId, Long userId, RecordOfflineRequest req) {
        Student student = studentRepository.findByIdAndSchoolId(req.studentId(), schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", req.studentId()));
        Long invoiceId = resolveInvoiceId(schoolId, student.getId(), req.invoiceId());
        Money amount = Money.ofNaira(req.amountNaira());

        InvoiceDto invoice = billingService.applyPayment(schoolId, invoiceId, amount, false,
                req.reference() != null ? req.reference() : "OFFLINE", userId);

        OfflinePayment op = new OfflinePayment();
        op.setSchoolId(schoolId);
        op.setStudentId(student.getId());
        op.setInvoiceId(invoiceId);
        op.setMethod(req.method() != null ? req.method() : OfflineMethod.CASH);
        op.setAmountKobo(amount.kobo());
        op.setReference(req.reference());
        op.setNote(req.note());
        op.setRecordedByUserId(userId);
        offlinePaymentRepository.save(op);

        notificationService.paymentReceived(schoolId, student.getId(), amount,
                req.reference() != null ? req.reference() : "OFFLINE", true);
        return invoice;
    }

    // ---- Queries ----------------------------------------------------------

    @Transactional(readOnly = true)
    public OrderDto getOrder(Long schoolId, Long id) {
        PaymentOrder order = orderRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> NotFoundException.of("Order", id));
        VirtualAccount va = virtualAccountRepository.findByOrderId(order.getId()).orElse(null);
        return toOrderDto(order, va, allocationRepository.findByOrderIdOrderByIdAsc(order.getId()));
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderDto> listOrders(Long schoolId, Pageable pageable) {
        return PageResponse.from(orderRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId, pageable),
                o -> toOrderDto(o, virtualAccountRepository.findByOrderId(o.getId()).orElse(null),
                        allocationRepository.findByOrderIdOrderByIdAsc(o.getId())));
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionDto> listTransactions(Long schoolId, Pageable pageable) {
        return PageResponse.from(transactionRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId, pageable),
                this::toTransactionDto);
    }

    @Transactional(readOnly = true)
    public java.util.List<OrderDto> ordersForGuardian(Long guardianId) {
        return orderRepository.findByPayerGuardianIdOrderByCreatedAtDesc(guardianId).stream()
                .map(o -> toOrderDto(o, virtualAccountRepository.findByOrderId(o.getId()).orElse(null),
                        allocationRepository.findByOrderIdOrderByIdAsc(o.getId())))
                .toList();
    }

    // ---- helpers ----------------------------------------------------------

    private Long resolveInvoiceId(Long schoolId, Long studentId, Long requestedInvoiceId) {
        if (requestedInvoiceId != null) {
            Invoice inv = billingService.findInvoice(schoolId, requestedInvoiceId)
                    .orElseThrow(() -> NotFoundException.of("Invoice", requestedInvoiceId));
            if (!inv.getStudentId().equals(studentId)) {
                throw new BadRequestException("Invoice " + requestedInvoiceId + " does not belong to student " + studentId);
            }
            return inv.getId();
        }
        List<Invoice> open = billingService.outstandingInvoicesForStudent(studentId);
        if (open.isEmpty()) {
            throw new BadRequestException("Student " + studentId + " has no outstanding invoice to pay");
        }
        return open.get(0).getId();
    }

    private String newReference() {
        String ref;
        do {
            ref = "FB-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        } while (orderRepository.findByReference(ref).isPresent());
        return ref;
    }

    private OrderDto toOrderDto(PaymentOrder order, VirtualAccount va, List<PaymentAllocation> allocations) {
        VirtualAccountDto vaDto = va == null ? null : new VirtualAccountDto(va.getAccountNumber(), va.getAccountName(),
                va.getBankName(), va.getExpiryAt(), Money.ofKobo(va.getExpectedAmountKobo()).toNaira());
        List<AllocationDto> allocDtos = allocations.stream().map(a -> new AllocationDto(a.getId(), a.getStudentId(),
                studentRepository.findById(a.getStudentId()).map(Student::fullName).orElse(null),
                a.getInvoiceId(), Money.ofKobo(a.getAmountKobo()).toNaira(), a.isApplied())).toList();
        return new OrderDto(order.getId(), order.getReference(), order.getChannel(), order.getStatus(),
                Money.ofKobo(order.getTotalAmountKobo()).toNaira(), order.getPayerGuardianId(), order.getNote(),
                order.getCreatedAt(), vaDto, allocDtos);
    }

    private TransactionDto toTransactionDto(PaymentTransaction t) {
        return new TransactionDto(t.getId(), t.getOrderId(), t.getProviderReference(), t.getAccountRef(),
                Money.ofKobo(t.getAmountKobo()).toNaira(), t.getStatus(), t.isMatched(), t.getPaidAt(), t.getCreatedAt());
    }
}
