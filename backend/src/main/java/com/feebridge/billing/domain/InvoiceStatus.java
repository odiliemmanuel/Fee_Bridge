package com.feebridge.billing.domain;

/** Payment status of an invoice; drives the school's status filters. */
public enum InvoiceStatus {
    PENDING,    // nothing paid yet
    PARTIAL,    // some paid, balance remaining
    PAID,       // fully settled
    OVERPAID    // settled and produced surplus credit (carried to next term)
}
