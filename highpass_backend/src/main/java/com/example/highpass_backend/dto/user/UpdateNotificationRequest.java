package com.example.highpass_backend.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationRequest {
    private boolean likeNotificationEnabled;
    private boolean commentNotificationEnabled;
}
