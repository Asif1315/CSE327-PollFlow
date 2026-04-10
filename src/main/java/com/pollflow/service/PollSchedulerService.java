package com.pollflow.service;

import com.pollflow.entity.Favorite;
import com.pollflow.entity.Poll;
import com.pollflow.entity.User;
import com.pollflow.repository.FavoriteRepository;
import com.pollflow.repository.PollRepository;
import com.pollflow.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PollSchedulerService {
    private final PollRepository pollRepository;
    private final FavoriteRepository favoriteRepository;
    private final NotificationService notificationService;
    private final VoteRepository voteRepository;

    @Scheduled(fixedRate = 60000)
    public void checkEndedPolls() {
        try {
            List<Poll> endedPolls = pollRepository.findEndedTimeBasedPolls(LocalDateTime.now());
            
            for (Poll poll : endedPolls) {
                if (!poll.getIsDeleted()) {
                    notifyPollParticipants(poll);
                }
            }
        } catch (Exception e) {
            System.out.println("Error checking ended polls: " + e.getMessage());
        }
    }
    
    private void notifyPollParticipants(Poll poll) {
        try {
            List<Favorite> favorites = favoriteRepository.findByPollId(poll.getId());
            for (Favorite fav : favorites) {
                User owner = fav.getUser();
                notificationService.createNotification(owner,
                    "Poll Results Available",
                    "The poll '" + poll.getTitle() + "' has ended. Results are now available!");
            }
            
            List<User> voters = voteRepository.findDistinctUserByPollId(poll.getId());
            
            for (User voter : voters) {
                notificationService.createNotification(voter,
                    "Poll Results Available",
                    "The poll '" + poll.getTitle() + "' you voted on has ended. Results are now available!");
            }
        } catch (Exception e) {
            System.out.println("Error notifying poll participants: " + e.getMessage());
        }
    }
}