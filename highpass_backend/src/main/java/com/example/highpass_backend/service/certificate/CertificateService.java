package com.example.highpass_backend.service.certificate;

import com.example.highpass_backend.dto.certificate.CertificateScheduleResponse;
import com.example.highpass_backend.dto.certificate.CertificateSyncResponse;
import com.example.highpass_backend.dto.certificate.DataIndustryCertificateScheduleResponse;
import com.example.highpass_backend.entity.certificate.DataIndustryCertificateSchedule;
import com.example.highpass_backend.entity.certificate.NationalCertificate;
import com.example.highpass_backend.repository.certificate.DataIndustryCertificateScheduleRepository;
import com.example.highpass_backend.repository.certificate.NationalCertificateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private static final int DEFAULT_DATA_INDUSTRY_EXAM_YEAR = 2026;

    private final CertificateDataService certificateDataService;
    private final NationalCertificateRepository nationalCertificateRepository;
    private final DataIndustryCertificateScheduleRepository dataIndustryCertificateScheduleRepository;

    @PostConstruct
    @Transactional
    public void initializeSchedules() {
        try {
            initializeQnetSchedules();
        } catch (Exception exception) {
            log.error("Q-Net 초기 적재 중 오류가 발생했습니다.", exception);
        }

        try {
            initializeDataIndustrySchedules(DEFAULT_DATA_INDUSTRY_EXAM_YEAR);
        } catch (Exception exception) {
            log.error("데이터 자격검정 초기 적재 중 오류가 발생했습니다.", exception);
        }
    }

    private void initializeQnetSchedules() {
        if (nationalCertificateRepository.count() > 0) {
            log.info("Q-Net schedules already exist. Skipping initial load.");
            return;
        }

        CertificateSyncResponse syncResult = syncSchedules();
        if (syncResult.getFetchedCount() == 0) {
            log.warn("Failed to fetch Q-Net schedules during initial load.");
            return;
        }

        log.info(
                "Q-Net initial load completed: fetched={}, created={}, updated={}, total={}",
                syncResult.getFetchedCount(),
                syncResult.getCreatedCount(),
                syncResult.getUpdatedCount(),
                syncResult.getTotalCount()
        );
    }

    private void initializeDataIndustrySchedules(int examYear) {
        if (dataIndustryCertificateScheduleRepository.count() > 0) {
            log.info("Data-industry schedules already exist. Skipping initial load for year={}.", examYear);
            return;
        }

        CertificateSyncResponse syncResult = syncDataIndustrySchedules(examYear);
        if (syncResult.getFetchedCount() == 0) {
            log.warn("Failed to fetch data-industry schedules during initial load for year={}.", examYear);
            return;
        }

        log.info(
                "Data-industry initial load completed: year={}, fetched={}, created={}, updated={}, total={}",
                examYear,
                syncResult.getFetchedCount(),
                syncResult.getCreatedCount(),
                syncResult.getUpdatedCount(),
                syncResult.getTotalCount()
        );
    }

    @Transactional(readOnly = true)
    public Optional<LocalDateTime> getLastSyncedAt() {
        return nationalCertificateRepository.findMaxUpdatedAt();
    }

    @Transactional(readOnly = true)
    public List<CertificateScheduleResponse> getSchedules() {
        return nationalCertificateRepository.findAllByOrderByYearAscCertificateNameAscRoundAsc().stream()
                .map(entity -> CertificateScheduleResponse.builder()
                        .id(entity.getId())
                        .certificateName(entity.getCertificateName())
                        .year(entity.getYear())
                        .writtenApplyStart(entity.getWrittenApplyStart())
                        .writtenApplyEnd(entity.getWrittenApplyEnd())
                        .writtenExamDate(entity.getWrittenExamDate())
                        .writtenResultDate(entity.getWrittenResultDate())
                        .practicalApplyStart(entity.getPracticalApplyStart())
                        .practicalApplyEnd(entity.getPracticalApplyEnd())
                        .practicalExamDate(entity.getPracticalExamDate())
                        .practicalResultDate(entity.getPracticalResultDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DataIndustryCertificateScheduleResponse> getDataIndustrySchedules(int examYear) {
        return dataIndustryCertificateScheduleRepository.findAllByExamYearOrderByExamDateAscExamNameAsc(examYear)
                .stream()
                .map(DataIndustryCertificateScheduleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CertificateSyncResponse syncSchedules() {
        List<NationalCertificate> fetched = certificateDataService.fetchAll();
        if (fetched.isEmpty()) {
            return CertificateSyncResponse.builder()
                    .fetchedCount(0)
                    .createdCount(0)
                    .updatedCount(0)
                    .totalCount((int) nationalCertificateRepository.count())
                    .message("Q-Net 일정 데이터를 가져오지 못했습니다.")
                    .build();
        }

        int createdCount = 0;
        int updatedCount = 0;

        for (NationalCertificate incoming : fetched) {
            NationalCertificate existing = nationalCertificateRepository
                    .findByCertificateNameAndWrittenApplyStartAndPracticalApplyStart(
                            incoming.getCertificateName(),
                            incoming.getWrittenApplyStart(),
                            incoming.getPracticalApplyStart()
                    )
                    .orElse(null);

            if (existing == null) {
                nationalCertificateRepository.save(incoming);
                createdCount++;
                continue;
            }

            if (applyUpdates(existing, incoming)) {
                updatedCount++;
            }
            nationalCertificateRepository.save(existing);
        }
        nationalCertificateRepository.touchAllUpdatedAt(LocalDateTime.now());

        return CertificateSyncResponse.builder()
                .fetchedCount(fetched.size())
                .createdCount(createdCount)
                .updatedCount(updatedCount)
                .totalCount((int) nationalCertificateRepository.count())
                .message("자격증 일정 동기화가 완료되었습니다.")
                .build();
    }

    @Transactional
    public CertificateSyncResponse syncDataIndustrySchedules(int examYear) {
        List<DataIndustryCertificateSchedule> fetched = certificateDataService.fetchDataIndustrySchedulesByYear(examYear);
        if (fetched.isEmpty()) {
            return CertificateSyncResponse.builder()
                    .fetchedCount(0)
                    .createdCount(0)
                    .updatedCount(0)
                    .totalCount((int) dataIndustryCertificateScheduleRepository.count())
                    .message("데이터 자격검정 일정을 가져오지 못했습니다.")
                    .build();
        }

        int createdCount = 0;
        for (DataIndustryCertificateSchedule incoming : fetched) {
            boolean exists = dataIndustryCertificateScheduleRepository
                    .findByExamNameAndExamTypeAndExamRoundAndExamDate(
                            incoming.getExamName(),
                            incoming.getExamType(),
                            incoming.getExamRound(),
                            incoming.getExamDate()
                    )
                    .isPresent();
            if (exists) {
                continue;
            }
            dataIndustryCertificateScheduleRepository.save(incoming);
            createdCount++;
        }

        return CertificateSyncResponse.builder()
                .fetchedCount(fetched.size())
                .createdCount(createdCount)
                .updatedCount(0)
                .totalCount((int) dataIndustryCertificateScheduleRepository.count())
                .message("데이터 자격검정 일정 동기화가 완료되었습니다.")
                .build();
    }

    private boolean applyUpdates(NationalCertificate existing, NationalCertificate incoming) {
        boolean changed = false;

        if (!sameDate(existing.getWrittenApplyStart(), incoming.getWrittenApplyStart())) {
            existing.setWrittenApplyStart(incoming.getWrittenApplyStart());
            changed = true;
        }
        if (!sameDate(existing.getWrittenApplyEnd(), incoming.getWrittenApplyEnd())) {
            existing.setWrittenApplyEnd(incoming.getWrittenApplyEnd());
            changed = true;
        }
        if (!sameDate(existing.getWrittenExamDate(), incoming.getWrittenExamDate())) {
            existing.setWrittenExamDate(incoming.getWrittenExamDate());
            changed = true;
        }
        if (!sameDate(existing.getWrittenResultDate(), incoming.getWrittenResultDate())) {
            existing.setWrittenResultDate(incoming.getWrittenResultDate());
            changed = true;
        }
        if (!sameDate(existing.getPracticalApplyStart(), incoming.getPracticalApplyStart())) {
            existing.setPracticalApplyStart(incoming.getPracticalApplyStart());
            changed = true;
        }
        if (!sameDate(existing.getPracticalApplyEnd(), incoming.getPracticalApplyEnd())) {
            existing.setPracticalApplyEnd(incoming.getPracticalApplyEnd());
            changed = true;
        }
        if (!sameDate(existing.getPracticalExamDate(), incoming.getPracticalExamDate())) {
            existing.setPracticalExamDate(incoming.getPracticalExamDate());
            changed = true;
        }
        if (!sameDate(existing.getPracticalResultDate(), incoming.getPracticalResultDate())) {
            existing.setPracticalResultDate(incoming.getPracticalResultDate());
            changed = true;
        }

        return changed;
    }

    private boolean sameDate(LocalDate left, LocalDate right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.isEqual(right);
    }
}
