package com.feebridge.notification.repo;

import com.feebridge.notification.domain.NotificationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Page<NotificationLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);
}
