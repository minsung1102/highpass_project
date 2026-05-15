package com.example.highpass_backend.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDto {

    private Long id;
    private Long roomId;
    private Long senderId;
    private Long receiverId;
    private boolean deleted;
    private String senderName;
    private String senderAvatarVisualClassName;
    private String message;
    private LocalDateTime createdAt;
    private Long unreadCount;
    private List<Long> readers;
    private MessageType type;
    private String roomName;
    private Long newOwnerId;

    public enum MessageType {
        ENTER, TALK, QUIT, JOIN_REQUEST, APPROVE, NOTICE, READ, DELETE
    }

    public void setEnterMessage() {
        this.message = this.senderName + "님이 입장하셨습니다.";
    }
}
