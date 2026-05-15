package com.example.highpass_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2SignupRequest {
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임을 입력해 주세요.")
    @Size(max = 50, message = "닉네임은 50자 이하로 입력해 주세요.")
    private String nickname;

    @Size(max = 20, message = "연령대는 20자 이하로 입력해 주세요.")
    private String ageRange;

    @Size(max = 20, message = "성별은 20자 이하로 입력해 주세요.")
    private String gender;

    @Size(max = 50, message = "시/도는 50자 이하로 입력해 주세요.")
    private String siDo;

    @Size(max = 50, message = "군/구는 50자 이하로 입력해 주세요.")
    private String gunGu;

    @NotBlank(message = "소셜 provider를 입력해 주세요.")
    @Pattern(regexp = "(?i)GOOGLE|KAKAO", message = "지원하지 않는 소셜 provider입니다.")
    private String provider;     // GOOGLE or KAKAO

    @NotBlank(message = "소셜 providerId를 입력해 주세요.")
    @Size(max = 120, message = "소셜 providerId는 120자 이하로 입력해 주세요.")
    private String providerId;
}
