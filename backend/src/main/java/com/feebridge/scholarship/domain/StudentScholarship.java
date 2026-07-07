package com.feebridge.scholarship.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** A scholarship awarded to a student, snapshotted and optionally scoped to a session/term. */
@Entity
@Table(name = "student_scholarships")
public class StudentScholarship extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "scholarship_id")
    private Long scholarshipId;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "term_id")
    private Long termId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScholarshipType type;

    private BigDecimal percentage;

    @Column(name = "amount_kobo")
    private Long amountKobo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScholarshipStatus status = ScholarshipStatus.ACTIVE;

    @Column(name = "awarded_by_user_id")
    private Long awardedByUserId;

    private String note;

    /** True when this award applies to the given session/term (null scope = applies to all). */
    public boolean appliesTo(Long sessionId, Long termId) {
        boolean sessionOk = this.sessionId == null || this.sessionId.equals(sessionId);
        boolean termOk = this.termId == null || this.termId.equals(termId);
        return sessionOk && termOk;
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

    public Long getScholarshipId() {
        return scholarshipId;
    }

    public void setScholarshipId(Long scholarshipId) {
        this.scholarshipId = scholarshipId;
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

    public ScholarshipType getType() {
        return type;
    }

    public void setType(ScholarshipType type) {
        this.type = type;
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public void setPercentage(BigDecimal percentage) {
        this.percentage = percentage;
    }

    public Long getAmountKobo() {
        return amountKobo;
    }

    public void setAmountKobo(Long amountKobo) {
        this.amountKobo = amountKobo;
    }

    public ScholarshipStatus getStatus() {
        return status;
    }

    public void setStatus(ScholarshipStatus status) {
        this.status = status;
    }

    public Long getAwardedByUserId() {
        return awardedByUserId;
    }

    public void setAwardedByUserId(Long awardedByUserId) {
        this.awardedByUserId = awardedByUserId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
