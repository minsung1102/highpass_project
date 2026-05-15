package com.example.highpass_backend.dto.user;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserCertificateResponse {
    private Long id;
    private Long certificateScheduleId;
    private String certificateName;
    private int year;
    private int round;
    private LocalDate writtenApplyStart;
    private LocalDate writtenApplyEnd;
    private LocalDate writtenExamDate;
    private LocalDate writtenResultDate;
    private LocalDate practicalApplyStart;
    private LocalDate practicalApplyEnd;
    private LocalDate practicalExamDate;
    private LocalDate practicalResultDate;
}
