package com.feebridge.notification.sms;

/** Pluggable SMS transport (log by default; Termii/Twilio can be added as implementations). */
public interface SmsProvider {

    /** Sends the SMS and returns a provider reference. Throws on failure. */
    String send(String to, String body);
}
