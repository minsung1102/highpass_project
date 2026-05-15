package com.example.highpass_backend.service.user;

import com.example.highpass_backend.dto.user.UserCertificateRequest;
import com.example.highpass_backend.dto.user.UserCertificateResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.certificate.NationalCertificate;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.entity.user.UserCertificate;
import com.example.highpass_backend.repository.certificate.NationalCertificateRepository;
import com.example.highpass_backend.repository.user.UserCertificateRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCertificateService {

    private final UserRepository userRepository;
    private final NationalCertificateRepository nationalCertificateRepository;
    private final UserCertificateRepository userCertificateRepository;

    public UserCertificateResponse addUserCertificate(Long userId, UserCertificateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));

        NationalCertificate certificate = nationalCertificateRepository.findById(request.getCertificateScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 자격증 일정입니다."));

        UserCertificate userCertificate = userCertificateRepository
                .findByUserIdAndNationalCertificate_Id(userId, certificate.getId())
                .orElseGet(() -> userCertificateRepository.save(
                        UserCertificate.builder()
                                .user(user)
                                .nationalCertificate(certificate)
                                .build()
                ));

        return toResponse(userCertificate);
    }

    @Transactional(readOnly = true)
    public List<UserCertificateResponse> getUserCertificates(Long userId) {
        return userCertificateRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteUserCertificate(Long currentUserId, Long userCertificateId) {
        UserCertificate userCertificate = userCertificateRepository.findById(userCertificateId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 저장 자격증입니다."));
        if (!userCertificate.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "저장 자격증에 접근할 권한이 없습니다.");
        }
        userCertificateRepository.delete(userCertificate);
    }

    private UserCertificateResponse toResponse(UserCertificate userCertificate) {
        NationalCertificate certificate = userCertificate.getNationalCertificate();
        return UserCertificateResponse.builder()
                .id(userCertificate.getId())
                .certificateScheduleId(certificate.getId())
                .certificateName(certificate.getCertificateName())
                .year(certificate.getYear())
                .writtenApplyStart(certificate.getWrittenApplyStart())
                .writtenApplyEnd(certificate.getWrittenApplyEnd())
                .writtenExamDate(certificate.getWrittenExamDate())
                .writtenResultDate(certificate.getWrittenResultDate())
                .practicalApplyStart(certificate.getPracticalApplyStart())
                .practicalApplyEnd(certificate.getPracticalApplyEnd())
                .practicalExamDate(certificate.getPracticalExamDate())
                .practicalResultDate(certificate.getPracticalResultDate())
                .build();
    }
}
