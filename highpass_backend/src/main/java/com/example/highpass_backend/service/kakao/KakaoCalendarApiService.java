package com.example.highpass_backend.service.kakao;

import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class KakaoCalendarApiService {
    private static final String KAKAO_CALENDAR_EVENTS_URL = "https://kapi.kakao.com/v2/api/calendar/events";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getEvents(String accessToken, String from, String to) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String url = UriComponentsBuilder.fromUriString(KAKAO_CALENDAR_EVENTS_URL)
                .queryParam("from", from)
                .queryParam("to", to)
                .build()
                .toUriString();

        try {
            ResponseEntity<String> response = new RestTemplate().exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );

            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return Map.of("events", List.of());
            }

            return objectMapper.readValue(body, new TypeReference<>() {});
        } catch (HttpStatusCodeException exception) {
            ErrorCode errorCode = switch (exception.getStatusCode().value()) {
                case 401 -> ErrorCode.UNAUTHORIZED;
                case 403 -> ErrorCode.FORBIDDEN;
                default -> ErrorCode.EXTERNAL_SERVICE_ERROR;
            };

            throw new BusinessException(
                    errorCode,
                    extractKakaoErrorMessage(exception)
            );
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.EXTERNAL_SERVICE_ERROR, "카카오 일정을 불러오지 못했습니다.");
        }
    }

    private String extractKakaoErrorMessage(HttpStatusCodeException exception) {
        try {
            Map<String, Object> body = objectMapper.readValue(exception.getResponseBodyAsString(), new TypeReference<>() {});
            Object message = body.get("msg") == null ? body.get("message") : body.get("msg");
            return message == null ? "카카오 일정을 불러오지 못했습니다." : String.valueOf(message);
        } catch (Exception ignored) {
            return "카카오 일정을 불러오지 못했습니다.";
        }
    }
}
