package com.example.highpass_backend.repository.notification;

import com.example.highpass_backend.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByRecipientIdAndIsReadFalse(Long recipientId);
    void deleteByRecipientId(Long recipientId);

    // 최근 중복 알림 체크용
    Optional<Notification> findFirstByRecipientIdAndSenderNicknameAndTypeAndTargetIdOrderByCreatedAtDesc(
            Long recipientId, String senderNickname, com.example.highpass_backend.entity.notification.NotificationType type, Long targetId);
}

