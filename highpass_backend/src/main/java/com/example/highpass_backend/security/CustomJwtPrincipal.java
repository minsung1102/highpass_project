package com.example.highpass_backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomJwtPrincipal {
    private Long userId;
    private String email;
}