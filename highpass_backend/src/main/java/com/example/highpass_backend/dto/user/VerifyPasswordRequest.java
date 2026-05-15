package com.example.highpass_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordRequest {
    @NotBlank(message = "현재 비밀번호를 입력해 주세요.")
    private String currentPassword;
}
