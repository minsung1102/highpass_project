package com.example.highpass_backend.entity.board;

import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(                         // 좋아요 중복 방지
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_user_like",
            columnNames = {"user_id", "target_id", "target_type"}
        )
    }
)
public class BoardLike {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    public enum TargetType {
        STUDY,
        FREE
    }
}