package com.feebridge.billing.domain;

import com.feebridge.common.domain.BaseEntity;
import com.feebridge.common.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** A student's carried-over credit wallet (from overpayment), applied to future terms. */
@Entity
@Table(name = "student_credits")
public class StudentCredit extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "balance_kobo", nullable = false)
    private long balanceKobo;

    public Money getBalance() {
        return Money.ofKobo(balanceKobo);
    }

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

    public long getBalanceKobo() {
        return balanceKobo;
    }

    public void setBalanceKobo(long balanceKobo) {
        this.balanceKobo = balanceKobo;
    }
}
