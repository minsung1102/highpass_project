package com.example.highpass_backend.dto.board;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;


public record FreeBoardRequest (
    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 100, message = "제목은 100자 이하로 입력해 주세요.")
    String title,

    @NotBlank(message = "내용을 입력해 주세요.")
    @Size(max = 5000, message = "내용은 5000자 이하로 입력해 주세요.")
    String content,

    List<String> tags
) {}
