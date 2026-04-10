package com.pollflow.controller;

import com.pollflow.dto.*;
import com.pollflow.service.PollService;
import com.pollflow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/polls")
@RequiredArgsConstructor
public class PollController {
    private final PollService pollService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<PollDTO>> getAllPolls(Pageable pageable) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(pollService.getAllPolls(pageable, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PollDTO> getPollById(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(pollService.getPollById(id, userId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PollDTO>> getPollsByCategory(
            @PathVariable Long categoryId, Pageable pageable) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(pollService.getPollsByCategory(categoryId, pageable, userId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PollDTO>> getPollsByUser(
            @PathVariable Long userId, Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(pollService.getPollsByUser(userId, pageable, currentUserId));
    }

    @PostMapping
    @PreAuthorize("hasRole('POLL_ADMIN') or hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<PollDTO> createPoll(@Valid @RequestBody PollRequest request) {
        return ResponseEntity.ok(pollService.createPoll(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('POLL_ADMIN') or hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<PollDTO> updatePoll(
            @PathVariable Long id, @Valid @RequestBody PollRequest request) {
        return ResponseEntity.ok(pollService.updatePoll(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('POLL_ADMIN') or hasRole('VERIFICATION_ADMIN')")
    public ResponseEntity<Void> deletePoll(@PathVariable Long id) {
        pollService.deletePoll(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<PollDTO> vote(@PathVariable Long id, @Valid @RequestBody VoteRequest request) {
        return ResponseEntity.ok(pollService.vote(id, request));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<PollDTO> getResults(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(pollService.getResults(id, userId));
    }

    private Long getCurrentUserId() {
        try {
            return userService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }
}
