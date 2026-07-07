package com.feebridge.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feebridge.common.money.Money;
import com.feebridge.nomba.NombaProperties;
import com.feebridge.nomba.NombaSignature;
import com.feebridge.payments.dto.PaymentDtos.NombaWebhookPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Receives Nomba payment webhooks. The raw body is HMAC-verified against the configured signature
 * key before the transaction is settled. Idempotency is enforced downstream by the transaction ref.
 */
@RestController
public class NombaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(NombaWebhookController.class);

    private final PaymentService paymentService;
    private final NombaProperties nombaProperties;
    private final ObjectMapper objectMapper;

    public NombaWebhookController(PaymentService paymentService, NombaProperties nombaProperties,
                                  ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.nombaProperties = nombaProperties;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/api/webhooks/nomba")
    public ResponseEntity<String> handle(@RequestBody String rawBody,
                                         @RequestHeader(value = "x-nomba-signature", required = false) String signature) {
        if (!NombaSignature.verify(rawBody, signature, nombaProperties.signatureKey())) {
            log.warn("Rejected Nomba webhook with invalid signature");
            return ResponseEntity.status(401).body("invalid signature");
        }
        try {
            NombaWebhookPayload payload = objectMapper.readValue(rawBody, NombaWebhookPayload.class);
            NombaWebhookPayload.Data data = payload.data();
            if (data == null || data.transaction() == null) {
                return ResponseEntity.badRequest().body("missing transaction");
            }
            boolean success = payload.event_type() != null
                    && payload.event_type().toLowerCase().contains("success");
            String accountRef = data.account() != null ? data.account().accountRef() : null;
            Instant paidAt = data.transaction().time() != null ? data.transaction().time() : Instant.now();
            Money amount = Money.ofNaira(data.transaction().amount());

            paymentService.processSettlement(data.transaction().reference(), accountRef, amount, paidAt, rawBody, success);
            return ResponseEntity.ok("processed");
        } catch (Exception ex) {
            log.error("Failed to process Nomba webhook", ex);
            return ResponseEntity.badRequest().body("could not process payload");
        }
    }
}
