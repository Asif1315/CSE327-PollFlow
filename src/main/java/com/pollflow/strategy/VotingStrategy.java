package com.pollflow.strategy;

import com.pollflow.dto.PollDTO;

public interface VotingStrategy {
    boolean canVote(Long userId, Long pollId);
    void recordVote(Long userId, Long pollId, Long optionId);
    PollDTO applyVotingRules(PollDTO poll);
}
