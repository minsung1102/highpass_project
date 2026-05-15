package com.example.highpass_backend.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserCertificateRequest {
    @NotNull(message = "자격증 일정 ID를 입력해 주세요.")
    private Long certificateScheduleId;
}
