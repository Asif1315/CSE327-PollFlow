package com.pollflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollOptionDTO {
    private Long id;
    private String optionText;
    private Integer voteCount;
    private Double percentage;
}
