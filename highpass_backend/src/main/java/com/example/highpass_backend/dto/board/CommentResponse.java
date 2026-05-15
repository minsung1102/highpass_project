package com.example.highpass_backend.dto.board;

import com.example.highpass_backend.entity.board.Comment;
import com.example.highpass_backend.dto.user.UserDisplayName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private String content;
    private String nickname;
    private Long userId;
    private String avatarVisualClassName;

    private LocalDateTime createdAt;

    public static CommentResponse from (Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(UserDisplayName.nickname(comment.getUser()))
                .userId(comment.getUser().getId())
                .avatarVisualClassName(comment.getUser().getAvatarVisualClassName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
