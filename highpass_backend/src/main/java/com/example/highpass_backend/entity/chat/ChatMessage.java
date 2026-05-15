package com.example.highpass_backend.entity.chat;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message")
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = true)
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(nullable = true)
    private Boolean deleted;

    public enum MessageType {
        TALK, ENTER, QUIT, NOTICE
    }

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String message, MessageType type) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.type = type;
    }

    @PrePersist
    public void prePersist() {
        this.type = this.type == null ? MessageType.TALK : this.type;
        this.deleted = this.deleted == null ? false : this.deleted;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

    public void markAsDeleted() {
        this.deleted = true;
    }
}
