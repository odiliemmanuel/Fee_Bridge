package com.feebridge.payments.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** A payment intent that may cover multiple students (allocations) in one transaction. */
@Entity
@Table(name = "payment_orders")
public class PaymentOrder extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String reference;

    @Column(name = "payer_guardian_id")
    private Long payerGuardianId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentChannel channel;

    @Column(name = "total_amount_kobo", nullable = false)
    private long totalAmountKobo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentOrderStatus status = PaymentOrderStatus.AWAITING_PAYMENT;

    private String note;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Long getPayerGuardianId() {
        return payerGuardianId;
    }

    public void setPayerGuardianId(Long payerGuardianId) {
        this.payerGuardianId = payerGuardianId;
    }

    public PaymentChannel getChannel() {
        return channel;
    }

    public void setChannel(PaymentChannel channel) {
        this.channel = channel;
    }

    public long getTotalAmountKobo() {
        return totalAmountKobo;
    }

    public void setTotalAmountKobo(long totalAmountKobo) {
        this.totalAmountKobo = totalAmountKobo;
    }

    public PaymentOrderStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentOrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
