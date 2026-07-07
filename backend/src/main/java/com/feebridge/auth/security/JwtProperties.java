package com.feebridge.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "feebridge.security.jwt")
public record JwtProperties(String secret, long accessTokenTtlMinutes) {
}
