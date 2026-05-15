package com.example.highpass_backend.dto.chat;

import com.example.highpass_backend.entity.chat.ChatParticipant;
import lombok.Getter;

@Getter
public class ChatParticipantResponse {
    private Long userId;
    private String nickname;
    private String avatarVisualClassName;
    private String status;

    public ChatParticipantResponse(ChatParticipant entity) {
        this.userId = entity.getUser() != null ? entity.getUser().getId() : null;
        this.nickname = entity.getUser() != null ? entity.getUser().getNickname() : "알 수 없음";
        this.avatarVisualClassName = entity.getUser() != null ? entity.getUser().getAvatarVisualClassName() : null;
        this.status = entity.getStatus() != null ? entity.getStatus().name() : "JOINED";
    }
}
