package com.feebridge.school.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** A tenant. Every other school-owned entity is scoped to a School. */
@Entity
@Table(name = "schools")
public class School extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    private String email;
    private String phone;
    private String address;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private String currency = "NGN";

    @Column(name = "has_day", nullable = false)
    private boolean hasDay = true;

    @Column(name = "has_boarding", nullable = false)
    private boolean hasBoarding = false;

    @Column(name = "nomba_account_id")
    private String nombaAccountId;

    @Column(name = "nomba_account_ref")
    private String nombaAccountRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SchoolStatus status = SchoolStatus.ACTIVE;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isHasDay() {
        return hasDay;
    }

    public void setHasDay(boolean hasDay) {
        this.hasDay = hasDay;
    }

    public boolean isHasBoarding() {
        return hasBoarding;
    }

    public void setHasBoarding(boolean hasBoarding) {
        this.hasBoarding = hasBoarding;
    }

    public String getNombaAccountId() {
        return nombaAccountId;
    }

    public void setNombaAccountId(String nombaAccountId) {
        this.nombaAccountId = nombaAccountId;
    }

    public String getNombaAccountRef() {
        return nombaAccountRef;
    }

    public void setNombaAccountRef(String nombaAccountRef) {
        this.nombaAccountRef = nombaAccountRef;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }
}
