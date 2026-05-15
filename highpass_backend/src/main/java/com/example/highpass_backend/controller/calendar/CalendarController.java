package com.example.highpass_backend.controller.calendar;

import com.example.highpass_backend.dto.calendar.CalendarRequest;
import com.example.highpass_backend.dto.calendar.CalendarResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.calendar.CalendarService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @PostMapping
    public ResponseEntity<CalendarResponse> create(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody CalendarRequest request
    ) {
        CalendarResponse calendarResponse = calendarService.createCalendar(principal.getUserId(), request);
        return new ResponseEntity<>(calendarResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CalendarResponse>> getCalendar(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(calendarService.getCalendarList(principal.getUserId()));
    }

    @PatchMapping("/{calendarId}/content")
    public ResponseEntity<CalendarResponse> updateCalendar(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody CalendarRequest request
    ) {
        CalendarResponse updatedCalendar = calendarService.updateCalendar(principal.getUserId(), calendarId, request);
        return ResponseEntity.ok(updatedCalendar);
    }

    @DeleteMapping("/{calendarId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long calendarId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        calendarService.deleteCalendar(principal.getUserId(), calendarId);
        return ResponseEntity.ok().build();
    }

    // 알람
    // 알람 조회
    @GetMapping("/alarms")
    public ResponseEntity<List<CalendarResponse>> getTodayAlarms(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        List<CalendarResponse> alarms = calendarService.getTodayAlarms(principal.getUserId());
        return ResponseEntity.ok(alarms);
    }

    // 알람 확인 완료
    @PostMapping("/alarms/check")
    public ResponseEntity<Void> checkAlarm(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        calendarService.markAlarmAsChecked(principal.getUserId());
        return ResponseEntity.ok().build();
    }
}
