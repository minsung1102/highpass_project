package com.example.highpass_backend.controller.calendar;

import com.example.highpass_backend.dto.calendar.HolidayDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; // ⭐️ XmlMapper 대신 ObjectMapper 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/calendar")
@CrossOrigin(origins = "http://localhost:3000")
public class HolidayController {

    @Value("${HOLIDAY_API_KEY}")
    private String serviceKey;

    private final Map<Integer, List<HolidayDto>> cache = new ConcurrentHashMap<>();

    @GetMapping("/holidays/{year}")
    public ResponseEntity<List<HolidayDto>> getHolidays(@PathVariable int year) {
        try {
            if (cache.containsKey(year)) {
                return ResponseEntity.ok(cache.get(year));
            }

            URI uri = UriComponentsBuilder
                    .fromUriString("http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("solYear", year)
                    .queryParam("numOfRows", 100)
                    .queryParam("_type", "json") // ⭐️ 확실하게 JSON 형식만 달라고 쐐기를 박습니다.
                    .encode()
                    .build()
                    .toUri();

            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(uri, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<HolidayDto> holidayList = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    holidayList.add(convertNodeToDto(item));
                }
            } else if (!items.isMissingNode()) {
                holidayList.add(convertNodeToDto(items));
            }

            if (holidayList.stream().noneMatch(h -> h.getDate().endsWith("-05-01"))) {
                holidayList.add(new HolidayDto(year + "-05-01", "근로자의 날"));
            }

            holidayList.sort((a, b) -> a.getDate().compareTo(b.getDate()));

            cache.put(year, holidayList);
            return ResponseEntity.ok(holidayList);

        } catch (Exception e) {
            System.err.println("실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private HolidayDto convertNodeToDto(JsonNode item) {
        String locdate = item.path("locdate").asText();
        String dateName = item.path("dateName").asText();

        // "20260505" -> "2026-05-05" 포맷팅
        String formattedDate = locdate.substring(0, 4) + "-" + locdate.substring(4, 6) + "-" + locdate.substring(6, 8);
        return new HolidayDto(formattedDate, dateName);
    }
}