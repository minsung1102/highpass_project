package com.example.highpass_backend.dto.admin;

public record AdminReportChatMessageResponse(
        String id,
        String sender,
        String message,
        String createdAt
) {
}
