package com.pollflow.strategy;

import com.pollflow.dto.PollDTO;
import com.pollflow.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SingleVoteStrategy implements VotingStrategy {
    private final VoteRepository voteRepository;

    @Override
    public boolean canVote(Long userId, Long pollId) {
        return !voteRepository.existsByPollIdAndUserId(pollId, userId);
    }

    @Override
    public void recordVote(Long userId, Long pollId, Long optionId) {
        // Vote is recorded by PollService
    }

    @Override
    public PollDTO applyVotingRules(PollDTO poll) {
        return poll;
    }
}
