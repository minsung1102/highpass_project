package com.example.highpass_backend.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminStatusRequest(
        @NotBlank(message = "상태 값을 입력해 주세요.")
        @Size(max = 20, message = "상태 값은 20자 이하로 입력해 주세요.")
        String status,

        @Size(max = 2000, message = "답변은 2000자 이하로 입력해 주세요.")
        String message
) {
}
