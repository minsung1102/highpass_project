package com.example.highpass_backend.repository.auth;

import com.example.highpass_backend.entity.auth.OAuth2Account;
import com.example.highpass_backend.entity.auth.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface OAuth2AccountRepository extends JpaRepository<OAuth2Account, Long> {

    Optional<OAuth2Account> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    boolean existsByProviderAndProviderId(OAuthProvider provider, String providerId);
    List<OAuth2Account> findAllByUserId(Long userId);

}

