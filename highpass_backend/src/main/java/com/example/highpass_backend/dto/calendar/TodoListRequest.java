package com.example.highpass_backend.dto.calendar;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoListRequest {

        @NotBlank(message = "할 일 내용을 입력해 주세요.")
        @Size(max = 255, message = "할 일 내용은 255자 이하로 입력해 주세요.")
        private String content;

        @NotNull(message = "할 일 날짜를 입력해 주세요.")
        @JsonProperty("date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate date;

        private Boolean status;
}
