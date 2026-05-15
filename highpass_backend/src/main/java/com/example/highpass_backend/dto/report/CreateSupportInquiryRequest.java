package com.example.highpass_backend.dto.report;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportInquiryRequest(
        @NotBlank(message = "이메일을 입력해 주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "문의 제목을 입력해 주세요.")
        @Size(max = 120, message = "문의 제목은 120자 이하로 입력해 주세요.")
        String title,

        @NotBlank(message = "문의 사유를 선택해 주세요.")
        @Size(max = 40, message = "문의 사유 코드는 40자 이하로 입력해 주세요.")
        String reasonCode,

        @NotBlank(message = "문의 상세 내용을 입력해 주세요.")
        @Size(min = 10, max = 1000, message = "문의 상세 내용은 10자 이상 1000자 이하로 입력해 주세요.")
        String detail
) {
}
