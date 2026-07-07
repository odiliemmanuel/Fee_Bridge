package com.feebridge.payments.dto;

import com.feebridge.payments.domain.OfflineMethod;
import com.feebridge.payments.domain.PaymentChannel;
import com.feebridge.payments.domain.PaymentOrderStatus;
import com.feebridge.payments.domain.TransactionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record AllocationInput(
            @NotNull Long studentId,
            Long invoiceId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amountNaira
    ) {
    }

    /** Pay for one or many children in a single transaction. */
    public record CreateOrderRequest(
            Long payerGuardianId,
            String note,
            @NotEmpty @Valid List<AllocationInput> allocations
    ) {
    }

    public record VirtualAccountDto(
            String accountNumber,
            String accountName,
            String bankName,
            Instant expiryAt,
            BigDecimal expectedAmountNaira
    ) {
    }

    public record AllocationDto(
            Long id,
            Long studentId,
            String studentName,
            Long invoiceId,
            BigDecimal amountNaira,
            boolean applied
    ) {
    }

    public record OrderDto(
            Long id,
            String reference,
            PaymentChannel channel,
            PaymentOrderStatus status,
            BigDecimal totalNaira,
            Long payerGuardianId,
            String note,
            Instant createdAt,
            VirtualAccountDto virtualAccount,
            List<AllocationDto> allocations
    ) {
    }

    public record RecordOfflineRequest(
            @NotNull Long studentId,
            Long invoiceId,
            @NotNull OfflineMethod method,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amountNaira,
            String reference,
            String note
    ) {
    }

    public record TransactionDto(
            Long id,
            Long orderId,
            String providerReference,
            String accountRef,
            BigDecimal amountNaira,
            TransactionStatus status,
            boolean matched,
            Instant paidAt,
            Instant createdAt
    ) {
    }

    /** Payload contract for the Nomba webhook (subset we consume). */
    public record NombaWebhookPayload(
            String event_type,
            Data data
    ) {
        public record Data(Transaction transaction, Account account) {
        }

        public record Transaction(String reference, BigDecimal amount, Instant time) {
        }

        public record Account(String accountRef, String accountNumber) {
        }
    }
}
