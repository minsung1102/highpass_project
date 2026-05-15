package com.example.highpass_backend.entity.user;

import jakarta.persistence.*;
import lombok.*;
import com.example.highpass_backend.util.NicknameNormalizer;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_email", columnNames = "email")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;          // 소셜도 저장, nullable 가능 여부는 정책에 따라

    private String password;       // 일반 회원만 저장, 소셜은 null

    @Column(nullable = false, length = 50, unique = true)
    private String nickname;

    @Column(name = "age_range", length = 20)
    private String ageRange;

    @Column(length = 20)
    private String gender;

    @Column(length = 50)
    private String siDo;

    @Column(length = 50)
    private String gunGu;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.ACTIVE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "avatar_visual_class_name", length = 120)
    private String avatarVisualClassName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.nickname = NicknameNormalizer.sanitizeForStorage(this.nickname);
//        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.nickname = NicknameNormalizer.sanitizeForStorage(this.nickname);
    }

    // 알림 설정
    @Builder.Default // 🚨 빌더로 생성할 때도 true가 기본값이 되도록 보장합니다!
    @Column(nullable = false)
    private boolean isCommentNotiOn = true;

    @Builder.Default // 🚨 여기도 추가!
    @Column(nullable = false)
    private boolean isLikeNotiOn = true;

    public void toggleCommentNoti(boolean on) {
        this.isCommentNotiOn = on;
    }

    public void toggleLikeNoti(boolean on) {
        this.isLikeNotiOn = on;
    }

    public void encodePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String nickname, String ageRange, String gender, String siDo, String gunGu) {
        this.nickname = NicknameNormalizer.sanitizeForStorage(nickname);
        this.ageRange = ageRange;
        this.gender = gender;
        this.siDo = siDo;
        this.gunGu = gunGu;
    }

    public void updateAvatarVisualClassName(String avatarVisualClassName) {
        this.avatarVisualClassName = avatarVisualClassName;
    }

    public void updateStatus(Status status) {
        this.status = status;
        if (status == Status.DELETED) {
            this.deletedAt = LocalDateTime.now();
        } else if (status == Status.ACTIVE) {
            this.deletedAt = null;
        }
    }

    public void updateRole(Role role) {
        this.role = role;
    }

    public void markSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }

    public enum Status {
        ACTIVE,
        SUSPENDED,
        DELETED
    }

    public enum Role {
        USER,
        ADMIN
    }
}
