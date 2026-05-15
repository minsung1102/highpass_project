package com.example.highpass_backend.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotBlank(message = "신고 대상 유형을 입력해 주세요.")
        @Pattern(regexp = "user|post|comment|chat|inquiry|USER|POST|COMMENT|CHAT|INQUIRY", message = "신고 대상 유형이 올바르지 않습니다.")
        String targetType,

        @NotBlank(message = "신고 대상 ID를 입력해 주세요.")
        @Size(max = 64, message = "신고 대상 ID는 64자 이하로 입력해 주세요.")
        String targetId,

        @Size(max = 120, message = "신고 대상 라벨은 120자 이하로 입력해 주세요.")
        String targetLabel,

        @NotBlank(message = "신고 사유를 선택해 주세요.")
        @Size(max = 40, message = "신고 사유 코드는 40자 이하로 입력해 주세요.")
        String reasonCode,

        @NotBlank(message = "신고 상세 내용을 입력해 주세요.")
        @Size(min = 10, max = 1000, message = "신고 상세 내용은 10자 이상 1000자 이하로 입력해 주세요.")
        String detail
) {
}
