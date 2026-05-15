package com.example.highpass_backend.entity.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryAt;

    public void update(String token, LocalDateTime expiryAt) {
        this.token = token;
        this.expiryAt = expiryAt;
    }
}