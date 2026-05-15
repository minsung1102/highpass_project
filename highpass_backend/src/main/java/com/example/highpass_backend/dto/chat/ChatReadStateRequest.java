package com.example.highpass_backend.dto.chat;

import java.util.List;

public record ChatReadStateRequest(
        List<Long> messageIds
) {
}
