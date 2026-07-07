package com.feebridge.auth.security;

import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessor for the authenticated principal inside controllers/services. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthPrincipal get() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }
        return null;
    }

    public static Long userId() {
        AuthPrincipal p = get();
        return p == null ? null : p.userId();
    }

    public static Long schoolId() {
        AuthPrincipal p = get();
        return p == null ? null : p.schoolId();
    }
}
