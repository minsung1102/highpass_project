package com.example.highpass_backend.repository.user;

import com.example.highpass_backend.entity.user.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {
    List<UserCertificate> findByUserId(Long userId);

    Optional<UserCertificate> findByUserIdAndNationalCertificate_Id(Long userId, Long nationalCertificateId);

    boolean existsByUserIdAndNationalCertificate_Id(Long userId, Long nationalCertificateId);
}
