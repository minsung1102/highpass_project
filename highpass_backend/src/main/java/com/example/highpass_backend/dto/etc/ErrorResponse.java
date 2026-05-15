package com.example.highpass_backend.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
}
