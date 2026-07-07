package com.feebridge.fee.domain;

import com.feebridge.common.domain.BaseEntity;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.common.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** The fee amount for a (class, session, term, residency) combination. */
@Entity
@Table(name = "fee_structures")
public class FeeStructure extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "residency_type", nullable = false)
    private ResidencyType residencyType;

    @Column(name = "amount_kobo", nullable = false)
    private long amountKobo;

    private String description;

    public Money getAmount() {
        return Money.ofKobo(amountKobo);
    }

    public void setAmount(Money amount) {
        this.amountKobo = amount.kobo();
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
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

    public ResidencyType getResidencyType() {
        return residencyType;
    }

    public void setResidencyType(ResidencyType residencyType) {
        this.residencyType = residencyType;
    }

    public long getAmountKobo() {
        return amountKobo;
    }

    public void setAmountKobo(long amountKobo) {
        this.amountKobo = amountKobo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
