package com.feebridge.billing;

import com.feebridge.academics.AcademicsService;
import com.feebridge.academics.dto.AcademicsDtos.ClassDto;
import com.feebridge.academics.dto.AcademicsDtos.CreateClassRequest;
import com.feebridge.academics.dto.AcademicsDtos.CreateSessionRequest;
import com.feebridge.academics.dto.AcademicsDtos.SessionDto;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.billing.repo.StudentCreditRepository;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.common.money.Money;
import com.feebridge.fee.FeeService;
import com.feebridge.fee.dto.FeeDtos.UpsertFeeRequest;
import com.feebridge.people.StudentService;
import com.feebridge.people.dto.PeopleDtos.CreateStudentRequest;
import com.feebridge.scholarship.ScholarshipService;
import com.feebridge.scholarship.domain.ScholarshipType;
import com.feebridge.scholarship.dto.ScholarshipDtos.AwardScholarshipRequest;
import com.feebridge.school.domain.School;
import com.feebridge.school.repo.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "feebridge.demo-data=false")
@Transactional
class BillingEngineTest {

    @Autowired SchoolRepository schoolRepository;
    @Autowired AcademicsService academics;
    @Autowired FeeService feeService;
    @Autowired StudentService studentService;
    @Autowired ScholarshipService scholarshipService;
    @Autowired BillingService billing;
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired StudentCreditRepository creditRepository;

    private static final Long USER = 1L;

    private Long schoolId;
    private Long sessionId;
    private Long term1;
    private Long term2;
    private Long classId;

    @BeforeEach
    void setUp() {
        School school = new School();
        school.setName("Test School");
        school.setCode("test-" + System.nanoTime());
        schoolId = schoolRepository.save(school).getId();

        ClassDto jss1 = academics.createClass(schoolId, new CreateClassRequest("JSS1", 1));
        classId = jss1.id();

        SessionDto session = academics.createSession(schoolId,
                new CreateSessionRequest("2024/2025", null, null, true));
        sessionId = session.id();
        term1 = session.terms().get(0).id();
        term2 = session.terms().get(1).id();

        upsertFee(term1, "50000");
        upsertFee(term2, "50000");
    }

    @Test
    void fullPaymentSettlesInvoiceAndReconciles() {
        Long studentId = newStudent();
        billing.generateInvoices(schoolId, sessionId, term1, USER);
        Invoice inv = invoice(studentId, term1);
        assertThat(inv.getNetAmountKobo()).isEqualTo(5_000_000L);

        billing.applyPayment(schoolId, inv.getId(), Money.ofNaira(50_000), true, "ref-1", USER);

        Invoice after = invoiceRepository.findById(inv.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(after.getBalanceKobo()).isZero();
        assertThat(after.getAmountPaidKobo()).isEqualTo(5_000_000L);
        assertThat(billing.reconcileStudent(schoolId, studentId).consistent()).isTrue();
    }

    @Test
    void partialPaymentLeavesBalanceAndPartialStatus() {
        Long studentId = newStudent();
        billing.generateInvoices(schoolId, sessionId, term1, USER);
        Invoice inv = invoice(studentId, term1);

        billing.applyPayment(schoolId, inv.getId(), Money.ofNaira(20_000), true, "ref-2", USER);

        Invoice after = invoiceRepository.findById(inv.getId()).orElseThrow();
        assertThat(after.getStatus()).isEqualTo(InvoiceStatus.PARTIAL);
        assertThat(after.getBalanceKobo()).isEqualTo(3_000_000L);
        assertThat(billing.reconcileStudent(schoolId, studentId).consistent()).isTrue();
    }

    @Test
    void overpaymentCascadesToNextTermInvoice() {
        Long studentId = newStudent();
        billing.generateInvoices(schoolId, sessionId, term1, USER);
        billing.generateInvoices(schoolId, sessionId, term2, USER);
        Invoice inv1 = invoice(studentId, term1);

        // Pay 70,000 against a 50,000 term-1 bill -> 20,000 surplus carried to term 2.
        billing.applyPayment(schoolId, inv1.getId(), Money.ofNaira(70_000), true, "ref-3", USER);

        Invoice after1 = invoiceRepository.findById(inv1.getId()).orElseThrow();
        Invoice after2 = invoice(studentId, term2);
        assertThat(after1.getStatus()).isEqualTo(InvoiceStatus.OVERPAID);
        assertThat(after1.getBalanceKobo()).isZero();
        assertThat(after2.getCreditAppliedKobo()).isEqualTo(2_000_000L);
        assertThat(after2.getBalanceKobo()).isEqualTo(3_000_000L);
        assertThat(walletKobo(studentId)).isZero();
        assertThat(billing.reconcileStudent(schoolId, studentId).consistent()).isTrue();
    }

    @Test
    void overpaymentCreditCarriesInWhenNextTermGeneratedLater() {
        Long studentId = newStudent();
        billing.generateInvoices(schoolId, sessionId, term1, USER);
        Invoice inv1 = invoice(studentId, term1);

        billing.applyPayment(schoolId, inv1.getId(), Money.ofNaira(70_000), true, "ref-4", USER);
        assertThat(walletKobo(studentId)).isEqualTo(2_000_000L); // surplus parked until next term exists

        billing.generateInvoices(schoolId, sessionId, term2, USER);
        Invoice inv2 = invoice(studentId, term2);
        assertThat(inv2.getCreditAppliedKobo()).isEqualTo(2_000_000L);
        assertThat(inv2.getBalanceKobo()).isEqualTo(3_000_000L);
        assertThat(walletKobo(studentId)).isZero();
    }

    @Test
    void percentageScholarshipReducesNetAmount() {
        Long studentId = newStudent();
        scholarshipService.award(schoolId, USER, new AwardScholarshipRequest(
                studentId, null, ScholarshipType.PERCENTAGE, new BigDecimal("25"), null, null, null, "Merit"));

        billing.generateInvoices(schoolId, sessionId, term1, USER);
        Invoice inv = invoice(studentId, term1);

        assertThat(inv.getScholarshipAmountKobo()).isEqualTo(1_250_000L); // 25% of 50,000
        assertThat(inv.getNetAmountKobo()).isEqualTo(3_750_000L);
    }

    // ---- helpers ----

    private void upsertFee(Long termId, String naira) {
        feeService.upsertFee(schoolId, USER,
                new UpsertFeeRequest(classId, termId, ResidencyType.DAY, new BigDecimal(naira), "Fee", null));
    }

    private Long newStudent() {
        return studentService.createStudent(schoolId, new CreateStudentRequest(
                null, "Ada", "Obi", null, null, null, classId, ResidencyType.DAY, null, true, null)).id();
    }

    private Invoice invoice(Long studentId, Long termId) {
        return invoiceRepository.findByStudentIdAndTermId(studentId, termId).orElseThrow();
    }

    private long walletKobo(Long studentId) {
        return creditRepository.findByStudentId(studentId).orElseThrow().getBalanceKobo();
    }
}
