package com.feebridge.reconciliation;

import com.feebridge.billing.BillingService;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.dto.BillingDtos.ReconcileResultDto;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.common.money.Money;
import com.feebridge.payments.domain.PaymentTransaction;
import com.feebridge.payments.domain.TransactionStatus;
import com.feebridge.payments.repo.PaymentTransactionRepository;
import com.feebridge.reconciliation.domain.ReconciliationException;
import com.feebridge.reconciliation.domain.ReconciliationRun;
import com.feebridge.reconciliation.dto.ReconciliationDtos.ExceptionDto;
import com.feebridge.reconciliation.dto.ReconciliationDtos.RunResultDto;
import com.feebridge.reconciliation.repo.ReconciliationExceptionRepository;
import com.feebridge.reconciliation.repo.ReconciliationRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Reconciliation: flags Nomba credits that could not be matched to an order, and verifies each
 * student's ledger equals their invoice balances ("upon successful payment verify with balance").
 */
@Service
public class ReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationService.class);

    private final PaymentTransactionRepository transactionRepository;
    private final ReconciliationExceptionRepository exceptionRepository;
    private final ReconciliationRunRepository runRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingService billingService;

    public ReconciliationService(PaymentTransactionRepository transactionRepository,
                                 ReconciliationExceptionRepository exceptionRepository,
                                 ReconciliationRunRepository runRepository, InvoiceRepository invoiceRepository,
                                 BillingService billingService) {
        this.transactionRepository = transactionRepository;
        this.exceptionRepository = exceptionRepository;
        this.runRepository = runRepository;
        this.invoiceRepository = invoiceRepository;
        this.billingService = billingService;
    }

    /** Scans for successful-but-unmatched transactions and records an exception for each. */
    @Transactional
    public RunResultDto scanUnmatched() {
        int checked = 0;
        int created = 0;
        for (PaymentTransaction txn : transactionRepository.findByMatchedFalseOrderByCreatedAtDesc()) {
            if (txn.getStatus() != TransactionStatus.SUCCESS) {
                continue;
            }
            checked++;
            if (!exceptionRepository.existsByTransactionId(txn.getId())) {
                exceptionRepository.save(ReconciliationException.of(txn.getSchoolId(),
                        ReconciliationException.Type.UNMATCHED_CREDIT, txn.getId(), txn.getAccountRef(),
                        null, txn.getAmountKobo(), "Credit received with no matching payment order"));
                created++;
            }
        }
        ReconciliationRun run = new ReconciliationRun();
        run.setCheckedCount(checked);
        run.setExceptionCount(created);
        runRepository.save(run);
        return new RunResultDto(checked, created);
    }

    @Scheduled(cron = "0 */15 * * * *")
    @Transactional
    public void scheduledScan() {
        RunResultDto result = scanUnmatched();
        if (result.exceptionsCreated() > 0) {
            log.warn("Reconciliation found {} new unmatched credit(s)", result.exceptionsCreated());
        }
    }

    /** Verifies each student's ledger is consistent with their invoice balances; flags mismatches. */
    @Transactional
    public List<ReconcileResultDto> verifyLedger(Long schoolId) {
        List<Long> studentIds = invoiceRepository.filterList(schoolId, null, null, null, null).stream()
                .map(Invoice::getStudentId).distinct().toList();
        List<ReconcileResultDto> inconsistencies = new java.util.ArrayList<>();
        for (Long studentId : studentIds) {
            ReconcileResultDto result = billingService.reconcileStudent(schoolId, studentId);
            if (!result.consistent()) {
                inconsistencies.add(result);
                exceptionRepository.save(ReconciliationException.of(schoolId,
                        ReconciliationException.Type.LEDGER_IMBALANCE, null, "student:" + studentId,
                        Money.ofNaira(result.invoiceBalanceSumNaira()).kobo(),
                        Money.ofNaira(result.ledgerBalanceNaira()).kobo(),
                        "Ledger balance does not match invoice balances"));
            }
        }
        return inconsistencies;
    }

    @Transactional(readOnly = true)
    public List<ExceptionDto> listExceptions(Long schoolId) {
        return exceptionRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId).stream().map(this::toDto).toList();
    }

    @Transactional
    public void resolve(Long schoolId, Long id) {
        ReconciliationException e = exceptionRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> com.feebridge.common.exception.NotFoundException.of("Exception", id));
        e.setResolved(true);
    }

    private ExceptionDto toDto(ReconciliationException e) {
        return new ExceptionDto(e.getId(), e.getSchoolId(), e.getType(), e.getTransactionId(), e.getReference(),
                e.getExpectedAmountKobo() == null ? null : Money.ofKobo(e.getExpectedAmountKobo()).toNaira(),
                e.getActualAmountKobo() == null ? null : Money.ofKobo(e.getActualAmountKobo()).toNaira(),
                e.getDetail(), e.isResolved(), e.getCreatedAt());
    }
}
