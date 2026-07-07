package com.feebridge.auth.domain;

/** Canonical role names; Spring Security authorities are exposed as ROLE_&lt;name&gt;. */
public final class RoleNames {

    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String SCHOOL_ADMIN = "SCHOOL_ADMIN";
    public static final String BURSAR = "BURSAR";
    public static final String PARENT = "PARENT";
    public static final String GUARDIAN = "GUARDIAN";

    private RoleNames() {
    }
}
