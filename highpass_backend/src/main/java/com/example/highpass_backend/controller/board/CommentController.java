package com.example.highpass_backend.controller.board;

import com.example.highpass_backend.dto.board.CommentRequest;
import com.example.highpass_backend.dto.board.CommentResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.board.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = commentService.createComment(principal.getUserId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{targetType}/{targetId}")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable String targetType,
            @PathVariable Long targetId) {

        return ResponseEntity.ok(commentService.getCommentsByTarget(targetId, targetType));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse response = commentService.updateComment(commentId, request, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomJwtPrincipal principal) {

        commentService.deleteComment(commentId, principal.getUserId());
        return ResponseEntity.ok().build();
    }
}
