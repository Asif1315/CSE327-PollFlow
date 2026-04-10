package com.pollflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private Long categoryId;
    
    @NotNull(message = "Options are required")
    private List<String> options;
    
    private String pollType;
    
    private LocalDateTime endTime;
    
    private Boolean hideResults;
}
