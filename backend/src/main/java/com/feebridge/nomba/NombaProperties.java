package com.feebridge.nomba;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "feebridge.nomba")
public record NombaProperties(
        String baseUrl,
        String clientId,
        String clientSecret,
        String accountId,
        String signatureKey,
        int virtualAccountTtlMinutes,
        boolean enabled
) {
}
