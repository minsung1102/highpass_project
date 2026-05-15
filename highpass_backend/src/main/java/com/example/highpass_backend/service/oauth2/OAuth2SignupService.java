package com.example.highpass_backend.service.oauth2;

import com.example.highpass_backend.dto.auth.OAuth2SignupRequest;
import com.example.highpass_backend.dto.auth.OAuth2SignupResult;
import jakarta.servlet.http.HttpServletResponse;

public interface OAuth2SignupService {
    OAuth2SignupResult signup(OAuth2SignupRequest request, HttpServletResponse response);
}