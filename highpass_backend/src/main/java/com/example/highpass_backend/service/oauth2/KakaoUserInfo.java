package com.example.highpass_backend.service.oauth2;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object id = attributes.get("id");
        return id == null ? null : String.valueOf(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Object kakaoAccountObj = attributes.get("kakao_account");
        if (!(kakaoAccountObj instanceof Map)) {
            return null;
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoAccountObj;
        Object email = kakaoAccount.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getNickname() {
        Object propertiesObj = attributes.get("properties");
        if (!(propertiesObj instanceof Map)) {
            return null;
        }

        Map<String, Object> properties = (Map<String, Object>) propertiesObj;
        Object nickname = properties.get("nickname");
        return nickname == null ? null : String.valueOf(nickname);
    }
}