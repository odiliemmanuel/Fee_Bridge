package com.feebridge.academics;

import com.feebridge.academics.dto.AcademicsDtos.ClassDto;
import com.feebridge.academics.dto.AcademicsDtos.CreateClassRequest;
import com.feebridge.academics.dto.AcademicsDtos.CreateSessionRequest;
import com.feebridge.academics.dto.AcademicsDtos.SessionDto;
import com.feebridge.academics.dto.AcademicsDtos.TermDto;
import com.feebridge.auth.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class AcademicsController {

    private final AcademicsService service;

    public AcademicsController(AcademicsService service) {
        this.service = service;
    }

    @PostMapping("/api/sessions")
    public SessionDto createSession(@Valid @RequestBody CreateSessionRequest request) {
        return service.createSession(CurrentUser.schoolId(), request);
    }

    @GetMapping("/api/sessions")
    public List<SessionDto> listSessions() {
        return service.listSessions(CurrentUser.schoolId());
    }

    @PostMapping("/api/sessions/{id}/activate")
    public SessionDto activateSession(@PathVariable Long id) {
        return service.activateSession(CurrentUser.schoolId(), id);
    }

    @PostMapping("/api/terms/{id}/activate")
    public TermDto activateTerm(@PathVariable Long id) {
        return service.activateTerm(CurrentUser.schoolId(), id);
    }

    @PostMapping("/api/classes")
    public ClassDto createClass(@Valid @RequestBody CreateClassRequest request) {
        return service.createClass(CurrentUser.schoolId(), request);
    }

    @GetMapping("/api/classes")
    public List<ClassDto> listClasses() {
        return service.listClasses(CurrentUser.schoolId());
    }
}
