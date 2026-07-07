package com.feebridge.notification;

import com.feebridge.notification.domain.NotificationLog;
import com.feebridge.notification.domain.NotificationLog.Channel;
import com.feebridge.notification.domain.NotificationLog.Status;
import com.feebridge.notification.repo.NotificationLogRepository;
import com.feebridge.notification.sms.SmsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Delivers notifications over email (SMTP -> MailHog locally) and SMS, persisting every attempt
 * to {@link NotificationLog}. Delivery failures are recorded, never thrown, so a payment is never
 * rolled back because a notification could not be sent.
 */
@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final JavaMailSender mailSender;
    private final SmsProvider smsProvider;
    private final NotificationLogRepository logRepository;
    private final String from;

    public NotificationSender(JavaMailSender mailSender, SmsProvider smsProvider,
                              NotificationLogRepository logRepository,
                              @Value("${feebridge.mail.from:no-reply@feebridge.africa}") String from) {
        this.mailSender = mailSender;
        this.smsProvider = smsProvider;
        this.logRepository = logRepository;
        this.from = from;
    }

    public void email(Long schoolId, String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logRepository.save(NotificationLog.of(schoolId, Channel.EMAIL, to, subject, body, Status.SENT, null));
        } catch (Exception ex) {
            log.warn("Email to {} failed: {}", to, ex.getMessage());
            logRepository.save(NotificationLog.of(schoolId, Channel.EMAIL, to, subject, body, Status.FAILED,
                    truncate(ex.getMessage())));
        }
    }

    public void sms(Long schoolId, String to, String body) {
        try {
            smsProvider.send(to, body);
            logRepository.save(NotificationLog.of(schoolId, Channel.SMS, to, null, body, Status.SENT, null));
        } catch (Exception ex) {
            log.warn("SMS to {} failed: {}", to, ex.getMessage());
            logRepository.save(NotificationLog.of(schoolId, Channel.SMS, to, null, body, Status.FAILED,
                    truncate(ex.getMessage())));
        }
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() > 290 ? s.substring(0, 290) : s;
    }
}
