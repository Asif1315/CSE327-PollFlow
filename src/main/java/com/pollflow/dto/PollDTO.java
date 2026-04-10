package com.pollflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollDTO {
    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private Long createdById;
    private String createdByName;
    private List<PollOptionDTO> options;
    private Long totalVotes;
    private Boolean hasVoted;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private String pollType;
    private LocalDateTime endTime;
    private Boolean showResults;
    private Boolean hideResults;
}
