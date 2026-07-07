package com.feebridge.people.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Maps a student to a guardian. A delegated payer (sponsor/NGO) is flagged here. */
@Entity
@Table(name = "student_guardians")
public class StudentGuardian extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "guardian_id", nullable = false)
    private Long guardianId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuardianRelationship relationship = GuardianRelationship.GUARDIAN;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    @Column(name = "is_payer", nullable = false)
    private boolean payer = true;

    @Column(name = "is_delegated_payer", nullable = false)
    private boolean delegatedPayer = false;

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

    public Long getGuardianId() {
        return guardianId;
    }

    public void setGuardianId(Long guardianId) {
        this.guardianId = guardianId;
    }

    public GuardianRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(GuardianRelationship relationship) {
        this.relationship = relationship;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isPayer() {
        return payer;
    }

    public void setPayer(boolean payer) {
        this.payer = payer;
    }

    public boolean isDelegatedPayer() {
        return delegatedPayer;
    }

    public void setDelegatedPayer(boolean delegatedPayer) {
        this.delegatedPayer = delegatedPayer;
    }
}
