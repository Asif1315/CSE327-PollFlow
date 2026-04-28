package com.pollflow.strategy;

import com.pollflow.dto.PollDTO;
import com.pollflow.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SingleVoteStrategyTest {

    @Mock
    private VoteRepository voteRepository;

    @InjectMocks
    private SingleVoteStrategy singleVoteStrategy;

    @Test
    void canVote_ShouldReturnTrue_WhenUserHasNotVotedBefore() {
        Long pollId = 1L;
        Long userId = 10L;

        when(voteRepository.existsByPollIdAndUserId(userId, pollId)).thenReturn(false);

        boolean result = singleVoteStrategy.canVote(pollId, userId);

        assertTrue(result);
    }

    @Test
    void canVote_ShouldReturnFalse_WhenUserHasAlreadyVoted() {
        Long pollId = 1L;
        Long userId = 10L;

        when(voteRepository.existsByPollIdAndUserId(userId, pollId)).thenReturn(true);

        boolean result = singleVoteStrategy.canVote(pollId, userId);

        assertFalse(result);
    }

    @Test
    void applyVotingRules_ShouldReturnSamePollDTO() {
        PollDTO pollDTO = new PollDTO();

        PollDTO result = singleVoteStrategy.applyVotingRules(pollDTO);

        assertSame(pollDTO, result);
    }
}