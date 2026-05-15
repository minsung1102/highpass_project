package com.example.highpass_backend.controller.report;

import com.example.highpass_backend.dto.report.CreateReportRequest;
import com.example.highpass_backend.dto.report.CreateSupportInquiryRequest;
import com.example.highpass_backend.dto.report.ReportResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        ReportResponse response = reportService.createReport(principal.getUserId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/inquiries")
    public ResponseEntity<ReportResponse> createSupportInquiry(
            @Valid @RequestBody CreateSupportInquiryRequest request
    ) {
        ReportResponse response = reportService.createSupportInquiry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReportResponse>> getMyReports(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(reportService.getMyReports(principal.getUserId()));
    }
}
