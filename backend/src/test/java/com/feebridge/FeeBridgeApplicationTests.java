package com.feebridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Boots the full application context on H2, applying all Flyway migrations. */
@SpringBootTest(properties = "feebridge.demo-data=false")
class FeeBridgeApplicationTests {

    @Test
    void contextLoads() {
        // Verifies Spring wiring, JPA mappings and Flyway migrations are consistent.
    }
}
