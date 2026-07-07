package com.feebridge.people;

import com.feebridge.academics.domain.AcademicSession;
import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.repo.AcademicSessionRepository;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.ConflictException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.common.web.PageResponse;
import com.feebridge.people.domain.Enrollment;
import com.feebridge.people.domain.Student;
import com.feebridge.people.domain.StudentStatus;
import com.feebridge.people.dto.PeopleDtos.CreateStudentRequest;
import com.feebridge.people.dto.PeopleDtos.EnrollRequest;
import com.feebridge.people.dto.PeopleDtos.EnrollmentDto;
import com.feebridge.people.dto.PeopleDtos.StudentDto;
import com.feebridge.people.dto.PeopleDtos.StudentWithGuardiansDto;
import com.feebridge.people.dto.PeopleDtos.UpdateStudentRequest;
import com.feebridge.people.repo.EnrollmentRepository;
import com.feebridge.people.repo.StudentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolClassRepository classRepository;
    private final AcademicSessionRepository sessionRepository;
    private final GuardianService guardianService;

    public StudentService(StudentRepository studentRepository, EnrollmentRepository enrollmentRepository,
                          SchoolClassRepository classRepository, AcademicSessionRepository sessionRepository,
                          GuardianService guardianService) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.classRepository = classRepository;
        this.sessionRepository = sessionRepository;
        this.guardianService = guardianService;
    }

    @Transactional
    public StudentDto createStudent(Long schoolId, CreateStudentRequest req) {
        if (req.classId() != null) {
            requireClass(schoolId, req.classId());
        }
        String admissionNo = (req.admissionNo() == null || req.admissionNo().isBlank())
                ? generateAdmissionNo(schoolId) : req.admissionNo();
        if (studentRepository.existsBySchoolIdAndAdmissionNo(schoolId, admissionNo)) {
            throw new ConflictException("Admission number " + admissionNo + " already exists");
        }

        Student s = new Student();
        s.setSchoolId(schoolId);
        s.setAdmissionNo(admissionNo);
        s.setFirstName(req.firstName());
        s.setLastName(req.lastName());
        s.setMiddleName(req.middleName());
        s.setGender(req.gender());
        s.setDateOfBirth(req.dateOfBirth());
        s.setClassId(req.classId());
        s.setResidencyType(req.residencyType());
        s.setPhotoUrl(req.photoUrl());
        final Student student = studentRepository.save(s);

        if (req.autoEnroll() && req.classId() != null) {
            sessionRepository.findBySchoolIdAndCurrentTrue(schoolId).ifPresent(session ->
                    upsertEnrollment(schoolId, student, session.getId(), req.classId(), req.residencyType()));
        }
        if (req.guardian() != null) {
            guardianService.mapToStudent(schoolId, student.getId(), req.guardian());
        }
        return toDto(schoolId, student);
    }

    @Transactional
    public StudentDto updateStudent(Long schoolId, Long id, UpdateStudentRequest req) {
        Student s = requireStudent(schoolId, id);
        if (req.classId() != null) {
            requireClass(schoolId, req.classId());
        }
        s.setFirstName(req.firstName());
        s.setLastName(req.lastName());
        s.setMiddleName(req.middleName());
        s.setGender(req.gender());
        s.setDateOfBirth(req.dateOfBirth());
        s.setClassId(req.classId());
        s.setResidencyType(req.residencyType());
        s.setPhotoUrl(req.photoUrl());
        if (req.status() != null) {
            s.setStatus(req.status());
        }
        return toDto(schoolId, s);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudent(Long schoolId, Long id) {
        return toDto(schoolId, requireStudent(schoolId, id));
    }

    @Transactional(readOnly = true)
    public StudentWithGuardiansDto getStudentDetails(Long schoolId, Long id) {
        Student s = requireStudent(schoolId, id);
        var enrollments = enrollmentRepository.findByStudentId(id).stream()
                .map(e -> toEnrollmentDto(schoolId, e)).toList();
        return new StudentWithGuardiansDto(toDto(schoolId, s),
                guardianService.guardiansOfStudent(schoolId, id), enrollments);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentDto> search(Long schoolId, String q, Long classId, StudentStatus status,
                                           Pageable pageable) {
        var page = studentRepository.search(schoolId, blankToNull(q), classId, status, pageable);
        return PageResponse.from(page, s -> toDto(schoolId, s));
    }

    @Transactional
    public EnrollmentDto enroll(Long schoolId, Long studentId, EnrollRequest req) {
        Student s = requireStudent(schoolId, studentId);
        requireClass(schoolId, req.classId());
        Enrollment e = upsertEnrollment(schoolId, s, req.sessionId(), req.classId(), req.residencyType());
        return toEnrollmentDto(schoolId, e);
    }

    // ---- helpers ----------------------------------------------------------

    private Enrollment upsertEnrollment(Long schoolId, Student s, Long sessionId, Long classId,
                                        com.feebridge.common.domain.ResidencyType residency) {
        Enrollment e = enrollmentRepository.findByStudentIdAndSessionId(s.getId(), sessionId)
                .orElseGet(Enrollment::new);
        e.setSchoolId(schoolId);
        e.setStudentId(s.getId());
        e.setSessionId(sessionId);
        e.setClassId(classId);
        e.setResidencyType(residency);
        e = enrollmentRepository.save(e);
        // If enrolling into the current session, reflect it on the student's current class.
        Optional<AcademicSession> current = sessionRepository.findBySchoolIdAndCurrentTrue(schoolId);
        if (current.isPresent() && current.get().getId().equals(sessionId)) {
            s.setClassId(classId);
            s.setResidencyType(residency);
        }
        return e;
    }

    private Student requireStudent(Long schoolId, Long id) {
        return studentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> NotFoundException.of("Student", id));
    }

    private SchoolClass requireClass(Long schoolId, Long classId) {
        return classRepository.findByIdAndSchoolId(classId, schoolId)
                .orElseThrow(() -> new BadRequestException("Class " + classId + " does not exist"));
    }

    private String className(Long schoolId, Long classId) {
        if (classId == null) {
            return null;
        }
        return classRepository.findByIdAndSchoolId(classId, schoolId).map(SchoolClass::getName).orElse(null);
    }

    private String generateAdmissionNo(Long schoolId) {
        long count = studentRepository.countBySchoolId(schoolId);
        String candidate;
        do {
            candidate = "STU" + String.format("%04d", ++count);
        } while (studentRepository.existsBySchoolIdAndAdmissionNo(schoolId, candidate));
        return candidate;
    }

    private StudentDto toDto(Long schoolId, Student s) {
        return new StudentDto(s.getId(), s.getAdmissionNo(), s.getFirstName(), s.getLastName(), s.getMiddleName(),
                s.fullName(), s.getGender(), s.getDateOfBirth(), s.getClassId(), className(schoolId, s.getClassId()),
                s.getResidencyType(), s.getPhotoUrl(), s.getStatus());
    }

    private EnrollmentDto toEnrollmentDto(Long schoolId, Enrollment e) {
        return new EnrollmentDto(e.getId(), e.getStudentId(), e.getSessionId(), e.getClassId(),
                className(schoolId, e.getClassId()), e.getResidencyType(), e.getStatus().name());
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
