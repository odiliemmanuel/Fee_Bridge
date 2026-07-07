package com.feebridge.billing.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Immutable ledger movement for a student. See {@link LedgerEntryType} for sign convention. */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "term_id")
    private Long termId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(name = "amount_kobo", nullable = false)
    private long amountKobo;

    private String reference;

    private String description;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public static LedgerEntry of(Long schoolId, Long studentId, Long sessionId, Long termId, Long invoiceId,
                                 LedgerEntryType type, long signedAmountKobo, String reference, String description,
                                 Long userId) {
        LedgerEntry e = new LedgerEntry();
        e.schoolId = schoolId;
        e.studentId = studentId;
        e.sessionId = sessionId;
        e.termId = termId;
        e.invoiceId = invoiceId;
        e.entryType = type;
        e.amountKobo = signedAmountKobo;
        e.reference = reference;
        e.description = description;
        e.createdByUserId = userId;
        return e;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getTermId() {
        return termId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public long getAmountKobo() {
        return amountKobo;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }
}
