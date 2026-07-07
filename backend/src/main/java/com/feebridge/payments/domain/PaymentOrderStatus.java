package com.feebridge.payments.domain;

public enum PaymentOrderStatus {
    AWAITING_PAYMENT,
    PAID,
    PARTIALLY_SETTLED,
    EXPIRED,
    CANCELLED
}
