package com.example.highpass_backend.controller.kakao;

import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.service.kakao.KakaoCalendarApiService;
import com.example.highpass_backend.service.kakao.KakaoCalendarTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class KakaoTokenController {

    private final KakaoCalendarTokenService kakaoCalendarTokenService;
    private final KakaoCalendarApiService kakaoCalendarApiService;

    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> getKakaoToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = kakaoCalendarTokenService.getAccessToken(request, response);
        return ResponseEntity.ok(Map.of("kakaoAccessToken", accessToken));
    }

    @GetMapping("/calendar/events")
    public ResponseEntity<Map<String, Object>> getKakaoCalendarEvents(
            @RequestParam String from,
            @RequestParam String to,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String accessToken = kakaoCalendarTokenService.getAccessToken(request, response);
            return ResponseEntity.ok(kakaoCalendarApiService.getEvents(accessToken, from, to));
        } catch (BusinessException exception) {
            ErrorCode errorCode = exception.getErrorCode();
            if (errorCode != ErrorCode.UNAUTHORIZED && errorCode != ErrorCode.FORBIDDEN) {
                throw exception;
            }

            Map<String, Object> body = new HashMap<>();
            body.put("message", exception.getMessage());
            body.put("connectUrl", kakaoCalendarConnectUrl());
            return ResponseEntity.status(HttpStatus.valueOf(errorCode.getStatus().value())).body(body);
        }
    }

    private String kakaoCalendarConnectUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/oauth2/authorization/kakao-calendar")
                .toUriString();
    }
}
