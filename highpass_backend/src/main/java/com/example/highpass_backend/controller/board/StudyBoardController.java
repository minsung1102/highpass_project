package com.example.highpass_backend.controller.board;

import com.example.highpass_backend.dto.board.StudyBoardCreateRequest;
import com.example.highpass_backend.dto.board.StudyBoardDetailResponse;
import com.example.highpass_backend.dto.board.StudyBoardListResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.board.StudyBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyBoardController {
    private final StudyBoardService studyBoardService;

    @PostMapping
    public ResponseEntity<StudyBoardDetailResponse> addStudy(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody StudyBoardCreateRequest request
    ) {
        StudyBoardDetailResponse studyBoardDetailResponse = studyBoardService.createStudy(principal.getUserId(), request);
        return new ResponseEntity<>(studyBoardDetailResponse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<StudyBoardListResponse>> getAllStudy(@RequestParam(required = false) Long userId) {
        return ResponseEntity.ok(studyBoardService.getStudyList(userId));
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<StudyBoardDetailResponse> getStudy(@PathVariable Long studyId, @RequestParam(required = false) Long userId) {
        StudyBoardDetailResponse response = studyBoardService.getStudy(studyId, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{studyId}")
    public ResponseEntity<Void> deleteStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        studyBoardService.deleteStudy(principal.getUserId(), studyId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{studyId}")
    public ResponseEntity<StudyBoardDetailResponse> updateStudy(
            @PathVariable Long studyId,
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody StudyBoardCreateRequest request
    ) {
        StudyBoardDetailResponse response = studyBoardService.updateStudy(principal.getUserId(), studyId, request);
        return ResponseEntity.ok(response);
    }
}
