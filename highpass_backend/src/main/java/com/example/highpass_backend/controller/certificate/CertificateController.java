package com.example.highpass_backend.controller.certificate;

import com.example.highpass_backend.dto.certificate.CertificateScheduleResponse;
import com.example.highpass_backend.dto.certificate.CertificateSyncResponse;
import com.example.highpass_backend.dto.certificate.DataIndustryCertificateScheduleResponse;
import com.example.highpass_backend.service.certificate.CertificateDataService;
import com.example.highpass_backend.service.certificate.CertificateService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final CertificateDataService certificateDataService;

    @GetMapping(value = "/last-synced", produces = "application/json")
    public ResponseEntity<Map<String, Object>> getLastSyncedAt() {
        LocalDateTime lastSyncedAt = certificateService.getLastSyncedAt().orElse(null);
        return ResponseEntity.ok(Map.of("lastSyncedAt", lastSyncedAt != null ? lastSyncedAt.toString() : ""));
    }

    @GetMapping(value = "/schedules", produces = "application/json")
    public List<CertificateScheduleResponse> getSchedules() {
        return certificateService.getSchedules();
    }

    @GetMapping(value = "/data-industry-schedules", produces = "application/json")
    public List<DataIndustryCertificateScheduleResponse> getDataIndustrySchedules(
            @RequestParam(defaultValue = "2026") int examYear
    ) {
        return certificateService.getDataIndustrySchedules(examYear);
    }

    @PostMapping(value = "/admin/sync", produces = "application/json")
    public ResponseEntity<CertificateSyncResponse> syncSchedules() {
        return ResponseEntity.ok(certificateService.syncSchedules());
    }

    @PostMapping(value = "/admin/data-industry-sync", produces = "application/json")
    public ResponseEntity<CertificateSyncResponse> syncDataIndustrySchedules(
            @RequestParam(defaultValue = "2026") int examYear
    ) {
        return ResponseEntity.ok(certificateService.syncDataIndustrySchedules(examYear));
    }

    @GetMapping(value = "/data-industry-preview", produces = "application/json")
    public ResponseEntity<String> getDataIndustryPreview(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int perPage,
            @RequestParam(defaultValue = "2026") int examYear
    ) {
        JSONObject payload = certificateDataService.fetchDataIndustrySchedulePreview(page, perPage, examYear);
        return ResponseEntity.ok(payload.toString());
    }
}
