package com.feebridge.auth.security;

/** The authenticated actor stored as the Spring Security principal. */
public record AuthPrincipal(Long userId, Long schoolId, String email, boolean platformAdmin) {
}
