package com.example.highpass_backend.entity.board;

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
@EntityListeners(AuditingEntityListener.class)  // 생성일 자동화
public class FreeBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @CreatedDate
    @Column(name = "created_at" ,nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.VISIBLE;

    @Column(length = 200)
    private String tags;

    public void updateBoard(String title, String content) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
    }

    public void increaseViewCount() {
        this.viewCount ++ ;
    }

    public void increaseLikeCount() {
        this.likeCount ++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount-- ;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        VISIBLE,
        HIDDEN,
        DELETED
    }

}
