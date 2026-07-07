package com.feebridge.reconciliation.dto;

import com.feebridge.reconciliation.domain.ReconciliationException.Type;

import java.math.BigDecimal;
import java.time.Instant;

public final class ReconciliationDtos {

    private ReconciliationDtos() {
    }

    public record ExceptionDto(
            Long id,
            Long schoolId,
            Type type,
            Long transactionId,
            String reference,
            BigDecimal expectedNaira,
            BigDecimal actualNaira,
            String detail,
            boolean resolved,
            Instant createdAt
    ) {
    }

    public record RunResultDto(int checked, int exceptionsCreated) {
    }
}
