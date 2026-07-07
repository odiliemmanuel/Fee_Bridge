package com.feebridge.common.tenant;

/**
 * Holds the current request's tenant (school) id and acting user id, resolved from the
 * JWT by the security filter. Services scope every query by {@link #getSchoolId()} so
 * one school can never read or mutate another school's data.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> SCHOOL_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PLATFORM_ADMIN = ThreadLocal.withInitial(() -> false);

    private TenantContext() {
    }

    public static void set(Long schoolId, Long userId, boolean platformAdmin) {
        SCHOOL_ID.set(schoolId);
        USER_ID.set(userId);
        PLATFORM_ADMIN.set(platformAdmin);
    }

    public static Long getSchoolId() {
        return SCHOOL_ID.get();
    }

    public static Long requireSchoolId() {
        Long id = SCHOOL_ID.get();
        if (id == null) {
            throw new IllegalStateException("No school in the current security context");
        }
        return id;
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static boolean isPlatformAdmin() {
        return Boolean.TRUE.equals(PLATFORM_ADMIN.get());
    }

    public static void clear() {
        SCHOOL_ID.remove();
        USER_ID.remove();
        PLATFORM_ADMIN.remove();
    }
}
