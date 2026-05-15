package com.example.highpass_backend.service.auth;

import com.example.highpass_backend.dto.auth.LoginResponse;
import com.example.highpass_backend.dto.auth.UserLoginRequest;
import com.example.highpass_backend.dto.auth.UserSignupRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse signup(UserSignupRequest request, HttpServletResponse response);
    LoginResponse login(UserLoginRequest request, HttpServletResponse response);
}
