package com.feebridge.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void convertsNairaToKoboExactly() {
        assertThat(Money.ofNaira(1500).kobo()).isEqualTo(150_000L);
        assertThat(Money.ofNaira(new BigDecimal("1500.55")).kobo()).isEqualTo(150_055L);
        assertThat(Money.ofKobo(150_055L).toNaira()).isEqualByComparingTo("1500.55");
    }

    @Test
    void addsAndSubtracts() {
        Money a = Money.ofNaira(1000);
        Money b = Money.ofNaira(250);
        assertThat(a.plus(b).toNaira()).isEqualByComparingTo("1250");
        assertThat(a.minus(b).toNaira()).isEqualByComparingTo("750");
        assertThat(b.minus(a).isNegative()).isTrue();
    }

    @Test
    void computesPercentageHalfUp() {
        // 25% of 1500.00 = 375.00
        assertThat(Money.ofNaira(1500).percentage(new BigDecimal("25")).toNaira()).isEqualByComparingTo("375");
        // 33% of 100.00 = 33.00
        assertThat(Money.ofNaira(100).percentage(new BigDecimal("33")).toNaira()).isEqualByComparingTo("33");
        // 12.5% of 100.00 = 12.50
        assertThat(Money.ofNaira(100).percentage(new BigDecimal("12.5")).toNaira()).isEqualByComparingTo("12.50");
    }

    @Test
    void comparisonsAndMinMax() {
        Money low = Money.ofNaira(100);
        Money high = Money.ofNaira(200);
        assertThat(high.gt(low)).isTrue();
        assertThat(low.lt(high)).isTrue();
        assertThat(low.gte(Money.ofNaira(100))).isTrue();
        assertThat(low.min(high)).isEqualTo(low);
        assertThat(low.max(high)).isEqualTo(high);
        assertThat(Money.ZERO.isZero()).isTrue();
    }
}
