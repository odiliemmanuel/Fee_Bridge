package com.feebridge.people.dto;

import com.feebridge.common.domain.ResidencyType;
import com.feebridge.people.domain.Gender;
import com.feebridge.people.domain.GuardianRelationship;
import com.feebridge.people.domain.StudentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class PeopleDtos {

    private PeopleDtos() {
    }

    /** Reference an existing guardian by id, or supply details to create/reuse one (matched by phone). */
    public record GuardianInput(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String address
    ) {
    }

    public record MapGuardianRequest(
            @NotNull @Valid GuardianInput guardian,
            @NotNull GuardianRelationship relationship,
            boolean isPrimary,
            boolean isPayer,
            boolean isDelegatedPayer
    ) {
    }

    public record CreateStudentRequest(
            String admissionNo,
            @NotBlank String firstName,
            @NotBlank String lastName,
            String middleName,
            Gender gender,
            LocalDate dateOfBirth,
            Long classId,
            @NotNull ResidencyType residencyType,
            String photoUrl,
            boolean autoEnroll,
            @Valid MapGuardianRequest guardian
    ) {
    }

    public record UpdateStudentRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String middleName,
            Gender gender,
            LocalDate dateOfBirth,
            Long classId,
            @NotNull ResidencyType residencyType,
            String photoUrl,
            StudentStatus status
    ) {
    }

    public record StudentDto(
            Long id,
            String admissionNo,
            String firstName,
            String lastName,
            String middleName,
            String fullName,
            Gender gender,
            LocalDate dateOfBirth,
            Long classId,
            String className,
            ResidencyType residencyType,
            String photoUrl,
            StudentStatus status
    ) {
    }

    public record CreateGuardianRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String email,
            @NotBlank String phone,
            String address
    ) {
    }

    public record GuardianDto(
            Long id,
            String firstName,
            String lastName,
            String fullName,
            String email,
            String phone,
            String address,
            Long userId
    ) {
    }

    public record StudentGuardianDto(
            Long id,
            Long studentId,
            Long guardianId,
            String guardianName,
            String guardianPhone,
            String guardianEmail,
            GuardianRelationship relationship,
            boolean isPrimary,
            boolean isPayer,
            boolean isDelegatedPayer
    ) {
    }

    public record EnrollRequest(
            @NotNull Long sessionId,
            @NotNull Long classId,
            @NotNull ResidencyType residencyType
    ) {
    }

    public record EnrollmentDto(
            Long id,
            Long studentId,
            Long sessionId,
            Long classId,
            String className,
            ResidencyType residencyType,
            String status
    ) {
    }

    public record StudentWithGuardiansDto(
            StudentDto student,
            List<StudentGuardianDto> guardians,
            List<EnrollmentDto> enrollments
    ) {
    }
}
