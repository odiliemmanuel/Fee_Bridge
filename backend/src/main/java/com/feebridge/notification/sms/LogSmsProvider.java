package com.feebridge.notification.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Default SMS provider: logs the message. Swap for Termii/Twilio in production. */
@Component
public class LogSmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(LogSmsProvider.class);

    @Override
    public String send(String to, String body) {
        String ref = "SMS-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("SMS [{}] -> {} : {}", ref, to, body);
        return ref;
    }
}
