package com.feebridge.bootstrap;

import com.feebridge.academics.AcademicsService;
import com.feebridge.academics.dto.AcademicsDtos.ClassDto;
import com.feebridge.academics.dto.AcademicsDtos.CreateClassRequest;
import com.feebridge.academics.dto.AcademicsDtos.CreateSessionRequest;
import com.feebridge.academics.dto.AcademicsDtos.SessionDto;
import com.feebridge.auth.AuthService;
import com.feebridge.auth.dto.AuthDtos.AuthResponse;
import com.feebridge.auth.dto.AuthDtos.RegisterSchoolRequest;
import com.feebridge.billing.BillingService;
import com.feebridge.common.domain.ResidencyType;
import com.feebridge.fee.FeeService;
import com.feebridge.fee.dto.FeeDtos.UpsertFeeRequest;
import com.feebridge.parent.ParentService;
import com.feebridge.parent.dto.ParentDtos.CreateLoginRequest;
import com.feebridge.payments.PaymentService;
import com.feebridge.payments.domain.OfflineMethod;
import com.feebridge.payments.dto.PaymentDtos.RecordOfflineRequest;
import com.feebridge.people.StudentService;
import com.feebridge.people.domain.GuardianRelationship;
import com.feebridge.people.dto.PeopleDtos.CreateStudentRequest;
import com.feebridge.people.dto.PeopleDtos.GuardianInput;
import com.feebridge.people.dto.PeopleDtos.MapGuardianRequest;
import com.feebridge.people.dto.PeopleDtos.StudentGuardianDto;
import com.feebridge.scholarship.ScholarshipService;
import com.feebridge.scholarship.domain.ScholarshipType;
import com.feebridge.scholarship.dto.ScholarshipDtos.AwardScholarshipRequest;
import com.feebridge.school.repo.SchoolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Seeds a demo school so the platform is immediately explorable. Runs only when
 * {@code feebridge.demo-data=true} and the database is empty. Prints demo credentials on startup.
 */
@Component
@ConditionalOnProperty(name = "feebridge.demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final SchoolRepository schoolRepository;
    private final AuthService authService;
    private final AcademicsService academics;
    private final FeeService feeService;
    private final StudentService studentService;
    private final ScholarshipService scholarshipService;
    private final BillingService billingService;
    private final PaymentService paymentService;
    private final ParentService parentService;

    public DemoDataSeeder(SchoolRepository schoolRepository, AuthService authService, AcademicsService academics,
                          FeeService feeService, StudentService studentService, ScholarshipService scholarshipService,
                          BillingService billingService, PaymentService paymentService, ParentService parentService) {
        this.schoolRepository = schoolRepository;
        this.authService = authService;
        this.academics = academics;
        this.feeService = feeService;
        this.studentService = studentService;
        this.scholarshipService = scholarshipService;
        this.billingService = billingService;
        this.paymentService = paymentService;
        this.parentService = parentService;
    }

    @Override
    public void run(String... args) {
        if (schoolRepository.count() > 0) {
            return;
        }
        AuthResponse admin = authService.registerSchool(new RegisterSchoolRequest(
                "Greenfield Academy", "greenfield", "info@greenfield.edu.ng", "080-GREEN", "12 Palm Ave, Lagos",
                true, true, "Grace", "Adeyemi", "admin@greenfield.edu.ng", "080-ADMIN", "password123"));
        Long schoolId = admin.user().schoolId();
        Long userId = admin.user().id();

        ClassDto jss1 = academics.createClass(schoolId, new CreateClassRequest("JSS1", 1));
        ClassDto jss2 = academics.createClass(schoolId, new CreateClassRequest("JSS2", 2));
        ClassDto ss1 = academics.createClass(schoolId, new CreateClassRequest("SS1", 4));

        SessionDto session = academics.createSession(schoolId,
                new CreateSessionRequest("2024/2025", null, null, true));
        Long sessionId = session.id();
        Long term1 = session.terms().get(0).id();

        setFees(schoolId, userId, session, jss1, 50_000, 120_000);
        setFees(schoolId, userId, session, jss2, 55_000, 125_000);
        setFees(schoolId, userId, session, ss1, 60_000, 130_000);

        // Students + guardians (siblings reuse the same guardian by phone).
        Long ada = createStudent(schoolId, "Ada", "Obi", jss1.id(), ResidencyType.DAY,
                guardian("John", "Obi", "obi@example.com", "08030000001", GuardianRelationship.FATHER, true, false));
        createStudent(schoolId, "Emeka", "Obi", jss2.id(), ResidencyType.DAY,
                guardian("John", "Obi", "obi@example.com", "08030000001", GuardianRelationship.FATHER, true, false));
        createStudent(schoolId, "Bola", "Ade", ss1.id(), ResidencyType.BOARDING,
                guardian("Funke", "Ade", "ade@example.com", "08030000002", GuardianRelationship.MOTHER, true, false));
        Long chidi = createStudent(schoolId, "Chidi", "Eze", jss1.id(), ResidencyType.BOARDING,
                guardian("Bright Future", "Foundation", "ngo@example.org", "08030000003",
                        GuardianRelationship.NGO, false, true));

        // Chidi is on a 50% scholarship funded by the sponsor NGO.
        scholarshipService.award(schoolId, userId, new AwardScholarshipRequest(
                chidi, null, ScholarshipType.PERCENTAGE, new BigDecimal("50"), null, sessionId, null, "NGO scholarship"));

        billingService.generateInvoices(schoolId, sessionId, term1, userId);

        // Ada makes a partial cash payment.
        paymentService.recordOffline(schoolId, userId, new RecordOfflineRequest(
                ada, null, OfflineMethod.CASH, new BigDecimal("20000"), "RCPT-001", "Part payment"));

        // Provision a parent-portal login for the Obi household.
        StudentGuardianDto obiMapping = studentService.getStudentDetails(schoolId, ada).guardians().get(0);
        parentService.createLogin(schoolId, obiMapping.guardianId(), new CreateLoginRequest(null, "password123"));

        log.info("========================================================");
        log.info(" FeeBridge demo data seeded for 'Greenfield Academy'");
        log.info("   School admin : admin@greenfield.edu.ng / password123");
        log.info("   Parent (Obi) : obi@example.com / password123");
        log.info("========================================================");
    }

    private void setFees(Long schoolId, Long userId, SessionDto session, ClassDto clazz, long day, long boarding) {
        session.terms().forEach(term -> {
            feeService.upsertFee(schoolId, userId, new UpsertFeeRequest(clazz.id(), term.id(),
                    ResidencyType.DAY, BigDecimal.valueOf(day), "Tuition", "Initial fee"));
            feeService.upsertFee(schoolId, userId, new UpsertFeeRequest(clazz.id(), term.id(),
                    ResidencyType.BOARDING, BigDecimal.valueOf(boarding), "Tuition + boarding", "Initial fee"));
        });
    }

    private MapGuardianRequest guardian(String first, String last, String email, String phone,
                                        GuardianRelationship relationship, boolean primary, boolean delegated) {
        return new MapGuardianRequest(new GuardianInput(null, first, last, email, phone, "Lagos"),
                relationship, primary, true, delegated);
    }

    private Long createStudent(Long schoolId, String first, String last, Long classId, ResidencyType residency,
                               MapGuardianRequest guardian) {
        return studentService.createStudent(schoolId, new CreateStudentRequest(
                null, first, last, null, null, null, classId, residency, null, true, guardian)).id();
    }
}
