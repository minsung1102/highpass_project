package com.example.highpass_backend.dto.certificate;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificateSyncResponse {
    private int fetchedCount;
    private int createdCount;
    private int updatedCount;
    private int totalCount;
    private String message;
}
