package com.pollflow.controller;

import com.pollflow.dto.CategoryDTO;
import com.pollflow.dto.PollDTO;
import com.pollflow.service.AiService;
import com.pollflow.service.CategoryService;
import com.pollflow.service.PollService;
import com.pollflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final UserService userService;
    private final PollService pollService;
    private final CategoryService categoryService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Please login to use AI chat"
            ));
        }

        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Message cannot be empty"
            ));
        }

        Map<String, Object> context = buildContext();
        String response = aiService.chatWithAi(userMessage, context);
        
        return ResponseEntity.ok(Map.of(
            "message", response,
            "timestamp", new Date().toString()
        ));
    }

    @GetMapping("/analyze/{pollId}")
    public ResponseEntity<Map<String, Object>> analyzePoll(
            @PathVariable Long pollId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Please login to analyze polls"
            ));
        }

        Map<String, Object> pollData = new HashMap<>();
        pollData.put("title", "Sample Poll");
        pollData.put("description", "Sample description");
        pollData.put("category", "Technology");
        pollData.put("totalVotes", 150);
        
        List<Map<String, Object>> options = new ArrayList<>();
        options.add(Map.of("optionText", "Yes", "voteCount", 80, "percentage", 53.3));
        options.add(Map.of("optionText", "No", "voteCount", 70, "percentage", 46.7));
        pollData.put("options", options);

        String analysis = aiService.analyzePoll(pollId, pollData);

        return ResponseEntity.ok(Map.of(
            "pollId", pollId,
            "analysis", analysis,
            "timestamp", new Date().toString()
        ));
    }

    @GetMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendation(Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Please login to get recommendations"
            ));
        }

        List<Map<String, Object>> votingHistory = new ArrayList<>();
        List<Map<String, Object>> availablePolls = new ArrayList<>();

        String recommendation = aiService.getPollRecommendation(votingHistory, availablePolls);

        return ResponseEntity.ok(Map.of(
            "recommendation", recommendation,
            "timestamp", new Date().toString()
        ));
    }

    @GetMapping("/suggest")
    public ResponseEntity<Map<String, Object>> suggestPolls(
            @RequestParam String topic,
            @RequestParam(defaultValue = "4") int numOptions,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Please login to get poll suggestions"
            ));
        }

        String suggestions = aiService.generatePollSuggestions(topic, numOptions);

        return ResponseEntity.ok(Map.of(
            "topic", topic,
            "suggestions", suggestions,
            "timestamp", new Date().toString()
        ));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "available", aiService.isConfigured(),
            "service", "PollFlow AI Assistant",
            "version", "1.0"
        ));
    }

    @GetMapping("/models")
    public ResponseEntity<Map<String, Object>> listModels() {
        String models = aiService.listAvailableModels();
        return ResponseEntity.ok(Map.of(
            "models", models
        ));
    }

    private Map<String, Object> buildContext() {
        Map<String, Object> context = new HashMap<>();
        
        try {
            var user = userService.getCurrentUser();
            if (user != null) {
                context.put("userName", user.getFullName());
                context.put("userEmail", user.getEmail());
            }
        } catch (Exception e) {
            // User context not available
        }

        try {
            List<PollDTO> polls = pollService.getAllPolls();
            List<CategoryDTO> categories = categoryService.getAllCategories();
            
            context.put("totalPolls", polls.size());
            context.put("totalCategories", categories.size());
            
            StringBuilder pollsInfo = new StringBuilder();
            pollsInfo.append("Total Active Polls: ").append(polls.size()).append("\n\n");
            
            pollsInfo.append("=== ALL AVAILABLE POLLS ===\n");
            for (PollDTO poll : polls) {
                pollsInfo.append("- ID: ").append(poll.getId())
                         .append(", Title: ").append(poll.getTitle())
                         .append(", Category: ").append(poll.getCategoryName())
                         .append(", Type: ").append(poll.getPollType())
                         .append(", Votes: ").append(poll.getTotalVotes())
                         .append(", Has Voted: ").append(poll.getHasVoted())
                         .append("\n");
            }
            
            StringBuilder categoriesInfo = new StringBuilder();
            categoriesInfo.append("=== POLL CATEGORIES ===\n");
            for (CategoryDTO cat : categories) {
                categoriesInfo.append("- ").append(cat.getName());
                long count = polls.stream().filter(p -> p.getCategoryName().equals(cat.getName())).count();
                categoriesInfo.append(" (").append(count).append(" polls)\n");
            }
            
            polls.sort(Comparator.comparingLong(PollDTO::getTotalVotes).reversed());
            StringBuilder topPolls = new StringBuilder();
            topPolls.append("=== TOP 5 MOST VOTED POLLS ===\n");
            for (int i = 0; i < Math.min(5, polls.size()); i++) {
                PollDTO poll = polls.get(i);
                topPolls.append((i+1)).append(". ").append(poll.getTitle())
                        .append(" - ").append(poll.getTotalVotes()).append(" votes\n");
            }
            
            context.put("pollsList", pollsInfo.toString());
            context.put("categoriesList", categoriesInfo.toString());
            context.put("topVotedPolls", topPolls.toString());
            context.put("recentPolls", polls.stream().limit(5).map(PollDTO::getTitle).toList().toString());
            
        } catch (Exception e) {
            context.put("pollsError", "Could not load polls: " + e.getMessage());
        }

        return context;
    }
}
