package com.example.highpass_backend.dto.board;

import lombok.Getter;

@Getter
public class InteractionRequest {

    private String targetType;
    private Long targetId;
}