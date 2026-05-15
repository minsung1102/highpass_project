package com.example.highpass_backend.repository.certificate;

import com.example.highpass_backend.entity.certificate.DataIndustryCertificateSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DataIndustryCertificateScheduleRepository extends JpaRepository<DataIndustryCertificateSchedule, Long> {

    Optional<DataIndustryCertificateSchedule> findByExamNameAndExamTypeAndExamRoundAndExamDate(
            String examName,
            String examType,
            Integer examRound,
            LocalDate examDate
    );

    List<DataIndustryCertificateSchedule> findAllByExamYearOrderByExamDateAscExamNameAsc(int examYear);
}
