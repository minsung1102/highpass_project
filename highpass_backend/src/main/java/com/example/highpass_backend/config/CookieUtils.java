package com.example.highpass_backend.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtils {
    private final AppProperties appProperties;

    public void addAccessTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addKakaoCalendarAccessTokenCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("kakao_calendar_access_token", token)
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addKakaoCalendarRefreshTokenCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("kakao_calendar_refresh_token", token)
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addKakaoCalendarAccessTokenExpiryCookie(HttpServletResponse response, String expiresAt, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from("kakao_calendar_access_token_expires_at", expiresAt)
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void deleteKakaoCalendarCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie("kakao_calendar_access_token"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie("kakao_calendar_refresh_token"));
        response.addHeader(HttpHeaders.SET_COOKIE, buildExpiredCookie("kakao_calendar_access_token_expires_at"));
    }

    public String getCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private String buildExpiredCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(appProperties.isSecureCookie())
                .sameSite(appProperties.isSecureCookie() ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString();
    }
}
