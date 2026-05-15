package com.example.highpass_backend.service.oauth2;

import java.util.Map;

public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProviderId() {
        Object sub = attributes.get("sub");
        return sub == null ? null : String.valueOf(sub);
    }

    @Override
    public String getEmail() {
        Object email = attributes.get("email");
        return email == null ? null : String.valueOf(email);
    }

    @Override
    public String getNickname() {
        Object name = attributes.get("name");
        if (name != null) {
            return String.valueOf(name);
        }

        Object givenName = attributes.get("given_name");
        return givenName == null ? null : String.valueOf(givenName);
    }
}