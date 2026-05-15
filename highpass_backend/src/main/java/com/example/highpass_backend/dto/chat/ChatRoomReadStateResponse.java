package com.example.highpass_backend.dto.chat;

import java.util.List;

public record ChatRoomReadStateResponse(
        Long roomId,
        List<MessageReadStateResponse> messages
) {
}
