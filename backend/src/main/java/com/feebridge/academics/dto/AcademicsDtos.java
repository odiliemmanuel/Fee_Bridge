package com.feebridge.academics.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public final class AcademicsDtos {

    private AcademicsDtos() {
    }

    public record CreateSessionRequest(
            @NotBlank String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean makeCurrent
    ) {
    }

    public record TermDto(
            Long id,
            String name,
            int sequence,
            LocalDate startDate,
            LocalDate endDate,
            boolean current
    ) {
    }

    public record SessionDto(
            Long id,
            String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean current,
            List<TermDto> terms
    ) {
    }

    public record CreateClassRequest(
            @NotBlank String name,
            @Min(0) int levelOrder
    ) {
    }

    public record ClassDto(
            Long id,
            String name,
            int levelOrder
    ) {
    }
}
