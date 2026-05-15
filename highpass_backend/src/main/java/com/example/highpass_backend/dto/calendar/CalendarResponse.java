package com.example.highpass_backend.dto.calendar;

import com.example.highpass_backend.entity.calendar.Calendar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarResponse {
    private Long userId;
    private Long id;
    private String title;
    private String content;
    private String kind;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String userNickname;

    public static CalendarResponse from(Calendar calendar) {
        return CalendarResponse.builder()
                .userId(calendar.getUser().getId())
                .id(calendar.getId())
                .title(calendar.getTitle())
                .content(calendar.getContent())
                .kind(calendar.getKind())
                .startDate(calendar.getStartDate())
                .endDate(calendar.getEndDate())
                .startTime(calendar.getStartTime())
                .endTime(calendar.getEndTime())
                .userNickname(calendar.getUser().getNickname())
                .build();
    }
}
