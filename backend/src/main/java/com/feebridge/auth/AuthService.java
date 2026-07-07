package com.feebridge.auth;

import com.feebridge.auth.domain.Role;
import com.feebridge.auth.domain.RoleNames;
import com.feebridge.auth.domain.User;
import com.feebridge.auth.domain.UserStatus;
import com.feebridge.auth.dto.AuthDtos.AuthResponse;
import com.feebridge.auth.dto.AuthDtos.LoginRequest;
import com.feebridge.auth.dto.AuthDtos.RegisterSchoolRequest;
import com.feebridge.auth.dto.AuthDtos.UserSummary;
import com.feebridge.auth.repo.RoleRepository;
import com.feebridge.auth.repo.UserRepository;
import com.feebridge.auth.security.JwtService;
import com.feebridge.common.exception.ApiException;
import com.feebridge.common.exception.ConflictException;
import com.feebridge.common.exception.NotFoundException;
import com.feebridge.school.domain.School;
import com.feebridge.school.repo.SchoolRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(SchoolRepository schoolRepository, UserRepository userRepository,
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registerSchool(RegisterSchoolRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.adminEmail())) {
            throw new ConflictException("A user with email " + req.adminEmail() + " already exists");
        }

        School school = new School();
        school.setName(req.schoolName());
        school.setCode(uniqueCode(req.schoolCode(), req.schoolName()));
        school.setEmail(req.schoolEmail());
        school.setPhone(req.schoolPhone());
        school.setAddress(req.schoolAddress());
        school.setHasDay(req.hasDay() || !req.hasBoarding()); // default to at least DAY
        school.setHasBoarding(req.hasBoarding());
        school = schoolRepository.save(school);

        Role adminRole = roleRepository.findByName(RoleNames.SCHOOL_ADMIN)
                .orElseThrow(() -> new NotFoundException("Role SCHOOL_ADMIN is not seeded"));

        User admin = new User();
        admin.setSchool(school);
        admin.setEmail(req.adminEmail());
        admin.setPhone(req.adminPhone());
        admin.setFirstName(req.adminFirstName());
        admin.setLastName(req.adminLastName());
        admin.setPasswordHash(passwordEncoder.encode(req.adminPassword()));
        admin.addRole(adminRole);
        admin = userRepository.save(admin);

        return buildResponse(admin);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                        "Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED", "This account is disabled");
        }
        user.setLastLoginAt(Instant.now());
        return buildResponse(user);
    }

    @Transactional(readOnly = true)
    public UserSummary me(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.of("User", userId));
        return toSummary(user);
    }

    private AuthResponse buildResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer",
                jwtService.getTtlMinutes(), toSummary(user));
    }

    private UserSummary toSummary(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();
        String schoolName = user.getSchool() != null ? user.getSchool().getName() : null;
        return new UserSummary(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getSchoolId(), schoolName, roles);
    }

    private String uniqueCode(String requested, String schoolName) {
        String base = (requested != null && !requested.isBlank() ? requested : schoolName)
                .toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "school";
        }
        String candidate = base;
        int suffix = 1;
        while (schoolRepository.existsByCode(candidate)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }
}
