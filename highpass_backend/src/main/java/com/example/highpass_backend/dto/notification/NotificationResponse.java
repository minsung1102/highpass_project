package com.example.highpass_backend.dto.notification;

import com.example.highpass_backend.entity.notification.Notification;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String senderNickname;
    private NotificationType type;
    private String message;
    private Long targetId;
    private String targetType;
    private String content;
    @JsonProperty("isRead")
    private boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .senderNickname(notification.getSenderNickname())
                .type(notification.getType())
                .message(notification.getMessage())
                .targetId(notification.getTargetId())
                .targetType(notification.getTargetType())
                .content(notification.getContent())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
