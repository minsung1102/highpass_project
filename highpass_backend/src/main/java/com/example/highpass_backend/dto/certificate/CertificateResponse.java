package com.example.highpass_backend.dto.certificate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateResponse {
    private Long id;
    private String name;
}