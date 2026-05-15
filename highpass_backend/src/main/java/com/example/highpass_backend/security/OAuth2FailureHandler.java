package com.example.highpass_backend.security;

import com.example.highpass_backend.config.AppProperties;
import com.example.highpass_backend.config.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AppProperties appProperties;
    private final CookieUtils cookieUtils;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String registrationId = extractRegistrationId(request);
        String errorCode = "unknown";
        String description = exception.getMessage();

        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            OAuth2Error error = oauth2Exception.getError();
            if (error != null) {
                if (error.getErrorCode() != null) errorCode = error.getErrorCode();
                if (error.getDescription() != null && !error.getDescription().isBlank()) {
                    description = error.getDescription();
                }
            }
        }

        log.error("[OAuth2 failure] registrationId={}, errorCode={}, description={}", registrationId, errorCode, description, exception);

        String frontendUrl = appProperties.getFrontendUrl();
        if (frontendUrl == null || frontendUrl.isBlank()) {
            frontendUrl = "http://localhost:3000";
        }

        String safeDescription = description == null ? "" : description;
        String redirectUrl;
        if ("kakao-calendar".equals(registrationId)) {
            cookieUtils.deleteKakaoCalendarCookies(response);
            redirectUrl = frontendUrl + "/calendar?kakao_error=" + encode(errorCode)
                    + "&kakao_error_description=" + encode(safeDescription);
        } else {
            redirectUrl = frontendUrl + "/login?oauth_error=" + encode(errorCode)
                    + "&oauth_error_description=" + encode(safeDescription);
        }

        response.sendRedirect(redirectUrl);
    }

    private String extractRegistrationId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) return "";
        int idx = uri.lastIndexOf('/');
        return idx >= 0 && idx < uri.length() - 1 ? uri.substring(idx + 1) : "";
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
