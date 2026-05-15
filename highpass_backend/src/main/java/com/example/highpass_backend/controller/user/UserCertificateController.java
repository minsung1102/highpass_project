package com.example.highpass_backend.controller.user;

import com.example.highpass_backend.dto.user.UserCertificateRequest;
import com.example.highpass_backend.dto.user.UserCertificateResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.user.UserCertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user-certificates")
@RequiredArgsConstructor
public class UserCertificateController {

    private final UserCertificateService userCertificateService;

    @PostMapping
    public ResponseEntity<UserCertificateResponse> addUserCertificate(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody UserCertificateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userCertificateService.addUserCertificate(principal.getUserId(), request));
    }

    @GetMapping
    public ResponseEntity<List<UserCertificateResponse>> getUserCertificates(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(userCertificateService.getUserCertificates(principal.getUserId()));
    }

    @DeleteMapping("/{userCertificateId}")
    public ResponseEntity<Void> deleteUserCertificate(
            @PathVariable Long userCertificateId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        userCertificateService.deleteUserCertificate(principal.getUserId(), userCertificateId);
        return ResponseEntity.noContent().build();
    }
}
