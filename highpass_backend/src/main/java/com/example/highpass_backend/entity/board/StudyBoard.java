package com.example.highpass_backend.entity.board;

import com.example.highpass_backend.entity.chat.ChatRoom;
import com.example.highpass_backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StudyBoard {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = true)
    private ChatRoom chatRoom;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, length = 50)
    private String locationName;

    @Column(length = 100, nullable = false)
    private String cert;

    @Column(nullable = false)
    private String placeId;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

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

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {this.likeCount++; }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void updateStudy(String title, String content, String locationName, String address, Double latitude, Double longitude, String placeId, String cert) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (locationName != null) {
            this.locationName = locationName;
        }
        if (address != null) {
            this.address = address;
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
        if (placeId != null) {
            this.placeId = placeId;
        }
        if (cert != null) {
            this.cert = cert;
        }
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
