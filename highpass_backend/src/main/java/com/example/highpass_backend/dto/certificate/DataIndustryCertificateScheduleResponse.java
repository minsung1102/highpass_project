package com.example.highpass_backend.dto.certificate;

import com.example.highpass_backend.entity.certificate.DataIndustryCertificateSchedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DataIndustryCertificateScheduleResponse {
    private Long id;
    private int examYear;
    private Integer sequenceNo;
    private String examName;
    private String examCategory;
    private Integer examRound;
    private LocalDate examDate;
    private String examStartTime;
    private LocalDate applyStartDate;
    private LocalDate applyEndDate;
    private String examPlace;
    private LocalDate resultAnnouncementDate;
    private String examType;

    public static DataIndustryCertificateScheduleResponse from(DataIndustryCertificateSchedule entity) {
        return DataIndustryCertificateScheduleResponse.builder()
                .id(entity.getId())
                .examYear(entity.getExamYear())
                .sequenceNo(entity.getSequenceNo())
                .examName(entity.getExamName())
                .examCategory(entity.getExamCategory())
                .examRound(entity.getExamRound())
                .examDate(entity.getExamDate())
                .examStartTime(entity.getExamStartTime())
                .applyStartDate(entity.getApplyStartDate())
                .applyEndDate(entity.getApplyEndDate())
                .examPlace(entity.getExamPlace())
                .resultAnnouncementDate(entity.getResultAnnouncementDate())
                .examType(entity.getExamType())
                .build();
    }
}
