package com.example.highpass_backend.dto.notification;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatNotificationDto {
    private Long id;
    private String senderNickname;
    private String type;
    private String message;
    private String content;
    private Long targetId;
    private String targetType;
    private String createdAt;
    private boolean isRead;
}

