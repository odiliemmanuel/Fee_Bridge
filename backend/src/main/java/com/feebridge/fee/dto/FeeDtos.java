package com.feebridge.fee.dto;

import com.feebridge.common.domain.ResidencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public final class FeeDtos {

    private FeeDtos() {
    }

    public record UpsertFeeRequest(
            @NotNull Long classId,
            @NotNull Long termId,
            @NotNull ResidencyType residencyType,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal amountNaira,
            String description,
            String reason
    ) {
    }

    public record FeeDto(
            Long id,
            Long classId,
            String className,
            Long sessionId,
            Long termId,
            String termName,
            ResidencyType residencyType,
            BigDecimal amountNaira,
            String description
    ) {
    }

    public record FeeChangeDto(
            Long id,
            Long classId,
            Long termId,
            ResidencyType residencyType,
            BigDecimal oldAmountNaira,
            BigDecimal newAmountNaira,
            String reason,
            Long changedByUserId,
            Instant changedAt
    ) {
    }
}
