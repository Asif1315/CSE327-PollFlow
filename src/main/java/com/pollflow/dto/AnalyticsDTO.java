package com.pollflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private long totalPolls;
    private long totalVotes;
    private long totalUsers;
    private long pendingUsers;
    private long totalCategories;
    private long activePolls;
    private List<PollDTO> topPolls;
}
