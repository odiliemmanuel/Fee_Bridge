package com.feebridge.analytics;

import com.feebridge.academics.domain.SchoolClass;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.analytics.dto.AnalyticsDtos.ClassBreakdownDto;
import com.feebridge.analytics.dto.AnalyticsDtos.DashboardDto;
import com.feebridge.analytics.dto.AnalyticsDtos.ResidencyBreakdownDto;
import com.feebridge.analytics.dto.AnalyticsDtos.StatusCountDto;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.billing.repo.StudentCreditRepository;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.common.money.Money;
import com.feebridge.people.repo.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Aggregates fee data into the analytics the school dashboard displays. */
@Service
public class AnalyticsService {

    private final InvoiceRepository invoiceRepository;
    private final StudentCreditRepository creditRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;

    public AnalyticsService(InvoiceRepository invoiceRepository, StudentCreditRepository creditRepository,
                            StudentRepository studentRepository, SchoolClassRepository classRepository) {
        this.invoiceRepository = invoiceRepository;
        this.creditRepository = creditRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
    }

    @Transactional(readOnly = true)
    public DashboardDto dashboard(Long schoolId, Long sessionId, Long termId) {
        List<Invoice> invoices = invoiceRepository.filterList(schoolId, sessionId, termId, null, null);

        long expected = invoices.stream().mapToLong(Invoice::getNetAmountKobo).sum();
        long collected = invoices.stream().mapToLong(Invoice::getAmountPaidKobo).sum();
        long outstanding = invoices.stream().mapToLong(Invoice::getBalanceKobo).sum();
        long scholarship = invoices.stream().mapToLong(Invoice::getScholarshipAmountKobo).sum();
        long credit = creditRepository.totalCreditForSchool(schoolId);

        BigDecimal rate = expected == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(collected).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(expected), 2, RoundingMode.HALF_UP);

        List<StatusCountDto> statusCounts = countByStatus(invoices);
        List<ClassBreakdownDto> byClass = byClass(schoolId, invoices);
        List<ResidencyBreakdownDto> byResidency = byResidency(invoices);

        return new DashboardDto(
                studentRepository.countBySchoolId(schoolId),
                classRepository.countBySchoolId(schoolId),
                invoices.size(),
                Money.ofKobo(expected).toNaira(),
                Money.ofKobo(collected).toNaira(),
                Money.ofKobo(outstanding).toNaira(),
                Money.ofKobo(scholarship).toNaira(),
                Money.ofKobo(credit).toNaira(),
                rate,
                statusCounts, byClass, byResidency);
    }

    private List<StatusCountDto> countByStatus(List<Invoice> invoices) {
        Map<InvoiceStatus, Long> counts = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getStatus, Collectors.counting()));
        return List.of(InvoiceStatus.values()).stream()
                .map(s -> new StatusCountDto(s, counts.getOrDefault(s, 0L)))
                .toList();
    }

    private List<ClassBreakdownDto> byClass(Long schoolId, List<Invoice> invoices) {
        Map<Long, String> names = classRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId).stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));
        Map<Long, List<Invoice>> grouped = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getClassId));
        return grouped.entrySet().stream().map(e -> {
            List<Invoice> group = e.getValue();
            return new ClassBreakdownDto(e.getKey(), names.get(e.getKey()),
                    kobo(group.stream().mapToLong(Invoice::getNetAmountKobo).sum()),
                    kobo(group.stream().mapToLong(Invoice::getAmountPaidKobo).sum()),
                    kobo(group.stream().mapToLong(Invoice::getBalanceKobo).sum()),
                    group.size());
        }).toList();
    }

    private List<ResidencyBreakdownDto> byResidency(List<Invoice> invoices) {
        Map<ResidencyType, List<Invoice>> grouped = invoices.stream()
                .collect(Collectors.groupingBy(Invoice::getResidencyType));
        return grouped.entrySet().stream().map(e -> new ResidencyBreakdownDto(e.getKey(),
                kobo(e.getValue().stream().mapToLong(Invoice::getNetAmountKobo).sum()),
                kobo(e.getValue().stream().mapToLong(Invoice::getAmountPaidKobo).sum()),
                e.getValue().size())).toList();
    }

    private BigDecimal kobo(long value) {
        return Money.ofKobo(value).toNaira();
    }
}
