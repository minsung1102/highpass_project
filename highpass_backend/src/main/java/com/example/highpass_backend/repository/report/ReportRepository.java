package com.example.highpass_backend.repository.report;

import com.example.highpass_backend.entity.report.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    int countByTargetTypeAndTargetId(Report.TargetType targetType, String targetId);

    List<Report> findAllByOrderByCreatedAtDesc();

    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
}
