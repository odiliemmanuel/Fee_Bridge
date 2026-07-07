package com.feebridge.notification.domain;

import com.feebridge.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {

    public enum Channel { EMAIL, SMS }

    public enum Status { SENT, FAILED }

    @Column(name = "school_id")
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String recipient;

    private String subject;

    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "provider_ref")
    private String providerRef;

    private String error;

    public static NotificationLog of(Long schoolId, Channel channel, String recipient, String subject, String body,
                                     Status status, String error) {
        NotificationLog n = new NotificationLog();
        n.schoolId = schoolId;
        n.channel = channel;
        n.recipient = recipient;
        n.subject = subject;
        n.body = body;
        n.status = status;
        n.error = error;
        return n;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public Status getStatus() {
        return status;
    }

    public String getProviderRef() {
        return providerRef;
    }

    public String getError() {
        return error;
    }
}
