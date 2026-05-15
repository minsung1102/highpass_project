package com.example.highpass_backend.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAvatarRequest {

    @Size(max = 120, message = "아바타 스타일은 120자 이하로 입력해 주세요.")
    private String avatarVisualClassName;
}
