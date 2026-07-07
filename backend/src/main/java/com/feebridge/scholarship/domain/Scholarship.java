package com.feebridge.scholarship.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** A reusable scholarship template (percentage discount or fixed amount off). */
@Entity
@Table(name = "scholarships")
public class Scholarship extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScholarshipType type;

    private BigDecimal percentage;

    @Column(name = "amount_kobo")
    private Long amountKobo;

    private String sponsor;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
