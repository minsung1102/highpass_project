package com.example.highpass_backend.dto.admin;

import java.time.LocalDateTime;

public record AdminPostResponse(
        String id,
        String type,
        String title,
        String content,
        String authorId,
        String author,
        String status,
        LocalDateTime createdAt,
        int views,
        int comments,
        int reports
) {
}
