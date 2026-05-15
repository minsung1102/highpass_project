package com.example.highpass_backend.controller.board;

import com.example.highpass_backend.dto.board.FreeBoardRequest;
import com.example.highpass_backend.dto.board.FreeBoardResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.board.FreeBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class FreeBoardController {
    private final FreeBoardService freeBoardService;

    @PostMapping
    public ResponseEntity<FreeBoardResponse> addFreeBoard(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody FreeBoardRequest request
    ) {
        FreeBoardResponse freeBoardResponse = freeBoardService.createFreeBoard(principal.getUserId(), request);
        return new ResponseEntity<>(freeBoardResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FreeBoardResponse>> getAllBoards(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(freeBoardService.getFreeBoardList(userId));
    }

    @GetMapping("/{freeBoardId}")
    public ResponseEntity<FreeBoardResponse> getBoard(@PathVariable Long freeBoardId, @RequestParam(required = false) Long userId) {
        FreeBoardResponse response = freeBoardService.getFreeBoard(freeBoardId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{freeBoardId}")
    public ResponseEntity<Void> deleteFreeBoard(
            @PathVariable Long freeBoardId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        freeBoardService.deleteFreeBoard(principal.getUserId(), freeBoardId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{freeBoardId}")
    public ResponseEntity<FreeBoardResponse> updateBoard(
            @PathVariable Long freeBoardId,
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody FreeBoardRequest request
    ) {
        FreeBoardResponse freeBoardResponse = freeBoardService.updateFreeBoard(principal.getUserId(), freeBoardId, request);
        return ResponseEntity.ok(freeBoardResponse);
    }
}
