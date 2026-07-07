package com.feebridge.audit.repo;

import com.feebridge.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);

    Page<AuditLog> findBySchoolIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            Long schoolId, String entityType, String entityId, Pageable pageable);
}
