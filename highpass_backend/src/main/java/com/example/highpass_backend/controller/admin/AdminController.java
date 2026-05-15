package com.example.highpass_backend.controller.admin;

import com.example.highpass_backend.dto.admin.AdminPostResponse;
import com.example.highpass_backend.dto.admin.AdminReportResponse;
import com.example.highpass_backend.dto.admin.AdminStatusRequest;
import com.example.highpass_backend.dto.admin.AdminUserResponse;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.admin.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getUsers(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.getUsers(principal.getUserId()));
    }

    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody AdminStatusRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.updateUserStatus(principal.getUserId(), userId, request.status()));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<AdminPostResponse>> getPosts(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.getPosts(principal.getUserId()));
    }

    @PatchMapping("/posts/{postId}/status")
    public ResponseEntity<AdminPostResponse> updatePostStatus(
            @PathVariable String postId,
            @Valid @RequestBody AdminStatusRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.updatePostStatus(principal.getUserId(), postId, request.status()));
    }

    @GetMapping("/reports")
    public ResponseEntity<List<AdminReportResponse>> getReports(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.getReports(principal.getUserId()));
    }

    @PatchMapping("/reports/{reportId}/status")
    public ResponseEntity<AdminReportResponse> updateReportStatus(
            @PathVariable String reportId,
            @Valid @RequestBody AdminStatusRequest request,
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(adminService.updateReportStatus(principal.getUserId(), reportId, request.status(), request.message()));
    }

}
