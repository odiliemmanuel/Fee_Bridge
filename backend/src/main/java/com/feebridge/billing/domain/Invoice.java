package com.feebridge.billing.domain;

import com.feebridge.common.domain.BaseEntity;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.common.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** What a student owes for one term, plus running paid/balance and status. */
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Enumerated(EnumType.STRING)
    @Column(name = "residency_type", nullable = false)
    private ResidencyType residencyType;

    @Column(name = "gross_amount_kobo", nullable = false)
    private long grossAmountKobo;

    @Column(name = "scholarship_amount_kobo", nullable = false)
    private long scholarshipAmountKobo;

    @Column(name = "credit_applied_kobo", nullable = false)
    private long creditAppliedKobo;

    @Column(name = "net_amount_kobo", nullable = false)
    private long netAmountKobo;

    @Column(name = "amount_paid_kobo", nullable = false)
    private long amountPaidKobo;

    @Column(name = "balance_kobo", nullable = false)
    private long balanceKobo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING;

    public Money getNet() {
        return Money.ofKobo(netAmountKobo);
    }

    public Money getBalance() {
        return Money.ofKobo(balanceKobo);
    }

    public Money getAmountPaid() {
        return Money.ofKobo(amountPaidKobo);
    }

    // getters / setters

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

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public ResidencyType getResidencyType() {
        return residencyType;
    }

    public void setResidencyType(ResidencyType residencyType) {
        this.residencyType = residencyType;
    }

    public long getGrossAmountKobo() {
        return grossAmountKobo;
    }

    public void setGrossAmountKobo(long grossAmountKobo) {
        this.grossAmountKobo = grossAmountKobo;
    }

    public long getScholarshipAmountKobo() {
        return scholarshipAmountKobo;
    }

    public void setScholarshipAmountKobo(long scholarshipAmountKobo) {
        this.scholarshipAmountKobo = scholarshipAmountKobo;
    }

    public long getCreditAppliedKobo() {
        return creditAppliedKobo;
    }

    public void setCreditAppliedKobo(long creditAppliedKobo) {
        this.creditAppliedKobo = creditAppliedKobo;
    }

    public long getNetAmountKobo() {
        return netAmountKobo;
    }

    public void setNetAmountKobo(long netAmountKobo) {
        this.netAmountKobo = netAmountKobo;
    }

    public long getAmountPaidKobo() {
        return amountPaidKobo;
    }

    public void setAmountPaidKobo(long amountPaidKobo) {
        this.amountPaidKobo = amountPaidKobo;
    }

    public long getBalanceKobo() {
        return balanceKobo;
    }

    public void setBalanceKobo(long balanceKobo) {
        this.balanceKobo = balanceKobo;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }
}
