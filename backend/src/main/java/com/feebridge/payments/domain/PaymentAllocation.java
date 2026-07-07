package com.feebridge.payments.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** One line of a payment order: how much of the payment goes to a given student's invoice. */
@Entity
@Table(name = "payment_allocations")
public class PaymentAllocation extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "amount_kobo", nullable = false)
    private long amountKobo;

    @Column(nullable = false)
    private boolean applied = false;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public long getAmountKobo() {
        return amountKobo;
    }

    public void setAmountKobo(long amountKobo) {
        this.amountKobo = amountKobo;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }
}
