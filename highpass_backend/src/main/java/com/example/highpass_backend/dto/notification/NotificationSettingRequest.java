package com.example.highpass_backend.dto.notification;

import com.example.highpass_backend.entity.notification.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingRequest {

    @NotNull(message = "알림 유형을 입력해 주세요.")
    private NotificationType type;

    @JsonProperty("isOn")
    private boolean isOn;

}
