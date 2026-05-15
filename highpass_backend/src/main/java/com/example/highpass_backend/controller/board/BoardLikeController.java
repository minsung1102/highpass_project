package com.example.highpass_backend.controller.board;

import com.example.highpass_backend.entity.board.BoardLike;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.board.BoardLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class BoardLikeController {
    private final BoardLikeService boardLikeService;

    @PostMapping("/{targetType}/{targetId}")
    public ResponseEntity<Void> toggleLike (
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @PathVariable Long targetId,
            @PathVariable BoardLike.TargetType targetType
            ) {
        boardLikeService.toggleLike(principal.getUserId(), targetType, targetId);

        return ResponseEntity.ok().build();
    }
}
