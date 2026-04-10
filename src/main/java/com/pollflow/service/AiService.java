package com.pollflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash-lite:generateContent";

    public String listAvailableModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models?key=" + geminiApiKey;
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("List Models Response: " + response.statusCode());
            System.out.println("List Models Body: " + response.body());
            return response.body();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String chatWithAi(String userMessage, Map<String, Object> context) {
        try {
            String prompt = buildContextPrompt(userMessage, context);
            String response = callGeminiApi(prompt);
            return response;
        } catch (Exception e) {
            return "I apologize, but I'm having trouble processing your request right now. Please try again.";
        }
    }

    public String analyzePoll(Long pollId, Map<String, Object> pollData) {
        try {
            String prompt = buildPollAnalysisPrompt(pollData);
            String response = callGeminiApi(prompt);
            return response;
        } catch (Exception e) {
            return "I apologize, but I'm having trouble analyzing this poll right now. Please try again.";
        }
    }

    public String generatePollSuggestions(String topic, int numOptions) {
        try {
            String prompt = String.format(
                "You are a poll creation assistant. Generate %d poll options for a poll about: %s. " +
                "Return ONLY a JSON array of strings with the options, nothing else. " +
                "Format: [\"option1\", \"option2\", ...]",
                numOptions, topic
            );
            String response = callGeminiApi(prompt);
            return response;
        } catch (Exception e) {
            return "[]";
        }
    }

    public String getPollRecommendation(List<Map<String, Object>> userVotingHistory, List<Map<String, Object>> availablePolls) {
        try {
            StringBuilder history = new StringBuilder("User's voting history:\n");
            for (Map<String, Object> vote : userVotingHistory) {
                history.append("- Voted on: ").append(vote.get("title")).append("\n");
            }

            StringBuilder polls = new StringBuilder("Available polls:\n");
            for (Map<String, Object> poll : availablePolls) {
                polls.append("- ID: ").append(poll.get("id"))
                     .append(", Title: ").append(poll.get("title"))
                     .append(", Category: ").append(poll.get("category")).append("\n");
            }

            String prompt = String.format(
                "Based on the user's voting history and available polls, recommend the most relevant poll. " +
                "User history: %s\n\nAvailable polls: %s\n\n" +
                "Return ONLY a JSON object with: {\"pollId\": number, \"reason\": \"short explanation\"}. Nothing else.",
                history.toString(), polls.toString()
            );
            String response = callGeminiApi(prompt);
            return response;
        } catch (Exception e) {
            return "{\"pollId\": null, \"reason\": \"Unable to get recommendations at this time\"}";
        }
    }

    private String buildContextPrompt(String userMessage, Map<String, Object> context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are PollFlow AI Assistant - an AI helper for the PollFlow polling application. ");
        prompt.append("Your job is to help users navigate polls, understand results, and find polls they're interested in.\n\n");
        
        prompt.append("IMPORTANT: You have access to real poll data from the database. ");
        prompt.append("Use this data to answer questions accurately. Do NOT say you don't have access to polls or ask the user to check elsewhere.\n\n");
        
        prompt.append("=== POLL DATA FROM DATABASE ===\n");
        
        if (context.containsKey("totalPolls")) {
            prompt.append("Total Polls in System: ").append(context.get("totalPolls")).append("\n");
        }
        if (context.containsKey("totalCategories")) {
            prompt.append("Total Categories: ").append(context.get("totalCategories")).append("\n");
        }
        if (context.containsKey("pollsList")) {
            prompt.append("\n").append(context.get("pollsList")).append("\n");
        }
        if (context.containsKey("categoriesList")) {
            prompt.append("\n").append(context.get("categoriesList")).append("\n");
        }
        if (context.containsKey("topVotedPolls")) {
            prompt.append("\n").append(context.get("topVotedPolls")).append("\n");
        }
        
        prompt.append("\n=== RESPONSE GUIDELINES ===\n");
        prompt.append("- Use the poll data above to answer questions\n");
        prompt.append("- Be specific: cite poll titles, vote counts, categories\n");
        prompt.append("- If asked about 'most voted' - refer to the top voted polls section\n");
        prompt.append("- If asked about categories - refer to the categories list\n");
        prompt.append("- If asked about a specific poll - look through the polls list\n");
        prompt.append("- Do NOT say you can't access polls - you have all the data above\n");
        prompt.append("- Do NOT ask user to check elsewhere - you have all the information\n");
        prompt.append("- Format: Use plain text, avoid markdown like ** or *\n");
        prompt.append("- Keep responses conversational but informative\n\n");
        
        if (context.containsKey("userName")) {
            prompt.append("Current user: ").append(context.get("userName")).append("\n\n");
        }

        prompt.append("User's question: ").append(userMessage).append("\n\n");
        prompt.append("Your answer:");

        return prompt.toString();
    }

    private String buildPollAnalysisPrompt(Map<String, Object> pollData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a poll analyst. Analyze the following poll and provide insights.\n\n");
        prompt.append("Poll: ").append(pollData.get("title")).append("\n");
        prompt.append("Description: ").append(pollData.get("description")).append("\n");
        prompt.append("Category: ").append(pollData.get("category")).append("\n");
        prompt.append("Total Votes: ").append(pollData.get("totalVotes")).append("\n\n");

        if (pollData.containsKey("options") && pollData.get("options") instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = (List<Map<String, Object>>) pollData.get("options");
            prompt.append("Results:\n");
            for (Map<String, Object> option : options) {
                prompt.append("- ").append(option.get("optionText"))
                       .append(": ").append(option.get("voteCount"))
                       .append(" votes (").append(option.get("percentage")).append("%)\n");
            }
        }

        prompt.append("\nProvide a brief analysis (2-3 sentences) including:\n");
        prompt.append("1. What the results show\n");
        prompt.append("2. Any interesting patterns or insights\n");
        prompt.append("3. A concluding observation\n\n");

        return prompt.toString();
    }

    private String callGeminiApi(String prompt) throws Exception {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            return "AI service is not configured. Please contact the administrator.";
        }

        String requestBody = objectMapper.writeValueAsString(Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            )),
            "generationConfig", Map.of(
                "temperature", 0.7,
                "maxOutputTokens", 500,
                "topP", 0.9,
                "topK", 40
            )
        ));

        String url = GEMINI_API_URL + "?key=" + geminiApiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Gemini API Response Status: " + response.statusCode());
        System.out.println("Gemini API Response Body: " + response.body());

        if (response.statusCode() == 200) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            JsonNode candidates = rootNode.path("candidates");
            
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");
                
                if (parts.isArray() && !parts.isEmpty()) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            return "I couldn't generate a response. Please try again.";
        } else if (response.statusCode() == 401) {
            return "AI service authentication failed. Please contact the administrator.";
        } else if (response.statusCode() == 429) {
            return "AI quota exceeded. Please try again in a few moments. If the issue persists, the daily limit may have been reached.";
        } else {
            return "I encountered an error (Code: " + response.statusCode() + "). Please try again later.";
        }
    }

    public boolean isConfigured() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }
}
