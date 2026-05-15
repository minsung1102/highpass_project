package com.example.highpass_backend.security;

import com.example.highpass_backend.config.AppProperties;
import com.example.highpass_backend.config.CookieUtils;
import com.example.highpass_backend.entity.auth.OAuth2Account;
import com.example.highpass_backend.entity.auth.OAuthProvider;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.auth.OAuth2AccountRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.service.oauth2.OAuth2UserPrincipal;
import com.example.highpass_backend.service.auth.RefreshTokenService;
import com.example.highpass_backend.service.user.UserPresenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppProperties appProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;
    private final UserPresenceService userPresenceService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2UserPrincipal principal = (OAuth2UserPrincipal) authentication.getPrincipal();
        String frontendUrl = appProperties.getFrontendUrl();
        String registrationId = authentication instanceof OAuth2AuthenticationToken oauth2AuthenticationToken
                ? oauth2AuthenticationToken.getAuthorizedClientRegistrationId()
                : "";

        if ("kakao-calendar".equals(registrationId)) {
            handleKakaoCalendarConnection(request, response, authentication, principal, frontendUrl);
            return;
        }

        if (principal.isNew()) {
            String redirectUrl = frontendUrl + "/signup/"
                    + "?email=" + URLEncoder.encode(
                    principal.getEmail() == null ? "" : principal.getEmail(),
                    StandardCharsets.UTF_8
            )
                    + "&provider=" + principal.getProvider().name()
                    + "&providerId=" + URLEncoder.encode(
                    principal.getProviderId(),
                    StandardCharsets.UTF_8
            )
                    + "&nickname=" + URLEncoder.encode(
                    principal.getNickname() == null ? "" : principal.getNickname(),
                    StandardCharsets.UTF_8
            );

            response.sendRedirect(redirectUrl);
            return;
        }

        String accessToken = jwtTokenProvider.createAccessToken(principal.getUserId(), principal.getEmail(), principal.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(principal.getUserId());

        refreshTokenService.save(
                principal.getUserId(),
                refreshToken,
                LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000)
        );

        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, refreshToken);
        userPresenceService.markSeen(principal.getUserId());

        response.sendRedirect(frontendUrl + "/calendar");
    }

    private void handleKakaoCalendarConnection(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication,
            OAuth2UserPrincipal principal,
            String frontendUrl
    ) throws IOException {
        Long currentUserId = extractCurrentUserId(request);
        if (currentUserId == null) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            response.sendRedirect(frontendUrl + "/calendar?kakao_error=login_required");
            return;
        }

        if (principal.isNew()) {
            User user = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

            OAuth2Account account = OAuth2Account.builder()
                    .user(user)
                    .provider(OAuthProvider.KAKAO)
                    .providerId(principal.getProviderId())
                    .build();

            oauth2AccountRepository.save(account);
        } else if (principal.getUserId() != null && !currentUserId.equals(principal.getUserId())) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            response.sendRedirect(frontendUrl + "/calendar?kakao_error=account_mismatch");
            return;
        }

        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient("kakao-calendar", authentication.getName());

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            response.sendRedirect(frontendUrl + "/calendar?kakao_error=token_unavailable");
            return;
        }

        Instant expiresAt = authorizedClient.getAccessToken().getExpiresAt();
        long expiresInSeconds = expiresAt == null
                ? 3600
                : Math.max(1, Duration.between(Instant.now(), expiresAt).getSeconds());

        cookieUtils.addKakaoCalendarAccessTokenCookie(
                response,
                authorizedClient.getAccessToken().getTokenValue(),
                Duration.ofSeconds(expiresInSeconds)
        );
        cookieUtils.addKakaoCalendarAccessTokenExpiryCookie(
                response,
                String.valueOf(expiresAt == null ? System.currentTimeMillis() + 3600_000 : expiresAt.toEpochMilli()),
                Duration.ofDays(14)
        );

        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        if (refreshToken != null) {
            long refreshMaxAge = refreshToken.getExpiresAt() == null
                    ? Duration.ofDays(60).getSeconds()
                    : Math.max(1, Duration.between(Instant.now(), refreshToken.getExpiresAt()).getSeconds());

            cookieUtils.addKakaoCalendarRefreshTokenCookie(
                    response,
                    refreshToken.getTokenValue(),
                    Duration.ofSeconds(refreshMaxAge)
            );
        }

        response.sendRedirect(frontendUrl + "/calendar?kakao_login=1");
    }

    private Long extractCurrentUserId(HttpServletRequest request) {
        String accessToken = cookieUtils.getCookieValue(request, "access_token");
        if (accessToken == null || !jwtTokenProvider.validateToken(accessToken)) {
            return null;
        }
        return jwtTokenProvider.getUserId(accessToken);
    }
}
