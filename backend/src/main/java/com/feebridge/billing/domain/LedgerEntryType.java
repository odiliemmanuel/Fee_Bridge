package com.feebridge.billing.domain;

/**
 * Kinds of ledger movement. Amounts are stored signed: charges positive (increase what is
 * owed), credits negative (reduce what is owed). Every entry is tied to an invoice, so an
 * invoice's balance always equals the signed sum of its ledger entries ("verify with balance").
 * Payments are capped at the invoice balance; any surplus goes to the student's credit wallet
 * (tracked on StudentCredit, applied to future invoices as CREDIT_CARRY_IN).
 */
public enum LedgerEntryType {
    DEBIT_FEE,               // +  term fee charged
    CREDIT_SCHOLARSHIP,      // -  scholarship discount
    CREDIT_CARRY_IN,         // -  credit applied from the student's wallet
    CREDIT_PAYMENT,          // -  online (Nomba) payment received
    CREDIT_OFFLINE_PAYMENT,  // -  cash / offline payment recorded
    ADJUSTMENT               // +/- manual correction
}
