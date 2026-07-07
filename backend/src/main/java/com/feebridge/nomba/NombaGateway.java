package com.feebridge.nomba;

import com.feebridge.common.money.Money;

import java.time.Instant;

/** Abstraction over Nomba so the app can run against the real API or a local fake. */
public interface NombaGateway {

    /** Result of minting a dynamic virtual account. */
    record VirtualAccountResult(String accountNumber, String accountName, String bankName, Instant expiryAt) {
    }

    /**
     * Create a dynamic virtual account for a payment order.
     *
     * @param accountRef     our unique reference (the payment order reference) that Nomba echoes back
     * @param accountName    the name to show on the account
     * @param expectedAmount the exact amount the account should accept
     * @param expiry         when the dynamic account should expire
     */
    VirtualAccountResult createVirtualAccount(String accountRef, String accountName, Money expectedAmount, Instant expiry);
}
