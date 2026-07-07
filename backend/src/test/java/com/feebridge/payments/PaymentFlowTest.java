package com.feebridge.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feebridge.academics.AcademicsService;
import com.feebridge.academics.dto.AcademicsDtos.ClassDto;
import com.feebridge.academics.dto.AcademicsDtos.CreateClassRequest;
import com.feebridge.academics.dto.AcademicsDtos.CreateSessionRequest;
import com.feebridge.academics.dto.AcademicsDtos.SessionDto;
import com.feebridge.billing.BillingService;
import com.feebridge.billing.domain.Invoice;
import com.feebridge.billing.domain.InvoiceStatus;
import com.feebridge.billing.repo.InvoiceRepository;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.fee.FeeService;
import com.feebridge.fee.dto.FeeDtos.UpsertFeeRequest;
import com.feebridge.nomba.NombaSignature;
import com.feebridge.payments.domain.PaymentOrderStatus;
import com.feebridge.payments.dto.PaymentDtos.AllocationInput;
import com.feebridge.payments.dto.PaymentDtos.CreateOrderRequest;
import com.feebridge.payments.dto.PaymentDtos.NombaWebhookPayload;
import com.feebridge.payments.dto.PaymentDtos.OrderDto;
import com.feebridge.payments.repo.PaymentOrderRepository;
import com.feebridge.payments.repo.PaymentTransactionRepository;
import com.feebridge.people.StudentService;
import com.feebridge.people.dto.PeopleDtos.CreateStudentRequest;
import com.feebridge.school.domain.School;
import com.feebridge.school.repo.SchoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "feebridge.demo-data=false",
        "feebridge.nomba.enabled=false",
        "feebridge.nomba.signature-key=test-sig-key"
})
@AutoConfigureMockMvc
@Transactional
class PaymentFlowTest {

    private static final String SIG_KEY = "test-sig-key";
    private static final Long USER = 1L;

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired SchoolRepository schoolRepository;
    @Autowired AcademicsService academics;
    @Autowired FeeService feeService;
    @Autowired StudentService studentService;
    @Autowired BillingService billing;
    @Autowired PaymentService paymentService;
    @Autowired InvoiceRepository invoiceRepository;
    @Autowired PaymentOrderRepository orderRepository;
    @Autowired PaymentTransactionRepository transactionRepository;

    private Long schoolId;
    private Long sessionId;
    private Long term1;
    private Long classId;
    private Long studentId;

    @BeforeEach
    void setUp() {
        School school = new School();
        school.setName("Nomba Test School");
        school.setCode("nomba-" + System.nanoTime());
        schoolId = schoolRepository.save(school).getId();

        ClassDto jss1 = academics.createClass(schoolId, new CreateClassRequest("JSS1", 1));
        classId = jss1.id();
        SessionDto session = academics.createSession(schoolId, new CreateSessionRequest("2024/2025", null, null, true));
        sessionId = session.id();
        term1 = session.terms().get(0).id();
        feeService.upsertFee(schoolId, USER,
                new UpsertFeeRequest(classId, term1, ResidencyType.DAY, new BigDecimal("50000"), "Fee", null));

        studentId = studentService.createStudent(schoolId, new CreateStudentRequest(
                null, "Ada", "Obi", null, null, null, classId, ResidencyType.DAY, null, true, null)).id();
        billing.generateInvoices(schoolId, sessionId, term1, USER);
    }

    @Test
    void signedWebhookSettlesOrderAndInvoice() throws Exception {
        OrderDto order = createOrder(50_000);
        String body = webhookBody(order.reference(), "NBTX-1", "50000.00");

        postWebhook(body).andExpect(status().isOk());

        Invoice inv = invoiceRepository.findByStudentIdAndTermId(studentId, term1).orElseThrow();
        assertThat(inv.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(inv.getBalanceKobo()).isZero();
        assertThat(orderRepository.findById(order.id()).orElseThrow().getStatus()).isEqualTo(PaymentOrderStatus.PAID);
        assertThat(transactionRepository.existsByProviderAndProviderReference("NOMBA", "NBTX-1")).isTrue();
        assertThat(billing.reconcileStudent(schoolId, studentId).consistent()).isTrue();
    }

    @Test
    void duplicateWebhookIsIdempotent() throws Exception {
        OrderDto order = createOrder(50_000);
        String body = webhookBody(order.reference(), "NBTX-DUP", "50000.00");

        postWebhook(body).andExpect(status().isOk());
        postWebhook(body).andExpect(status().isOk()); // replay

        Invoice inv = invoiceRepository.findByStudentIdAndTermId(studentId, term1).orElseThrow();
        assertThat(inv.getAmountPaidKobo()).isEqualTo(5_000_000L); // not doubled
    }

    @Test
    void invalidSignatureIsRejected() throws Exception {
        OrderDto order = createOrder(50_000);
        String body = webhookBody(order.reference(), "NBTX-BAD", "50000.00");

        mockMvc.perform(post("/api/webhooks/nomba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header("x-nomba-signature", "deadbeef"))
                .andExpect(status().isUnauthorized());

        assertThat(transactionRepository.existsByProviderAndProviderReference("NOMBA", "NBTX-BAD")).isFalse();
    }

    // ---- helpers ----

    private OrderDto createOrder(long naira) {
        return paymentService.createOrder(schoolId, USER, new CreateOrderRequest(
                null, "Test order", List.of(new AllocationInput(studentId, null, new BigDecimal(naira)))));
    }

    private String webhookBody(String orderRef, String txRef, String amount) throws Exception {
        NombaWebhookPayload payload = new NombaWebhookPayload("payment_success",
                new NombaWebhookPayload.Data(
                        new NombaWebhookPayload.Transaction(txRef, new BigDecimal(amount), Instant.now()),
                        new NombaWebhookPayload.Account(orderRef, "9012345678")));
        return objectMapper.writeValueAsString(payload);
    }



    private org.springframework.test.web.servlet.ResultActions postWebhook(String body) throws Exception {
        String signature = NombaSignature.hmacSha256Hex(body, SIG_KEY);
        return mockMvc.perform(post("/api/webhooks/nomba")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header("x-nomba-signature", signature));
    }
}
