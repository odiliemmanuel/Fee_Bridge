package com.feebridge.payments.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.time.Instant;

/** A Nomba dynamic virtual account minted for a specific payment order. */
@Entity
@Table(name = "virtual_accounts")
public class VirtualAccount extends BaseEntity {

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "account_ref", nullable = false)
    private String accountRef;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "expected_amount_kobo", nullable = false)
    private long expectedAmountKobo;

    @Column(name = "expiry_at")
    private Instant expiryAt;

    @Column(nullable = false)
    private String provider = "NOMBA";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VirtualAccountStatus status = VirtualAccountStatus.ACTIVE;

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

    public String getAccountRef() {
        return accountRef;
    }

    public void setAccountRef(String accountRef) {
        this.accountRef = accountRef;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public long getExpectedAmountKobo() {
        return expectedAmountKobo;
    }

    public void setExpectedAmountKobo(long expectedAmountKobo) {
        this.expectedAmountKobo = expectedAmountKobo;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(Instant expiryAt) {
        this.expiryAt = expiryAt;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public VirtualAccountStatus getStatus() {
        return status;
    }

    public void setStatus(VirtualAccountStatus status) {
        this.status = status;
    }
}
