package com.example.highpass_backend.repository.auth;

import com.example.highpass_backend.entity.auth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
}
