package com.example.highpass_backend.service.user;

import com.example.highpass_backend.entity.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
public class CustomUserDetails implements OAuth2User {

    @Getter
    private boolean isNew;

    // 기존 유저
    public CustomUserDetails(User user, boolean isNew) {
        this.isNew = isNew;
    }

    // 신규 유저
    public CustomUserDetails(String provider, String providerId, boolean isNew) {
        this.isNew = isNew;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return "";
    }

    public String getProvider() {
        return getProvider();
    }

    public String getProviderId() {
        return getProviderId();
    }
}