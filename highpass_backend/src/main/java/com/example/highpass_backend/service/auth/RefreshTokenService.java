package com.example.highpass_backend.service.auth;

import com.example.highpass_backend.entity.auth.RefreshToken;
import com.example.highpass_backend.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void save(Long userId, String token, LocalDateTime expiryAt) {
        refreshTokenRepository.findById(userId)
                .ifPresentOrElse(
                        entity -> entity.update(token, expiryAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userId(userId)
                                        .token(token)
                                        .expiryAt(expiryAt)
                                        .build()
                        )
                );
    }

    @Transactional(readOnly = true)
    public RefreshToken getByUserId(Long userId) {
        return refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("리프레시 토큰이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public RefreshToken findByUserId(Long userId) {
        return refreshTokenRepository.findById(userId).orElse(null);
    }

    public void delete(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }
}
