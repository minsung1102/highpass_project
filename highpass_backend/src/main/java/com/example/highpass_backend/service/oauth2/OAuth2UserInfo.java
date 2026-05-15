package com.example.highpass_backend.service.oauth2;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
}
