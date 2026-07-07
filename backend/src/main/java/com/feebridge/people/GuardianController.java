package com.feebridge.people;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.common.web.PageResponse;
import com.feebridge.parent.ParentService;
import com.feebridge.parent.dto.ParentDtos.CreateLoginRequest;
import com.feebridge.people.dto.PeopleDtos.CreateGuardianRequest;
import com.feebridge.people.dto.PeopleDtos.GuardianDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/guardians")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class GuardianController {

    private final GuardianService guardianService;
    private final ParentService parentService;

    public GuardianController(GuardianService guardianService, ParentService parentService) {
        this.guardianService = guardianService;
        this.parentService = parentService;
    }

    @PostMapping
    public GuardianDto create(@Valid @RequestBody CreateGuardianRequest request) {
        return guardianService.createGuardian(CurrentUser.schoolId(), request);
    }

    @GetMapping
    public PageResponse<GuardianDto> search(@RequestParam(required = false) String q,
                                            @PageableDefault(size = 20) Pageable pageable) {
        return guardianService.search(CurrentUser.schoolId(), q, pageable);
    }

    @GetMapping("/{id}")
    public GuardianDto get(@PathVariable Long id) {
        return guardianService.getGuardian(CurrentUser.schoolId(), id);
    }

    /** Provision a parent-portal login for this guardian. */
    @PostMapping("/{id}/login")
    public Map<String, Long> createLogin(@PathVariable Long id, @Valid @RequestBody CreateLoginRequest request) {
        Long userId = parentService.createLogin(CurrentUser.schoolId(), id, request);
        return Map.of("userId", userId);
    }
}
