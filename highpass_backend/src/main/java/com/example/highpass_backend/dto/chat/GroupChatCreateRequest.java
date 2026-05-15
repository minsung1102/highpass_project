package com.example.highpass_backend.dto.chat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupChatCreateRequest {
    private String name;
    private Long ownerId;
    private Boolean approvalRequired;
}
