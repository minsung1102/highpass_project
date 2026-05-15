package com.example.highpass_backend.entity.notification;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient; // 알림 수신자

    private String senderNickname; // 알림 발신자 닉네임 (또는 시스템)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type; // COMMENT, LIKE, CALENDAR

    private String message; // 알림 메시지

    private Long targetId; // 이동할 게시글 ID 등

    private String targetType; // 이동할 대상의 종류 (FREE, STUDY 등)

    private String content; // 알림 상세 내용 (댓글 내용 등)

    private boolean isRead; // 읽음 여부

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
