package com.example.highpass_backend.service.oauth2;

import com.example.highpass_backend.entity.auth.OAuthProvider;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class OAuth2UserPrincipal implements OAuth2User {

    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private OAuthProvider provider;
    private String providerId;
    private boolean isNew;
    private Map<String, Object> attributes;


    public String getName() {
        return userId != null ? String.valueOf(userId) : provider + "_" + providerId;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}