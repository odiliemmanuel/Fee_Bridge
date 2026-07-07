package com.feebridge.fee.domain;

import com.feebridge.common.domain.BaseEntity;
import com.feebridge.common.domain.ResidencyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Immutable record of a fee amount change, kept for audit ("payment amount change per term"). */
@Entity
@Table(name = "fee_change_log")
public class FeeChangeLog extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "fee_structure_id")
    private Long feeStructureId;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "term_id", nullable = false)
    private Long termId;

    @Enumerated(EnumType.STRING)
    @Column(name = "residency_type", nullable = false)
    private ResidencyType residencyType;

    @Column(name = "old_amount_kobo")
    private Long oldAmountKobo;

    @Column(name = "new_amount_kobo", nullable = false)
    private long newAmountKobo;

    private String reason;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public Long getFeeStructureId() {
        return feeStructureId;
    }

    public void setFeeStructureId(Long feeStructureId) {
        this.feeStructureId = feeStructureId;
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

    public Long getOldAmountKobo() {
        return oldAmountKobo;
    }

    public void setOldAmountKobo(Long oldAmountKobo) {
        this.oldAmountKobo = oldAmountKobo;
    }

    public long getNewAmountKobo() {
        return newAmountKobo;
    }

    public void setNewAmountKobo(long newAmountKobo) {
        this.newAmountKobo = newAmountKobo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }
}
