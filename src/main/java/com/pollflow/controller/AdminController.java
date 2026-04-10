package com.pollflow.controller;

import com.pollflow.dto.*;
import com.pollflow.entity.Poll;
import com.pollflow.service.AdminService;
import com.pollflow.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final PollService pollService;

    @GetMapping("/analytics/dashboard")
    public ResponseEntity<AnalyticsDTO> getDashboardAnalytics() {
        return ResponseEntity.ok(adminService.getDashboardAnalytics());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/pending")
    @PreAuthorize("hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<List<UserDTO>> getPendingUsers() {
        return ResponseEntity.ok(adminService.getPendingUsers());
    }

    @GetMapping("/users/search")
    @PreAuthorize("hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(adminService.searchUsers(query));
    }

    @PutMapping("/users/{id}/approve")
    @PreAuthorize("hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<UserDTO> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveUser(id));
    }

    @PutMapping("/users/{id}/reject")
    @PreAuthorize("hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<UserDTO> rejectUser(
            @PathVariable Long id, 
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminService.rejectUser(id, reason));
    }

    @GetMapping("/polls")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<List<PollDTO>> getAllPolls() {
        return ResponseEntity.ok(pollService.getAllPolls());
    }

    @GetMapping("/polls/{id}")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<?> getPollById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(pollService.getPollByIdForAdmin(id));
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/polls")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<PollDTO> createPoll(@RequestBody PollRequest request) {
        return ResponseEntity.ok(pollService.createPoll(request));
    }

    @PutMapping("/polls/{id}")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<PollDTO> updatePoll(@PathVariable Long id, @RequestBody PollRequest request) {
        return ResponseEntity.ok(pollService.updatePoll(id, request));
    }

    @DeleteMapping("/polls/{id}")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        pollService.deletePoll(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/polls/search")
    @PreAuthorize("hasRole('POLL_ADMIN')")
    public ResponseEntity<List<PollDTO>> searchPolls(@RequestParam String query) {
        return ResponseEntity.ok(pollService.searchPolls(query));
    }
}