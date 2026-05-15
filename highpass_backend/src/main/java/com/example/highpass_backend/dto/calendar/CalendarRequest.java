package com.example.highpass_backend.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class CalendarRequest {
    @NotNull(message = "일정 시작일을 입력해 주세요.")
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @NotBlank(message = "일정 제목을 입력해 주세요.")
    @Size(max = 50, message = "일정 제목은 50자 이하로 입력해 주세요.")
    private String title;

    @Size(max = 5000, message = "일정 내용은 5000자 이하로 입력해 주세요.")
    private String content;

    @Size(max = 30, message = "일정 유형은 30자 이하로 입력해 주세요.")
    private String kind;
}
