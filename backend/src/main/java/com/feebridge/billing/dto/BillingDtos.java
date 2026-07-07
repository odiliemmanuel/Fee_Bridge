package com.feebridge.billing.dto;

import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.domain.LedgerEntryType;
import com.feebridge.common.domain.ResidencyType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class BillingDtos {

    private BillingDtos() {
    }

    public record GenerateInvoicesRequest(
            @NotNull Long sessionId,
            @NotNull Long termId
    ) {
    }

    public record AssessmentResultDto(
            int invoicesCreated,
            int skipped,
            BigDecimal totalBilledNaira
    ) {
    }

    public record InvoiceDto(
            Long id,
            Long studentId,
            String studentName,
            String admissionNo,
            Long classId,
            String className,
            Long sessionId,
            Long termId,
            ResidencyType residencyType,
            BigDecimal grossNaira,
            BigDecimal scholarshipNaira,
            BigDecimal creditAppliedNaira,
            BigDecimal netNaira,
            BigDecimal amountPaidNaira,
            BigDecimal balanceNaira,
            InvoiceStatus status
    ) {
    }

    public record LedgerEntryDto(
            Long id,
            Long invoiceId,
            Long termId,
            LedgerEntryType entryType,
            BigDecimal amountNaira,
            String reference,
            String description,
            Instant createdAt
    ) {
    }

    public record StudentStatementDto(
            Long studentId,
            String studentName,
            BigDecimal creditBalanceNaira,
            BigDecimal outstandingBalanceNaira,
            List<InvoiceDto> invoices,
            List<LedgerEntryDto> ledger
    ) {
    }

    public record ReconcileResultDto(
            Long studentId,
            BigDecimal ledgerBalanceNaira,
            BigDecimal invoiceBalanceSumNaira,
            boolean consistent
    ) {
    }
}
