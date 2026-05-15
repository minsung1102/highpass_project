package com.example.highpass_backend.controller.oauth2;

import com.example.highpass_backend.dto.auth.OAuth2SignupRequest;
import com.example.highpass_backend.dto.auth.OAuth2SignupResult;
import com.example.highpass_backend.service.oauth2.OAuth2SignupService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final OAuth2SignupService oauth2SignupService;

    @PostMapping("/signup")
    public ResponseEntity<OAuth2SignupResult> signup(
            @Valid @RequestBody OAuth2SignupRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(oauth2SignupService.signup(request, response));
    }
}
