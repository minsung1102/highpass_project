package com.example.highpass_backend.service.notification;

import com.example.highpass_backend.dto.notification.NotificationSettingRequest;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final UserRepository userRepository;

    @Transactional
    public void updateNotificationSetting(Long userId, NotificationSettingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "인증된 사용자를 찾을 수 없습니다."));

        switch (request.getType()) {
            case COMMENT -> user.toggleCommentNoti(request.isOn());
            case LIKE -> user.toggleLikeNoti(request.isOn());
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT, "알 수 없는 알림 종류입니다.");
        }
    }
}
