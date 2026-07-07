package com.feebridge.nomba;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** HMAC-SHA256 helpers for signing/verifying Nomba webhook payloads. */
public final class NombaSignature {

    private NombaSignature() {
    }

    public static String hmacSha256Hex(String payload, String signatureKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signatureKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to compute HMAC signature", ex);
        }
    }

    /** Constant-time comparison of the expected and provided signatures. */
    public static boolean verify(String payload, String providedSignature, String signatureKey) {
        if (providedSignature == null) {
            return false;
        }
        String expected = hmacSha256Hex(payload, signatureKey);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                providedSignature.trim().getBytes(StandardCharsets.UTF_8));
    }
}
