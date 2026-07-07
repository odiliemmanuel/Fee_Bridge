package com.feebridge.payments.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** A cash / bank-transfer / POS payment recorded manually by school staff. */
@Entity
@Table(name = "offline_payments")
public class OfflinePayment extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfflineMethod method;

    @Column(name = "amount_kobo", nullable = false)
    private long amountKobo;

    private String reference;

    private String note;

    @Column(name = "recorded_by_user_id")
    private Long recordedByUserId;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public OfflineMethod getMethod() {
        return method;
    }

    public void setMethod(OfflineMethod method) {
        this.method = method;
    }

    public long getAmountKobo() {
        return amountKobo;
    }

    public void setAmountKobo(long amountKobo) {
        this.amountKobo = amountKobo;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(Long recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }
}
