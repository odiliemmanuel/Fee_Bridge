package com.feebridge.billing;

import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.domain.LedgerEntry;
import com.feebridge.billing.domain.LedgerEntryType;
import com.feebridge.billing.domain.StudentCredit;
import com.feebridge.billing.dto.BillingDtos.AssessmentResultDto;
import com.feebridge.billing.dto.BillingDtos.InvoiceDto;
import com.feebridge.billing.dto.BillingDtos.LedgerEntryDto;
import com.feebridge.billing.dto.BillingDtos.ReconcileResultDto;
import com.feebridge.billing.dto.BillingDtos.StudentStatementDto;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.billing.repo.LedgerEntryRepository;
import com.feebridge.billing.repo.StudentCreditRepository;
import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.money.Money;
import com.feebridge.common.web.PageResponse;
import com.feebridge.fee.domain.FeeStructure;
import com.feebridge.fee.repo.FeeStructureRepository;
import com.feebridge.people.domain.Enrollment;
import com.feebridge.people.domain.Student;
import com.feebridge.people.domain.StudentStatus;
import com.feebridge.people.repo.EnrollmentRepository;
import com.feebridge.people.repo.StudentRepository;
import com.feebridge.scholarship.ScholarshipService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The billing engine: term assessment (invoice generation with scholarships + credit carry-in)
 * and the payment application waterfall (full/partial/overpayment, surplus carried to next term).
 */
