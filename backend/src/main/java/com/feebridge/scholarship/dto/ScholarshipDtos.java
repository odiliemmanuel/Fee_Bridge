package com.feebridge.scholarship.dto;

import com.feebridge.scholarship.domain.ScholarshipStatus;
import com.feebridge.scholarship.domain.ScholarshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class ScholarshipDtos {

    private ScholarshipDtos() {
    }

    public record CreateScholarshipRequest(
            @NotBlank String name,
            @NotNull ScholarshipType type,
            BigDecimal percentage,
            BigDecimal amountNaira,
            String sponsor,
            String description
    ) {
    }

    public record ScholarshipDto(
            Long id,
            String name,
            ScholarshipType type,
            BigDecimal percentage,
            BigDecimal amountNaira,
            String sponsor,
            String description,
            boolean active
    ) {
    }

    /** Award a scholarship to a student: either reference a template ({@code scholarshipId}) or
     *  supply the {@code type} + value directly. Scope to a session/term or leave null for all. */
    public record AwardScholarshipRequest(
            @NotNull Long studentId,
            Long scholarshipId,
            ScholarshipType type,
            BigDecimal percentage,
            BigDecimal amountNaira,
            Long sessionId,
            Long termId,
            String note
    ) {
    }

    public record StudentScholarshipDto(
            Long id,
            Long studentId,
            Long scholarshipId,
            ScholarshipType type,
            BigDecimal percentage,
            BigDecimal amountNaira,
            Long sessionId,
            Long termId,
            ScholarshipStatus status,
            String note
    ) {
    }
}
