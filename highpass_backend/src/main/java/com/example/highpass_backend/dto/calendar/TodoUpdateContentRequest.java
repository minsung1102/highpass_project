package com.example.highpass_backend.dto.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TodoUpdateContentRequest {

    @NotBlank(message = "할 일 내용을 입력해 주세요.")
    @Size(max = 255, message = "할 일 내용은 255자 이하로 입력해 주세요.")
    private String content;
}