@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final StudentCreditRepository creditRepository;
    private final FeeStructureRepository feeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final ScholarshipService scholarshipService;

    public BillingService(InvoiceRepository invoiceRepository, LedgerEntryRepository ledgerRepository,
                          StudentCreditRepository creditRepository, FeeStructureRepository feeRepository,
                          EnrollmentRepository enrollmentRepository, StudentRepository studentRepository,
                          SchoolClassRepository classRepository, ScholarshipService scholarshipService) {
        this.invoiceRepository = invoiceRepository;
        this.ledgerRepository = ledgerRepository;
        this.creditRepository = creditRepository;
        this.feeRepository = feeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.scholarshipService = scholarshipService;
    }

    // ---- Assessment -------------------------------------------------------

    /** Generates term invoices for every active enrolment that has a fee and no invoice yet. */
    @Transactional
    public AssessmentResultDto generateInvoices(Long schoolId, Long sessionId, Long termId, Long userId) {
        List<Enrollment> enrollments = enrollmentRepository.findBySchoolIdAndSessionId(schoolId, sessionId);
        int created = 0;
        int skipped = 0;
        Money totalBilled = Money.ZERO;

        for (Enrollment e : enrollments) {
            Optional<Student> student = studentRepository.findById(e.getStudentId());
            if (student.isEmpty() || student.get().getStatus() != StudentStatus.ACTIVE) {
                skipped++;
                continue;
            }
            if (invoiceRepository.findByStudentIdAndTermId(e.getStudentId(), termId).isPresent()) {
                skipped++;
                continue;
            }
            Optional<FeeStructure> fee = feeRepository
                    .findByClassIdAndTermIdAndResidencyType(e.getClassId(), termId, e.getResidencyType());
            if (fee.isEmpty()) {
                skipped++;
                continue;
            }

            Money gross = fee.get().getAmount();
            Money scholarship = scholarshipService.discountFor(e.getStudentId(), sessionId, termId, gross);
            Money payableBeforeCredit = gross.minus(scholarship);

            StudentCredit wallet = getOrCreateWallet(schoolId, e.getStudentId());
            Money creditApplied = Money.ofKobo(wallet.getBalanceKobo()).min(payableBeforeCredit);
            Money net = payableBeforeCredit.minus(creditApplied);

            Invoice inv = new Invoice();
            inv.setSchoolId(schoolId);
            inv.setStudentId(e.getStudentId());
            inv.setSessionId(sessionId);
            inv.setTermId(termId);
            inv.setClassId(e.getClassId());
            inv.setResidencyType(e.getResidencyType());
            inv.setGrossAmountKobo(gross.kobo());
            inv.setScholarshipAmountKobo(scholarship.kobo());
            inv.setCreditAppliedKobo(creditApplied.kobo());
            inv.setNetAmountKobo(net.kobo());
            inv.setAmountPaidKobo(0);
            inv.setBalanceKobo(net.kobo());
            inv.setStatus(net.isZero() ? InvoiceStatus.PAID : InvoiceStatus.PENDING);
            inv = invoiceRepository.save(inv);

            ledger(inv, LedgerEntryType.DEBIT_FEE, gross.kobo(), "Term fee", userId);
            if (scholarship.isPositive()) {
                ledger(inv, LedgerEntryType.CREDIT_SCHOLARSHIP, -scholarship.kobo(), "Scholarship discount", userId);
            }
            if (creditApplied.isPositive()) {
                ledger(inv, LedgerEntryType.CREDIT_CARRY_IN, -creditApplied.kobo(), "Credit carried from prior term", userId);
                wallet.setBalanceKobo(wallet.getBalanceKobo() - creditApplied.kobo());
            }
            created++;
            totalBilled = totalBilled.plus(net);
        }
        return new AssessmentResultDto(created, skipped, totalBilled.toNaira());
    }

    // ---- Payment application (shared by online + offline) -----------------

    /**
     * Applies a payment to one invoice: caps at the balance, records the ledger movement, and
     * routes any surplus to the student's credit wallet, cascading it onto later open invoices.
     */
    @Transactional
    public InvoiceDto applyPayment(Long schoolId, Long invoiceId, Money amount, boolean online,
                                   String reference, Long userId) {
        if (!amount.isPositive()) {
            throw new BadRequestException("Payment amount must be positive");
        }
        Invoice invoice = invoiceRepository.findByIdAndSchoolId(invoiceId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Invoice", invoiceId));

        Money balance = invoice.getBalance();
        Money applied = amount.min(balance);
        Money surplus = amount.minus(applied);

        if (applied.isPositive()) {
            invoice.setAmountPaidKobo(invoice.getAmountPaidKobo() + applied.kobo());
            invoice.setBalanceKobo(invoice.getBalanceKobo() - applied.kobo());
            LedgerEntryType type = online ? LedgerEntryType.CREDIT_PAYMENT : LedgerEntryType.CREDIT_OFFLINE_PAYMENT;
            ledger(invoice, type, -applied.kobo(), online ? "Online payment" : "Offline payment", userId);
        }
        recomputeStatus(invoice, surplus.isPositive());

        if (surplus.isPositive()) {
            StudentCredit wallet = getOrCreateWallet(schoolId, invoice.getStudentId());
            wallet.setBalanceKobo(wallet.getBalanceKobo() + surplus.kobo());
            applyWalletToOpenInvoices(schoolId, invoice.getStudentId(), invoice.getId(), userId);
        }
        return toInvoiceDto(invoice);
    }

    /** Applies available wallet credit to the student's open invoices in term order. */
    @Transactional
    public void applyWalletToOpenInvoices(Long schoolId, Long studentId, Long excludeInvoiceId, Long userId) {
        StudentCredit wallet = getOrCreateWallet(schoolId, studentId);
        if (wallet.getBalanceKobo() <= 0) {
            return;
        }
        for (Invoice inv : invoiceRepository.findByStudentIdOrderByTermIdAsc(studentId)) {
            if (wallet.getBalanceKobo() <= 0) {
                break;
            }
            if (inv.getId().equals(excludeInvoiceId) || inv.getBalanceKobo() <= 0) {
                continue;
            }
            long apply = Math.min(wallet.getBalanceKobo(), inv.getBalanceKobo());
            inv.setCreditAppliedKobo(inv.getCreditAppliedKobo() + apply);
            inv.setNetAmountKobo(inv.getNetAmountKobo() - apply);
            inv.setBalanceKobo(inv.getBalanceKobo() - apply);
            ledger(inv, LedgerEntryType.CREDIT_CARRY_IN, -apply, "Credit applied from wallet", userId);
            wallet.setBalanceKobo(wallet.getBalanceKobo() - apply);
            recomputeStatus(inv, false);
        }
    }

    // ---- Queries ----------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<InvoiceDto> listInvoices(Long schoolId, Long sessionId, Long termId, Long classId,
                                                 InvoiceStatus status, Pageable pageable) {
        var page = invoiceRepository.filter(schoolId, sessionId, termId, classId, status, pageable);
        Map<Long, Student> students = studentsById(page.getContent());
        Map<Long, String> classes = classNames(schoolId);
        return PageResponse.from(page, inv -> toInvoiceDto(inv, students, classes));
    }

    @Transactional(readOnly = true)
    public InvoiceDto getInvoice(Long schoolId, Long id) {
        Invoice inv = invoiceRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> NotFoundException.of("Invoice", id));
        return toInvoiceDto(inv);
    }

    @Transactional(readOnly = true)
    public StudentStatementDto studentStatement(Long schoolId, Long studentId) {
        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        List<InvoiceDto> invoices = invoiceRepository.findByStudentIdOrderByTermIdAsc(studentId).stream()
                .map(this::toInvoiceDto).toList();
        List<LedgerEntryDto> ledger = ledgerRepository.findByStudentIdOrderByIdAsc(studentId).stream()
                .map(this::toLedgerDto).toList();
        long credit = creditRepository.findByStudentId(studentId).map(StudentCredit::getBalanceKobo).orElse(0L);
        long outstanding = invoiceRepository.findByStudentIdOrderByTermIdAsc(studentId).stream()
                .mapToLong(Invoice::getBalanceKobo).sum();
        return new StudentStatementDto(studentId, student.fullName(), Money.ofKobo(credit).toNaira(),
                Money.ofKobo(outstanding).toNaira(), invoices, ledger);
    }

    /** "Verify with balance": the ledger sum must equal the sum of invoice balances. */
    @Transactional(readOnly = true)
    public ReconcileResultDto reconcileStudent(Long schoolId, Long studentId) {
        studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        long ledgerBalance = ledgerRepository.balanceForStudent(studentId);
        long invoiceSum = invoiceRepository.findByStudentIdOrderByTermIdAsc(studentId).stream()
                .mapToLong(Invoice::getBalanceKobo).sum();
        return new ReconcileResultDto(studentId, Money.ofKobo(ledgerBalance).toNaira(),
                Money.ofKobo(invoiceSum).toNaira(), ledgerBalance == invoiceSum);
    }

    // ---- helpers ----------------------------------------------------------

    private StudentCredit getOrCreateWallet(Long schoolId, Long studentId) {
        return creditRepository.findByStudentId(studentId).orElseGet(() -> {
            StudentCredit c = new StudentCredit();
            c.setSchoolId(schoolId);
            c.setStudentId(studentId);
            c.setBalanceKobo(0);
            return creditRepository.save(c);
        });
    }

    private void ledger(Invoice inv, LedgerEntryType type, long signedKobo, String description, Long userId) {
        ledgerRepository.save(LedgerEntry.of(inv.getSchoolId(), inv.getStudentId(), inv.getSessionId(),
                inv.getTermId(), inv.getId(), type, signedKobo, null, description, userId));
    }

    private void recomputeStatus(Invoice inv, boolean hadSurplus) {
        if (inv.getBalanceKobo() <= 0) {
            inv.setStatus(hadSurplus ? InvoiceStatus.OVERPAID : InvoiceStatus.PAID);
        } else {
            inv.setStatus(inv.getAmountPaidKobo() > 0 ? InvoiceStatus.PARTIAL : InvoiceStatus.PENDING);
        }
    }

    private Map<Long, Student> studentsById(List<Invoice> invoices) {
        List<Long> ids = invoices.stream().map(Invoice::getStudentId).distinct().toList();
        return studentRepository.findAllById(ids).stream().collect(Collectors.toMap(Student::getId, Function.identity()));
    }

    private Map<Long, String> classNames(Long schoolId) {
        return classRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId).stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
    }

    private InvoiceDto toInvoiceDto(Invoice inv) {
        Student s = studentRepository.findById(inv.getStudentId()).orElse(null);
        String className = inv.getClassId() == null ? null :
                classRepository.findById(inv.getClassId()).map(SchoolClass::getName).orElse(null);
        return toInvoiceDto(inv, s, className);
    }

    private InvoiceDto toInvoiceDto(Invoice inv, Map<Long, Student> students, Map<Long, String> classes) {
        return toInvoiceDto(inv, students.get(inv.getStudentId()), classes.get(inv.getClassId()));
    }

    private InvoiceDto toInvoiceDto(Invoice inv, Student s, String className) {
        return new InvoiceDto(inv.getId(), inv.getStudentId(),
                s == null ? null : s.fullName(), s == null ? null : s.getAdmissionNo(),
                inv.getClassId(), className, inv.getSessionId(), inv.getTermId(), inv.getResidencyType(),
                Money.ofKobo(inv.getGrossAmountKobo()).toNaira(),
                Money.ofKobo(inv.getScholarshipAmountKobo()).toNaira(),
                Money.ofKobo(inv.getCreditAppliedKobo()).toNaira(),
                Money.ofKobo(inv.getNetAmountKobo()).toNaira(),
                Money.ofKobo(inv.getAmountPaidKobo()).toNaira(),
                Money.ofKobo(inv.getBalanceKobo()).toNaira(),
                inv.getStatus());
    }

    private LedgerEntryDto toLedgerDto(LedgerEntry l) {
        return new LedgerEntryDto(l.getId(), l.getInvoiceId(), l.getTermId(), l.getEntryType(),
                Money.ofKobo(l.getAmountKobo()).toNaira(), l.getReference(), l.getDescription(), l.getCreatedAt());
    }

    // exposed for the payments module to build allocations against outstanding invoices
    public Optional<Invoice> findInvoice(Long schoolId, Long invoiceId) {
        return invoiceRepository.findByIdAndSchoolId(invoiceId, schoolId);
    }

    public List<Invoice> outstandingInvoicesForStudent(Long studentId) {
        List<Invoice> all = invoiceRepository.findByStudentIdOrderByTermIdAsc(studentId);
        List<Invoice> open = new ArrayList<>();
        for (Invoice i : all) {
            if (i.getBalanceKobo() > 0) {
                open.add(i);
            }
        }
        return open;
    }
}
