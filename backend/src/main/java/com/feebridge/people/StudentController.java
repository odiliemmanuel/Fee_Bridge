package com.feebridge.people;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.common.web.PageResponse;
import com.feebridge.people.domain.StudentStatus;
import com.feebridge.people.dto.PeopleDtos.CreateStudentRequest;
import com.feebridge.people.dto.PeopleDtos.EnrollRequest;
import com.feebridge.people.dto.PeopleDtos.EnrollmentDto;
import com.feebridge.people.dto.PeopleDtos.MapGuardianRequest;
import com.feebridge.people.dto.PeopleDtos.StudentDto;
import com.feebridge.people.dto.PeopleDtos.StudentGuardianDto;
import com.feebridge.people.dto.PeopleDtos.StudentWithGuardiansDto;
import com.feebridge.people.dto.PeopleDtos.UpdateStudentRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class StudentController {

    private final StudentService studentService;
    private final GuardianService guardianService;

    public StudentController(StudentService studentService, GuardianService guardianService) {
        this.studentService = studentService;
        this.guardianService = guardianService;
    }

    @PostMapping
    public StudentDto create(@Valid @RequestBody CreateStudentRequest request) {
        return studentService.createStudent(CurrentUser.schoolId(), request);
    }

    @PutMapping("/{id}")
    public StudentDto update(@PathVariable Long id, @Valid @RequestBody UpdateStudentRequest request) {
        return studentService.updateStudent(CurrentUser.schoolId(), id, request);
    }

    @GetMapping("/{id}")
    public StudentDto get(@PathVariable Long id) {
        return studentService.getStudent(CurrentUser.schoolId(), id);
    }

    @GetMapping("/{id}/details")
    public StudentWithGuardiansDto details(@PathVariable Long id) {
        return studentService.getStudentDetails(CurrentUser.schoolId(), id);
    }

    @GetMapping
    public PageResponse<StudentDto> search(@RequestParam(required = false) String q,
                                           @RequestParam(required = false) Long classId,
                                           @RequestParam(required = false) StudentStatus status,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return studentService.search(CurrentUser.schoolId(), q, classId, status, pageable);
    }

    @PostMapping("/{id}/enroll")
    public EnrollmentDto enroll(@PathVariable Long id, @Valid @RequestBody EnrollRequest request) {
        return studentService.enroll(CurrentUser.schoolId(), id, request);
    }

    @PostMapping("/{id}/guardians")
    public StudentGuardianDto mapGuardian(@PathVariable Long id, @Valid @RequestBody MapGuardianRequest request) {
        return guardianService.mapToStudent(CurrentUser.schoolId(), id, request);
    }

    @GetMapping("/{id}/guardians")
    public List<StudentGuardianDto> guardians(@PathVariable Long id) {
        return guardianService.guardiansOfStudent(CurrentUser.schoolId(), id);
    }
}
