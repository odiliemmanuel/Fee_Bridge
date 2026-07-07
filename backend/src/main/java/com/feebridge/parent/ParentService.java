package com.feebridge.parent;

import com.feebridge.auth.domain.Role;
import com.feebridge.auth.domain.RoleNames;
import com.feebridge.auth.domain.User;
import com.feebridge.academics.repo.SchoolClassRepository;
import com.feebridge.auth.repo.RoleRepository;
import com.feebridge.auth.repo.UserRepository;
import com.feebridge.billing.BillingService;
import com.feebridge.billing.dto.BillingDtos.StudentStatementDto;
import com.feebridge.common.exception.BadRequestException;
import com.feebridge.common.exception.ConflictException;
import com.feebridge.common.exception.ForbiddenException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.parent.dto.ParentDtos.ChildDto;
import com.feebridge.parent.dto.ParentDtos.CreateLoginRequest;
import com.feebridge.parent.dto.ParentDtos.GuardianProfileDto;
import com.feebridge.payments.PaymentService;
import com.feebridge.payments.dto.PaymentDtos.CreateOrderRequest;
import com.feebridge.payments.dto.PaymentDtos.OrderDto;
import com.feebridge.people.domain.Guardian;
import com.feebridge.people.domain.Student;
import com.feebridge.people.repo.GuardianRepository;
import com.feebridge.people.repo.StudentGuardianRepository;
import com.feebridge.people.repo.StudentRepository;
import com.feebridge.school.repo.SchoolRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Backs the parent/guardian portal: login provisioning, children, statements and payments. */
@Service
public class ParentService {

    private final GuardianRepository guardianRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final BillingService billingService;
    private final PaymentService paymentService;

    public ParentService(GuardianRepository guardianRepository, StudentGuardianRepository studentGuardianRepository,
                         StudentRepository studentRepository, SchoolClassRepository classRepository,
                         UserRepository userRepository, RoleRepository roleRepository,
                         SchoolRepository schoolRepository, PasswordEncoder passwordEncoder,
                         BillingService billingService, PaymentService paymentService) {
        this.guardianRepository = guardianRepository;
        this.studentGuardianRepository = studentGuardianRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
        this.billingService = billingService;
        this.paymentService = paymentService;
    }

    /** Staff action: provision a portal login for a guardian. */
    @Transactional
    public Long createLogin(Long schoolId, Long guardianId, CreateLoginRequest req) {
        Guardian guardian = guardianRepository.findByIdAndSchoolId(guardianId, schoolId)
                .orElseThrow(() -> NotFoundException.of("Guardian", guardianId));
        String email = (req.email() != null && !req.email().isBlank()) ? req.email() : guardian.getEmail();
        if (email == null || email.isBlank()) {
            throw new BadRequestException("An email is required to create a login");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A user with email " + email + " already exists");
        }
        Role parentRole = roleRepository.findByName(RoleNames.PARENT)
                .orElseThrow(() -> new NotFoundException("Role PARENT is not seeded"));

        User user = new User();
        user.setSchool(schoolRepository.findById(schoolId).orElseThrow());
        user.setEmail(email);
        user.setPhone(guardian.getPhone());
        user.setFirstName(guardian.getFirstName());
        user.setLastName(guardian.getLastName());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.addRole(parentRole);
        user = userRepository.save(user);

        guardian.setUserId(user.getId());
        return user.getId();
    }

    @Transactional(readOnly = true)
    public GuardianProfileDto profile(Long schoolId, Long userId) {
        Guardian g = requireGuardian(schoolId, userId);
        return new GuardianProfileDto(g.getId(), g.fullName(), g.getEmail(), g.getPhone());
    }

    @Transactional(readOnly = true)
    public List<ChildDto> children(Long schoolId, Long userId) {
        Guardian guardian = requireGuardian(schoolId, userId);
        return studentGuardianRepository.findByGuardianId(guardian.getId()).stream()
                .map(sg -> studentRepository.findByIdAndSchoolId(sg.getStudentId(), schoolId).orElse(null))
                .filter(s -> s != null)
                .map(s -> toChildDto(schoolId, s))
                .toList();
    }

    @Transactional(readOnly = true)
    public StudentStatementDto statement(Long schoolId, Long userId, Long studentId) {
        requireOwnedChild(schoolId, userId, studentId);
        return billingService.studentStatement(schoolId, studentId);
    }

    @Transactional
    public OrderDto pay(Long schoolId, Long userId, CreateOrderRequest req) {
        Guardian guardian = requireGuardian(schoolId, userId);
        req.allocations().forEach(a -> requireOwnedChild(schoolId, userId, a.studentId()));
        CreateOrderRequest scoped = new CreateOrderRequest(guardian.getId(), req.note(), req.allocations());
        return paymentService.createOrder(schoolId, userId, scoped);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> myOrders(Long schoolId, Long userId) {
        Guardian guardian = requireGuardian(schoolId, userId);
        return paymentService.ordersForGuardian(guardian.getId());
    }

    // ---- helpers ----

    private Guardian requireGuardian(Long schoolId, Long userId) {
        Guardian g = guardianRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("No guardian profile is linked to this account"));
        if (!g.getSchoolId().equals(schoolId)) {
            throw new ForbiddenException("Guardian does not belong to this school");
        }
        return g;
    }

    private void requireOwnedChild(Long schoolId, Long userId, Long studentId) {
        Guardian guardian = requireGuardian(schoolId, userId);
        boolean owns = studentGuardianRepository.findByStudentIdAndGuardianId(studentId, guardian.getId()).isPresent();
        if (!owns) {
            throw new ForbiddenException("This student is not linked to your account");
        }
    }

    private ChildDto toChildDto(Long schoolId, Student s) {
        StudentStatementDto statement = billingService.studentStatement(schoolId, s.getId());
        String className = s.getClassId() == null ? null :
                classRepository.findById(s.getClassId())
                        .map(com.feebridge.academics.domain.SchoolClass::getName).orElse(null);
        return new ChildDto(s.getId(), s.getAdmissionNo(), s.fullName(), s.getClassId(), className,
                s.getResidencyType(), statement.outstandingBalanceNaira(), statement.creditBalanceNaira());
    }
}
