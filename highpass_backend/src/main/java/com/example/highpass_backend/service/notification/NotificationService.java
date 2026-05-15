package com.example.highpass_backend.service.notification;

import com.example.highpass_backend.dto.notification.NotificationResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.notification.Notification;
import com.example.highpass_backend.entity.notification.NotificationType;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void send(User recipient, NotificationType type, String message, Long targetId, String targetType, String content, String senderNickname) {
        if (type != NotificationType.COMMENT) {
            Optional<Notification> lastNotification = notificationRepository
                    .findFirstByRecipientIdAndSenderNicknameAndTypeAndTargetIdOrderByCreatedAtDesc(
                            recipient.getId(), senderNickname, type, targetId);

            if (lastNotification.isPresent()) {
                LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
                if (lastNotification.get().getCreatedAt().isAfter(fiveMinutesAgo)) {
                    return;
                }
            }
        }

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .targetId(targetId)
                .targetType(targetType)
                .content(content)
                .senderNickname(senderNickname)
                .build();

        notificationRepository.save(notification);
        messagingTemplate.convertAndSend("/sub/notifications/" + recipient.getId(), NotificationResponse.from(notification));
    }

    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long currentUserId, Long notificationId) {
        Notification notification = getNotification(notificationId);
        assertRecipient(currentUserId, notification);
        notification.markAsRead();
    }

    @Transactional
    public void deleteNotification(Long currentUserId, Long notificationId) {
        Notification notification = getNotification(notificationId);
        assertRecipient(currentUserId, notification);
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.deleteByRecipientId(userId);
    }

    private Notification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 알림입니다."));
    }

    private void assertRecipient(Long currentUserId, Notification notification) {
        if (notification.getRecipient() == null || !notification.getRecipient().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "알림에 접근할 권한이 없습니다.");
        }
    }
}
