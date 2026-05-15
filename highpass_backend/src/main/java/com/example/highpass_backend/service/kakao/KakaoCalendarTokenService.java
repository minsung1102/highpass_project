package com.example.highpass_backend.service.kakao;

import com.example.highpass_backend.config.CookieUtils;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoCalendarTokenService {

    private final CookieUtils cookieUtils;

    @Value("${KAKAO_REST_API_KEY}")
    private String kakaoRestApiKey;

    public String getAccessToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieUtils.getCookieValue(request, "kakao_calendar_access_token");
        String expiresAt = cookieUtils.getCookieValue(request, "kakao_calendar_access_token_expires_at");

        if (accessToken != null && !isExpired(expiresAt)) {
            return accessToken;
        }

        String refreshToken = cookieUtils.getCookieValue(request, "kakao_calendar_refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "카카오 캘린더 연동이 필요합니다.");
        }

        return refreshAccessToken(refreshToken, response);
    }

    private String refreshAccessToken(String refreshToken, HttpServletResponse response) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("client_id", kakaoRestApiKey);
        form.add("refresh_token", refreshToken);

        try {
            ResponseEntity<Map> tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    Map.class
            );

            Map<String, Object> body = tokenResponse.getBody();
            if (body == null || body.get("access_token") == null) {
                cookieUtils.deleteKakaoCalendarCookies(response);
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "카카오 캘린더 토큰을 갱신할 수 없습니다.");
            }

            String newAccessToken = String.valueOf(body.get("access_token"));
            long expiresIn = parseLong(body.get("expires_in"), 3600L);
            String nextRefreshToken = body.get("refresh_token") == null
                    ? refreshToken
                    : String.valueOf(body.get("refresh_token"));

            cookieUtils.addKakaoCalendarAccessTokenCookie(response, newAccessToken, Duration.ofSeconds(expiresIn));
            cookieUtils.addKakaoCalendarAccessTokenExpiryCookie(
                    response,
                    String.valueOf(Instant.now().plusSeconds(expiresIn).toEpochMilli()),
                    Duration.ofDays(14)
            );
            cookieUtils.addKakaoCalendarRefreshTokenCookie(response, nextRefreshToken, Duration.ofDays(60));

            return newAccessToken;
        } catch (HttpStatusCodeException exception) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "카카오 캘린더 연동이 만료되었습니다.");
        }
    }

    private boolean isExpired(String expiresAt) {
        if (expiresAt == null || expiresAt.isBlank()) {
            return true;
        }

        try {
            return Instant.ofEpochMilli(Long.parseLong(expiresAt)).isBefore(Instant.now().plusSeconds(30));
        } catch (NumberFormatException ignored) {
            return true;
        }
    }

    private long parseLong(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
