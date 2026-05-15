package com.example.highpass_backend.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentRequest {

    @NotBlank(message = "댓글 내용을 입력해 주세요.")
    @Size(max = 1000, message = "댓글은 1000자 이하로 입력해 주세요.")
    private String content;

    @NotBlank(message = "댓글 대상 유형을 입력해 주세요.")
    @Pattern(regexp = "FREE|STUDY", message = "댓글 대상 유형이 올바르지 않습니다.")
    private String targetType;

    @NotNull(message = "댓글 대상 ID를 입력해 주세요.")
    private Long targetId;

    private Long userId;
}
