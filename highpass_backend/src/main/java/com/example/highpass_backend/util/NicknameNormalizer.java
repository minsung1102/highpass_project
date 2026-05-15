package com.example.highpass_backend.util;

public final class NicknameNormalizer {

    private NicknameNormalizer() {
    }

    public static String sanitizeForStorage(String nickname) {
        return nickname == null ? null : nickname.replaceAll("\\s+", "").trim();
    }
}
