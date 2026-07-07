package com.feebridge.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value stored as integer minor units (kobo) to guarantee
 * exact arithmetic in the fee ledger, payment waterfall and reconciliation.
 * Naira/decimal representations are only for API and UI boundaries.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(0L);

    private final long kobo;

    private Money(long kobo) {
        this.kobo = kobo;
    }

    public static Money ofKobo(long kobo) {
        return new Money(kobo);
    }

    public static Money ofNaira(long naira) {
        return new Money(Math.multiplyExact(naira, 100L));
    }

    public static Money ofNaira(BigDecimal naira) {
        Objects.requireNonNull(naira, "naira");
        return new Money(naira.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact());
    }

    public long kobo() {
        return kobo;
    }

    public BigDecimal toNaira() {
        return BigDecimal.valueOf(kobo, 2);
    }

    public Money plus(Money other) {
        return new Money(Math.addExact(kobo, other.kobo));
    }

    public Money minus(Money other) {
        return new Money(Math.subtractExact(kobo, other.kobo));
    }

    /** Percentage of this amount, e.g. percentage(BigDecimal.valueOf(25)) == 25% of it, rounded half-up. */
    public Money percentage(BigDecimal percent) {
        BigDecimal result = BigDecimal.valueOf(kobo)
                .multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        return new Money(result.longValueExact());
    }

    public boolean isZero() {
        return kobo == 0;
    }

    public boolean isPositive() {
        return kobo > 0;
    }

    public boolean isNegative() {
        return kobo < 0;
    }

    public boolean gte(Money other) {
        return kobo >= other.kobo;
    }

    public boolean gt(Money other) {
        return kobo > other.kobo;
    }

    public boolean lt(Money other) {
        return kobo < other.kobo;
    }

    public Money min(Money other) {
        return kobo <= other.kobo ? this : other;
    }

    public Money max(Money other) {
        return kobo >= other.kobo ? this : other;
    }

    @Override
    public int compareTo(Money other) {
        return Long.compare(kobo, other.kobo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return kobo == money.kobo;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(kobo);
    }

    @Override
    public String toString() {
        return toNaira().toPlainString();
    }
}
