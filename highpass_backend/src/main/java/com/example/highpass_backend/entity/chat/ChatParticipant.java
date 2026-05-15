package com.example.highpass_backend.entity.chat;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatParticipant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoom_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Setter
    private boolean isOwner;

    @Setter
    private boolean isOnline;

    private String roomNickname;

    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt;

    @Column
    private LocalDateTime joinedAt;

    public enum ParticipantStatus {
        PENDING, JOINED, REJECTED
    }

    @Enumerated(EnumType.STRING)
    private ParticipantStatus status;

    public void updateLastRead() {
        this.lastReadAt = LocalDateTime.now();
    }


    @PrePersist
    public void prePersist() {
        this.lastReadAt = this.lastReadAt == null ? LocalDateTime.now() : this.lastReadAt;
        this.joinedAt = this.joinedAt == null ? LocalDateTime.now() : this.joinedAt;
        this.status = this.status == null ? ParticipantStatus.JOINED : this.status;
    }
}
