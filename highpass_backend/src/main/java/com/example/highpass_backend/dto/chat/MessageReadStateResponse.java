package com.example.highpass_backend.dto.chat;

import java.util.List;

public record MessageReadStateResponse(
        Long messageId,
        Long unreadCount,
        List<Long> readers
) {
}
