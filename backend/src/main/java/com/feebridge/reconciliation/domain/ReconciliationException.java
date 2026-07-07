package com.feebridge.reconciliation.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** A discrepancy flagged during reconciliation that needs human review. */
@Entity
@Table(name = "reconciliation_exceptions")
public class ReconciliationException extends BaseEntity {

    public enum Type { UNMATCHED_CREDIT, AMOUNT_MISMATCH, LEDGER_IMBALANCE }

    @Column(name = "school_id")
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(name = "transaction_id")
    private Long transactionId;

    private String reference;

    @Column(name = "expected_amount_kobo")
    private Long expectedAmountKobo;

    @Column(name = "actual_amount_kobo")
    private Long actualAmountKobo;

    private String detail;

    @Column(nullable = false)
    private boolean resolved = false;

    public static ReconciliationException of(Long schoolId, Type type, Long transactionId, String reference,
                                             Long expected, Long actual, String detail) {
        ReconciliationException e = new ReconciliationException();
        e.schoolId = schoolId;
        e.type = type;
        e.transactionId = transactionId;
        e.reference = reference;
        e.expectedAmountKobo = expected;
        e.actualAmountKobo = actual;
        e.detail = detail;
        return e;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Type getType() {
        return type;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public String getReference() {
        return reference;
    }

    public Long getExpectedAmountKobo() {
        return expectedAmountKobo;
    }

    public Long getActualAmountKobo() {
        return actualAmountKobo;
    }

    public String getDetail() {
        return detail;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
