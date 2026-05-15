package com.example.highpass_backend.entity.report;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Column(name = "target_label", nullable = false, length = 120)
    private String targetLabel;

    @Column(name = "reason_code", nullable = false, length = 40)
    private String reasonCode;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(name = "admin_response", length = 2000)
    private String adminResponse;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void respond(String message) {
        this.adminResponse = message;
        this.respondedAt = LocalDateTime.now();
    }

    public enum TargetType {
        USER,
        POST,
        COMMENT,
        CHAT,
        INQUIRY
    }

    public enum Status {
        PENDING,
        RESOLVED,
        DISMISSED
    }
}
