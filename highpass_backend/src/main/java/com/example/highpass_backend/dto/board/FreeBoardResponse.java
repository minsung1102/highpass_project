package com.example.highpass_backend.dto.board;

import com.example.highpass_backend.entity.board.FreeBoard;
import com.example.highpass_backend.dto.user.UserDisplayName;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Builder
public class FreeBoardResponse {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String nickname;
    private String avatarVisualClassName;
    private List<String> tags;

    private int viewCount;
    private int likeCount;
    private boolean likedByUser;

    private LocalDateTime createdAt;

    public static FreeBoardResponse from(FreeBoard board) {
        return from(board, false);
    }

    public static FreeBoardResponse from(FreeBoard board, boolean likedByUser) {
        return FreeBoardResponse.builder()
                .id(board.getId())
                .userId(board.getUser().getId())
                .title(board.getTitle())
                .content(board.getContent())
                .nickname(UserDisplayName.nickname(board.getUser()))
                .avatarVisualClassName(board.getUser().getAvatarVisualClassName())
                .tags(board.getTags() != null
                        ? Arrays.asList(board.getTags().split(","))
                        : List.of())
                .viewCount(board.getViewCount())
                .likeCount(board.getLikeCount())
                .likedByUser(likedByUser)
                .createdAt(board.getCreatedAt())
                .build();
    }
}
