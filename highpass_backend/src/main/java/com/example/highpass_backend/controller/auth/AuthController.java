package com.example.highpass_backend.controller.auth;

import com.example.highpass_backend.dto.auth.LoginResponse;
import com.example.highpass_backend.dto.auth.UserLoginRequest;
import com.example.highpass_backend.dto.auth.UserSignupRequest;
import com.example.highpass_backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(
            @Valid @RequestBody UserSignupRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.signup(request, response));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }
}
