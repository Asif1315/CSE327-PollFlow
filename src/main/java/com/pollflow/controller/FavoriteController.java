package com.pollflow.controller;

import com.pollflow.dto.PollDTO;
import com.pollflow.service.PollService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    private final PollService pollService;

    @GetMapping
    public ResponseEntity<Page<PollDTO>> getFavorites(Pageable pageable) {
        return ResponseEntity.ok(pollService.getFavorites(pageable));
    }

    @PostMapping("/{pollId}")
    public ResponseEntity<Void> addFavorite(@PathVariable Long pollId) {
        try {
            pollService.addFavorite(pollId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{pollId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Long pollId) {
        try {
            pollService.removeFavorite(pollId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
