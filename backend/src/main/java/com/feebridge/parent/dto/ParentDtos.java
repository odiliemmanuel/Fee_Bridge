package com.feebridge.parent.dto;

import com.feebridge.common.domain.ResidencyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ParentDtos {

    private ParentDtos() {
    }

    /** Create a login for a guardian so they can access the parent portal. */
    public record CreateLoginRequest(
            String email,
            @NotBlank @Size(min = 8) String password
    ) {
    }

    public record ChildDto(
            Long studentId,
            String admissionNo,
            String fullName,
            Long classId,
            String className,
            ResidencyType residencyType,
            BigDecimal outstandingNaira,
            BigDecimal creditNaira
    ) {
    }

    public record GuardianProfileDto(
            Long guardianId,
            String fullName,
            String email,
            String phone
    ) {
    }
}
