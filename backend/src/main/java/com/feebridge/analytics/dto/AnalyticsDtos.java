package com.feebridge.analytics.dto;

import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.common.domain.ResidencyType;

import java.math.BigDecimal;
import java.util.List;

public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    public record StatusCountDto(InvoiceStatus status, long count) {
    }

    public record ClassBreakdownDto(Long classId, String className, BigDecimal expectedNaira,
                                    BigDecimal collectedNaira, BigDecimal outstandingNaira, long invoiceCount) {
    }

    public record ResidencyBreakdownDto(ResidencyType residencyType, BigDecimal expectedNaira,
                                        BigDecimal collectedNaira, long invoiceCount) {
    }

    public record DashboardDto(
            long totalStudents,
            long totalClasses,
            long invoiceCount,
            BigDecimal expectedNaira,
            BigDecimal collectedNaira,
            BigDecimal outstandingNaira,
            BigDecimal scholarshipNaira,
            BigDecimal creditOutstandingNaira,
            BigDecimal collectionRatePercent,
            List<StatusCountDto> statusCounts,
            List<ClassBreakdownDto> byClass,
            List<ResidencyBreakdownDto> byResidency
    ) {
    }
}
