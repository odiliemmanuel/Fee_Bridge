package com.feebridge.audit;

import com.feebridge.audit.domain.AuditLog;
import com.feebridge.audit.repo.AuditLogRepository;
import com.feebridge.auth.security.CurrentUser;
import com.feebridge.common.web.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    public record AuditLogDto(Long id, Long actorUserId, String action, String entityType, String entityId,
                              String ipAddress, Instant createdAt) {
    }

    @GetMapping
    public PageResponse<AuditLogDto> list(@RequestParam(required = false) String entityType,
                                          @RequestParam(required = false) String entityId,
                                          @PageableDefault(size = 30) Pageable pageable) {
        Long schoolId = CurrentUser.schoolId();
        var page = (entityType != null && entityId != null)
                ? repository.findBySchoolIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(schoolId, entityType, entityId, pageable)
                : repository.findBySchoolIdOrderByCreatedAtDesc(schoolId, pageable);
        return PageResponse.from(page, this::toDto);
    }

    private AuditLogDto toDto(AuditLog a) {
        return new AuditLogDto(a.getId(), a.getActorUserId(), a.getAction(), a.getEntityType(), a.getEntityId(),
                a.getIpAddress(), a.getCreatedAt());
    }
}
