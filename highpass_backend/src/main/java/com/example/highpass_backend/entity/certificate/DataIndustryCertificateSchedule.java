package com.example.highpass_backend.entity.certificate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "data_industry_certificate_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_data_industry_exam_identity",
                        columnNames = {"exam_name", "exam_type", "exam_round", "exam_date"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DataIndustryCertificateSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_year", nullable = false)
    private int examYear;

    @Column(name = "sequence_no")
    private Integer sequenceNo;

    @Column(name = "exam_name", nullable = false, length = 255)
    private String examName;

    @Column(name = "exam_category", length = 100)
    private String examCategory;

    @Column(name = "exam_round")
    private Integer examRound;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "exam_start_time", length = 50)
    private String examStartTime;

    @Column(name = "apply_start_date")
    private LocalDate applyStartDate;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "exam_place", length = 255)
    private String examPlace;

    @Column(name = "result_announcement_date")
    private LocalDate resultAnnouncementDate;

    @Column(name = "exam_type", length = 50)
    private String examType;
}
