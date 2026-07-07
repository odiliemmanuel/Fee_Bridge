package com.feebridge.notification;

import com.feebridge.auth.security.CurrentUser;
import com.feebridge.common.web.PageResponse;
import com.feebridge.notification.domain.NotificationLog;
import com.feebridge.notification.repo.NotificationLogRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("hasAnyRole('SCHOOL_ADMIN','BURSAR')")
public class NotificationController {

    private final NotificationLogRepository logRepository;

    public NotificationController(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public record NotificationLogDto(Long id, String channel, String recipient, String subject, String status,
                                     String error, Instant createdAt) {
    }


    @GetMapping
    public PageResponse<NotificationLogDto> list(@PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(logRepository.findBySchoolIdOrderByCreatedAtDesc(CurrentUser.schoolId(), pageable),
                this::toDto);
    }

    private NotificationLogDto toDto(NotificationLog n) {
        return new NotificationLogDto(n.getId(), n.getChannel().name(), n.getRecipient(), n.getSubject(),
                n.getStatus().name(), n.getError(), n.getCreatedAt());
    }
}
