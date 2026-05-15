package com.example.highpass_backend.dto.certificate;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class CertificateScheduleResponse {
    private Long id;
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
