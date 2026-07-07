package com.feebridge.auth;

import com.feebridge.auth.dto.AuthDtos.AuthResponse;
import com.feebridge.auth.dto.AuthDtos.LoginRequest;
import com.feebridge.auth.dto.AuthDtos.RegisterSchoolRequest;
import com.feebridge.auth.dto.AuthDtos.UserSummary;
import com.feebridge.auth.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController


@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-school")
    public AuthResponse registerSchool(@Valid @RequestBody RegisterSchoolRequest request) {
        return authService.registerSchool(request);
    }


    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserSummary me() {
        return authService.me(CurrentUser.userId());
    }
}
