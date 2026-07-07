package com.feebridge.audit;

import com.feebridge.audit.domain.AuditLog;
import com.feebridge.audit.repo.AuditLogRepository;
import com.feebridge.common.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(Long schoolId, Long actorUserId, String action, String entityType, String entityId,
                       String beforeJson, String afterJson, String ip) {
        repository.save(AuditLog.of(schoolId, actorUserId, action, entityType, entityId, beforeJson, afterJson, ip));
    }

    /** Records using the current request's tenant/user from {@link TenantContext}. */
    @Transactional
    public void recordCurrent(String action, String entityType, String entityId, String afterJson) {
        record(TenantContext.getSchoolId(), TenantContext.getUserId(), action, entityType, entityId,
                null, afterJson, null);
    }
}
