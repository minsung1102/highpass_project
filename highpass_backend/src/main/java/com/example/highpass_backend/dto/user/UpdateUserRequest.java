package com.example.highpass_backend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    private String currentPassword;

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
