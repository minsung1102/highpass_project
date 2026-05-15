package com.example.highpass_backend.service.oauth2;

import com.example.highpass_backend.entity.auth.OAuthProvider;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class OAuth2Attributes {
    private String email;
    private String nickname;
    private OAuthProvider provider;
    private String providerId;
    private Map<String, Object> attributes;
}