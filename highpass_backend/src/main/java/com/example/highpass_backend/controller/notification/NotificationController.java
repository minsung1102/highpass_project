package com.example.highpass_backend.controller.notification;

import com.example.highpass_backend.dto.notification.NotificationResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.notification.NotificationService;
import com.example.highpass_backend.service.notification.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    // 내 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(principal.getUserId()));
    }

    // 안읽은 알림 개수
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(notificationService.getUnreadCount(principal.getUserId()));
    }

    // 읽음 처리
    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        notificationService.markAsRead(principal.getUserId(), alarmId);
        return ResponseEntity.ok().build();
    }

    // 개별 삭제
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long alarmId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        notificationService.deleteNotification(principal.getUserId(), alarmId);
        return ResponseEntity.ok().build();
    }

    // 전체 삭제
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllNotifications(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        notificationService.deleteAllNotifications(principal.getUserId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/settings")
    public ResponseEntity<Void> updateNotificationSetting(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody com.example.highpass_backend.dto.notification.NotificationSettingRequest request
    ) {
        notificationSettingService.updateNotificationSetting(principal.getUserId(), request);
        return ResponseEntity.ok().build();
    }
}
