package com.example.highpass_backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String email;
    private String nickname;
    private String ageRange;
    private String gender;
    private String siDo;
    private String gunGu;
    private String role;
    private String redirectUrl;
}
