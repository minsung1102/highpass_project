package com.example.highpass_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSignupRequest {
    @NotBlank(message = "이메일을 입력해 주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하로 입력해 주세요.")
    private String password;

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
}
