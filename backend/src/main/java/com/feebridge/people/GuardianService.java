package com.feebridge.people;

import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.ConflictException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.web.PageResponse;
import com.feebridge.people.domain.Guardian;
import com.feebridge.people.domain.StudentGuardian;
import com.feebridge.people.dto.PeopleDtos.CreateGuardianRequest;
import com.feebridge.people.dto.PeopleDtos.GuardianDto;
import com.feebridge.people.dto.PeopleDtos.GuardianInput;
import com.feebridge.people.dto.PeopleDtos.MapGuardianRequest;
import com.feebridge.people.dto.PeopleDtos.StudentGuardianDto;
import com.feebridge.people.repo.GuardianRepository;
import com.feebridge.people.repo.StudentGuardianRepository;
import com.feebridge.people.repo.StudentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentRepository studentRepository;

    public GuardianService(GuardianRepository guardianRepository,
                           StudentGuardianRepository studentGuardianRepository,
                           StudentRepository studentRepository) {
        this.guardianRepository = guardianRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public GuardianDto createGuardian(Long schoolId, CreateGuardianRequest req) {
        Guardian g = new Guardian();
        g.setSchoolId(schoolId);
        g.setFirstName(req.firstName());
        g.setLastName(req.lastName());
        g.setEmail(req.email());
        g.setPhone(req.phone());
        g.setAddress(req.address());
        return toDto(guardianRepository.save(g));
    }

    @Transactional(readOnly = true)
    public PageResponse<GuardianDto> search(Long schoolId, String q, Pageable pageable) {
        return PageResponse.from(guardianRepository.search(schoolId, blankToNull(q), pageable), this::toDto);
    }

    @Transactional(readOnly = true)
    public GuardianDto getGuardian(Long schoolId, Long id) {
        return toDto(requireGuardian(schoolId, id));
    }

    /**
     * Reuses an existing guardian (by id, else by phone within the school) or creates a new one.
     * This is how "if the parent already exists, just map the student to them" is satisfied.
     */
    @Transactional
    public Guardian resolveOrCreate(Long schoolId, GuardianInput input) {
        if (input.id() != null) {
            return requireGuardian(schoolId, input.id());
        }
        if (input.phone() != null && !input.phone().isBlank()) {
            var existing = guardianRepository.findBySchoolIdAndPhone(schoolId, input.phone());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        if (input.firstName() == null || input.lastName() == null || input.phone() == null) {
            throw new BadRequestException("New guardian requires firstName, lastName and phone");
        }
        Guardian g = new Guardian();
        g.setSchoolId(schoolId);
        g.setFirstName(input.firstName());
        g.setLastName(input.lastName());
        g.setEmail(input.email());
        g.setPhone(input.phone());
        g.setAddress(input.address());
        return guardianRepository.save(g);
    }

    @Transactional
    public StudentGuardianDto mapToStudent(Long schoolId, Long studentId, MapGuardianRequest req) {
        var student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        Guardian guardian = resolveOrCreate(schoolId, req.guardian());
        if (studentGuardianRepository.existsByStudentIdAndGuardianId(student.getId(), guardian.getId())) {
            throw new ConflictException("This guardian is already mapped to the student");
        }
        StudentGuardian sg = new StudentGuardian();
        sg.setSchoolId(schoolId);
        sg.setStudentId(student.getId());
        sg.setGuardianId(guardian.getId());
        sg.setRelationship(req.relationship());
        sg.setPrimary(req.isPrimary());
        sg.setPayer(req.isPayer());
        sg.setDelegatedPayer(req.isDelegatedPayer());
        sg = studentGuardianRepository.save(sg);
        return toMappingDto(sg, guardian);
    }

    @Transactional(readOnly = true)
    public List<StudentGuardianDto> guardiansOfStudent(Long schoolId, Long studentId) {
        return studentGuardianRepository.findByStudentId(studentId).stream()
                .map(sg -> toMappingDto(sg, requireGuardian(schoolId, sg.getGuardianId())))
                .toList();
    }

    private Guardian requireGuardian(Long schoolId, Long id) {
        return guardianRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> NotFoundException.of("Guardian", id));
    }

    private GuardianDto toDto(Guardian g) {
        return new GuardianDto(g.getId(), g.getFirstName(), g.getLastName(), g.fullName(),
                g.getEmail(), g.getPhone(), g.getAddress(), g.getUserId());
    }

    private StudentGuardianDto toMappingDto(StudentGuardian sg, Guardian g) {
        return new StudentGuardianDto(sg.getId(), sg.getStudentId(), sg.getGuardianId(), g.fullName(),
                g.getPhone(), g.getEmail(), sg.getRelationship(), sg.isPrimary(), sg.isPayer(), sg.isDelegatedPayer());
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
