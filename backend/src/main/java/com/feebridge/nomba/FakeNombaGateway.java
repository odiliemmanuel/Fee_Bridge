package com.feebridge.nomba;

import com.feebridge.common.money.Money;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Local/sandbox gateway used when {@code feebridge.nomba.enabled=false} (the default). It mints a
 * deterministic fake account number so the whole payment + webhook flow is demoable without Nomba.
 */
@Component
@ConditionalOnProperty(name = "feebridge.nomba.enabled", havingValue = "false", matchIfMissing = true)
public class FakeNombaGateway implements NombaGateway {

    @Override
    public VirtualAccountResult createVirtualAccount(String accountRef, String accountName, Money expectedAmount,
                                                     Instant expiry) {
        // Derive a stable 10-digit "account number" from the reference.
        long hash = Math.abs((long) accountRef.hashCode()) % 1_0000_00000L;
        String accountNumber = String.format("90%08d", hash);
        return new VirtualAccountResult(accountNumber, accountName, "FeeBridge Sandbox Bank", expiry);
    }
}
