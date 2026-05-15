package com.example.highpass_backend.dto.report;

import com.example.highpass_backend.entity.report.Report;

import java.time.LocalDateTime;

public record ReportResponse(
        String id,
        String targetType,
        String targetId,
        String targetLabel,
        String reasonCode,
        String reason,
        String status,
        LocalDateTime createdAt,
        String adminResponse,
        LocalDateTime respondedAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                String.valueOf(report.getId()),
                report.getTargetType().name().toLowerCase(),
                report.getTargetId(),
                report.getTargetLabel(),
                report.getReasonCode(),
                report.getReason(),
                report.getStatus().name().toLowerCase(),
                report.getCreatedAt(),
                report.getAdminResponse(),
                report.getRespondedAt()
        );
    }
}
