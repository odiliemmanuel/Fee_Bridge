package com.feebridge.scholarship;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.scholarship.dto.ScholarshipDtos.AwardScholarshipRequest;
import com.feebridge.scholarship.dto.ScholarshipDtos.CreateScholarshipRequest;
import com.feebridge.scholarship.dto.ScholarshipDtos.ScholarshipDto;
import com.feebridge.scholarship.dto.ScholarshipDtos.StudentScholarshipDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class ScholarshipController {

    private final ScholarshipService service;

    public ScholarshipController(ScholarshipService service) {
        this.service = service;
    }

    @PostMapping("/api/scholarships")
    public ScholarshipDto create(@Valid @RequestBody CreateScholarshipRequest request) {
        return service.create(CurrentUser.schoolId(), request);
    }

    @GetMapping("/api/scholarships")
    public List<ScholarshipDto> list() {
        return service.list(CurrentUser.schoolId());
    }

    @PostMapping("/api/scholarships/awards")
    public StudentScholarshipDto award(@Valid @RequestBody AwardScholarshipRequest request) {
        return service.award(CurrentUser.schoolId(), CurrentUser.userId(), request);
    }

    @DeleteMapping("/api/scholarships/awards/{id}")
    public void revoke(@PathVariable Long id) {
        service.revoke(CurrentUser.schoolId(), id);
    }

    @GetMapping("/api/scholarships/awards")
    public List<StudentScholarshipDto> studentAwards(@RequestParam Long studentId) {
        return service.studentAwards(CurrentUser.schoolId(), studentId);
    }
}
