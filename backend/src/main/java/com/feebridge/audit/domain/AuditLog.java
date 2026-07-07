package com.feebridge.audit.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** An immutable record of a change: who did what, to which entity, with before/after snapshots. */
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "before_json")
    private String beforeJson;

    @Column(name = "after_json")
    private String afterJson;

    @Column(name = "ip_address")
    private String ipAddress;

    public static AuditLog of(Long schoolId, Long actorUserId, String action, String entityType, String entityId,
                              String beforeJson, String afterJson, String ipAddress) {
        AuditLog a = new AuditLog();
        a.schoolId = schoolId;
        a.actorUserId = actorUserId;
        a.action = action;
        a.entityType = entityType;
        a.entityId = entityId;
        a.beforeJson = beforeJson;
        a.afterJson = afterJson;
        a.ipAddress = ipAddress;
        return a;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
