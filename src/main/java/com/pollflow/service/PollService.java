package com.pollflow.service;

import com.pollflow.dto.*;
import com.pollflow.entity.*;
import com.pollflow.entity.User;
import com.pollflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollService {
    private final PollRepository pollRepository;
    private final CategoryRepository categoryRepository;
    private final VoteRepository voteRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public Page<PollDTO> getAllPolls(Pageable pageable, Long userId) {
        return pollRepository.findByIsDeletedFalse(pageable).map(poll -> mapToDTO(poll, userId));
    }

    public Page<PollDTO> getPollsByCategory(Long categoryId, Pageable pageable, Long userId) {
        return pollRepository.findByCategoryAndIsDeletedFalse(categoryId, pageable)
                .map(poll -> mapToDTO(poll, userId));
    }

    public Page<PollDTO> getPollsByUser(Long userId, Pageable pageable, Long currentUserId) {
        return pollRepository.findByCreatedByIdAndIsDeletedFalse(userId, pageable)
                .map(poll -> mapToDTO(poll, currentUserId));
    }

    public PollDTO getPollById(Long id, Long userId) {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (poll.getIsDeleted()) {
            throw new RuntimeException("Poll not found");
        }
        
        return mapToDTO(poll, userId);
    }

    public PollDTO getPollByIdForAdmin(Long id) {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (poll.getIsDeleted()) {
            throw new RuntimeException("Poll not found");
        }
        
        return mapToAdminDTO(poll);
    }

    @Transactional
    public PollDTO createPoll(PollRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Poll.PollType pollType = Poll.PollType.OPEN;
        if (request.getPollType() != null && "TIME_BASED".equals(request.getPollType())) {
            pollType = Poll.PollType.TIME_BASED;
        }

        Boolean hideResults = request.getHideResults() != null ? request.getHideResults() : false;

        Poll poll = pollRepository.save(Poll.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(category)
                .createdBy(currentUser)
                .pollType(pollType)
                .endTime(request.getEndTime())
                .hideResults(hideResults)
                .build());

        List<PollOption> options = request.getOptions().stream()
                .map(optText -> PollOption.builder()
                        .poll(poll)
                        .optionText(optText)
                        .build())
                .collect(Collectors.toList());

        poll.setOptions(options);
        pollRepository.save(poll);

        return mapToDTO(poll, currentUser.getId());
    }

    @Transactional
    public PollDTO createPoll(String title, String description, Long categoryId, List<String> options) {
        User currentUser = userService.getCurrentUser();
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Poll poll = pollRepository.save(Poll.builder()
                .title(title)
                .description(description)
                .category(category)
                .createdBy(currentUser)
                .build());

        List<PollOption> pollOptions = options.stream()
                .map(optText -> PollOption.builder()
                        .poll(poll)
                        .optionText(optText)
                        .build())
                .collect(Collectors.toList());

        poll.setOptions(pollOptions);
        pollRepository.save(poll);

        return mapToDTO(poll, currentUser.getId());
    }

    @Transactional
    public PollDTO updatePoll(Long id, PollRequest request) {
        try {
            Poll poll = pollRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Poll not found"));

            if (poll.getIsDeleted()) {
                throw new RuntimeException("Poll not found");
            }

            long existingVotes = voteRepository.countByPollId(id);
            if (existingVotes > 0) {
                throw new RuntimeException("Cannot update poll with existing votes");
            }

            System.out.println("updatePoll - pollType from request: " + request.getPollType());
            System.out.println("updatePoll - endTime from request: " + request.getEndTime());
            System.out.println("updatePoll - hideResults from request: " + request.getHideResults());

            poll.setTitle(request.getTitle());
            poll.setDescription(request.getDescription());
            
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                poll.setCategory(category);
            }

            if (request.getPollType() != null) {
                poll.setPollType(Poll.PollType.valueOf(request.getPollType()));
            }
            poll.setEndTime(request.getEndTime());
            
            if (request.getHideResults() != null) {
                poll.setHideResults(request.getHideResults());
            }

            while (!poll.getOptions().isEmpty()) {
                poll.getOptions().remove(0);
            }
            
            for (String optText : request.getOptions()) {
                PollOption newOption = PollOption.builder()
                        .poll(poll)
                        .optionText(optText)
                        .build();
                poll.getOptions().add(newOption);
            }
            
            return mapToDTO(pollRepository.save(poll), userService.getCurrentUser().getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error updating poll: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePoll(Long id) {
        Poll poll = pollRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        poll.setIsDeleted(true);
        pollRepository.save(poll);
    }

    @Transactional
    public PollDTO vote(Long pollId, VoteRequest request) {
        User user = userService.getCurrentUser();
        
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (poll.getIsDeleted()) {
            throw new RuntimeException("Poll not found");
        }

        if (voteRepository.existsByPollIdAndUserId(pollId, user.getId())) {
            throw new RuntimeException("You have already voted on this poll");
        }

        PollOption option = poll.getOptions().stream()
                .filter(opt -> opt.getId().equals(request.getOptionId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Option not found"));

        Vote vote = Vote.builder()
                .poll(poll)
                .user(user)
                .option(option)
                .build();

        voteRepository.save(vote);
        option.setVoteCount(option.getVoteCount() + 1);
        
        notifyFavoriteOwners(poll);
        
        return mapToDTO(pollRepository.save(poll), user.getId());
    }
    
    private void notifyFavoriteOwners(Poll poll) {
        try {
            List<Favorite> favorites = favoriteRepository.findByPollId(poll.getId());
            for (Favorite fav : favorites) {
                User owner = fav.getUser();
                if (!owner.getId().equals(userService.getCurrentUser().getId())) {
                    notificationService.createNotification(owner,
                        "New Vote on Favorite Poll",
                        "Someone voted on your favorite poll: " + poll.getTitle());
                }
            }
        } catch (Exception e) {
            System.out.println("Error notifying favorite owners: " + e.getMessage());
        }
    }

    public PollDTO getResults(Long pollId, Long userId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        return mapToDTO(poll, userId);
    }

    public Page<PollDTO> getFavorites(Pageable pageable) {
        User user = userService.getCurrentUser();
        return favoriteRepository.findByUserId(user.getId(), pageable)
                .map(fav -> mapToDTO(fav.getPoll(), user.getId()));
    }
    
    public List<PollDTO> getFavorites() {
        User user = userService.getCurrentUser();
        return favoriteRepository.findByUserId(user.getId(), Pageable.unpaged())
                .map(fav -> mapToDTOSafe(fav.getPoll(), user.getId()))
                .getContent();
    }

    @Transactional
    public void addFavorite(Long pollId) {
        User user = userService.getCurrentUser();
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new RuntimeException("Poll not found"));
        
        if (favoriteRepository.existsByUserIdAndPollId(user.getId(), pollId)) {
            throw new RuntimeException("Poll already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .poll(poll)
                .build();
        
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(Long pollId) {
        User user = userService.getCurrentUser();
        favoriteRepository.deleteByUserIdAndPollId(user.getId(), pollId);
    }

    public List<PollDTO> getAllPolls() {
        Long userId = getCurrentUserId();
        
        return pollRepository.findByIsDeletedFalse().stream()
                .map(poll -> mapToDTO(poll, userId))
                .collect(Collectors.toList());
    }

    public List<PollDTO> getAllPollsPublic() {
        return pollRepository.findByIsDeletedFalse().stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> getPollsByCategory(Long categoryId) {
        return pollRepository.findByCategoryId(categoryId).stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> getPollsByPollType(String pollType) {
        Poll.PollType type = Poll.PollType.valueOf(pollType);
        return pollRepository.findByPollType(type).stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> getPollsByCategoryAndPollType(Long categoryId, String pollType) {
        Poll.PollType type = Poll.PollType.valueOf(pollType);
        return pollRepository.findByCategoryIdAndPollType(categoryId, type).stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> searchPollsPublic(String query) {
        return pollRepository.searchPolls(query).stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> searchPollsByCategory(String query, Long categoryId) {
        return pollRepository.searchPollsByCategory(query, categoryId).stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
    }
    
    public List<PollDTO> getPollsSorted(String sortBy, Long categoryId, String pollType, String search) {
        List<Poll> polls;
        
        if (search != null && !search.isBlank()) {
            if (categoryId != null) {
                polls = pollRepository.searchPollsByCategory(search, categoryId);
            } else {
                polls = pollRepository.searchPolls(search);
            }
        } else if (categoryId != null && pollType != null && !pollType.isBlank()) {
            polls = pollRepository.findByCategoryIdAndPollType(categoryId, Poll.PollType.valueOf(pollType));
        } else if (categoryId != null) {
            polls = pollRepository.findByCategoryId(categoryId);
        } else if (pollType != null && !pollType.isBlank()) {
            polls = pollRepository.findByPollType(Poll.PollType.valueOf(pollType));
        } else {
            polls = pollRepository.findByIsDeletedFalse();
        }
        
        List<PollDTO> result = polls.stream()
                .map(poll -> mapToDTOSafe(poll, null))
                .collect(Collectors.toList());
        
        if (sortBy != null && !sortBy.isBlank()) {
            switch (sortBy) {
                case "mostVoted":
                    result.sort((a, b) -> Long.compare(b.getTotalVotes(), a.getTotalVotes()));
                    break;
                case "leastVoted":
                    result.sort((a, b) -> Long.compare(a.getTotalVotes(), b.getTotalVotes()));
                    break;
                case "oldest":
                    result.sort((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                        return a.getCreatedAt().compareTo(b.getCreatedAt());
                    });
                    break;
                case "newest":
                    result.sort((a, b) -> {
                        if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    });
                    break;
            }
        }
        
        return result;
    }

    private PollDTO mapToDTOSafe(Poll poll, Long userId) {
        try {
            return mapToDTO(poll, userId);
        } catch (Exception e) {
            System.err.println("Error mapping poll " + poll.getId() + ": " + e.getMessage());
            e.printStackTrace();
            long totalVotes = poll.getOptions().stream()
                    .mapToInt(PollOption::getVoteCount)
                    .sum();
            
            List<PollOptionDTO> options = poll.getOptions().stream()
                    .map(opt -> PollOptionDTO.builder()
                            .id(opt.getId())
                            .optionText(opt.getOptionText())
                            .voteCount(opt.getVoteCount())
                            .percentage(0.0)
                            .build())
                    .collect(Collectors.toList());
            
            String pollTypeStr = "OPEN";
            if (poll.getPollType() != null) {
                pollTypeStr = poll.getPollType().name();
            }
            
            java.time.LocalDateTime endTimeVal = poll.getEndTime();
            System.err.println("Poll " + poll.getId() + " - pollType: " + pollTypeStr + ", endTime: " + endTimeVal);

            boolean showResultsVal = true;
            if (poll.getPollType() == com.pollflow.entity.Poll.PollType.TIME_BASED && endTimeVal != null) {
                showResultsVal = java.time.LocalDateTime.now().isAfter(endTimeVal);
            }

            return PollDTO.builder()
                    .id(poll.getId())
                    .title(poll.getTitle())
                    .description(poll.getDescription())
                    .options(options)
                    .totalVotes(totalVotes)
                    .hasVoted(false)
                    .isFavorite(false)
                    .createdAt(poll.getCreatedAt())
                    .pollType(pollTypeStr)
                    .endTime(endTimeVal)
                    .showResults(showResultsVal)
                    .build();
        }
    }

    public List<PollDTO> searchPolls(String query) {
        Long userId = getCurrentUserId();
        
        return pollRepository.searchPolls(query).stream()
                .map(poll -> mapToDTO(poll, userId))
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        try {
            return userService.getCurrentUser().getId();
        } catch (Exception e) {
            return null;
        }
    }

    private PollDTO mapToDTO(Poll poll, Long userId) {
        long totalVotes = poll.getOptions().stream()
                .mapToInt(PollOption::getVoteCount)
                .sum();

        boolean hasVoted = userId != null && voteRepository.existsByPollIdAndUserId(poll.getId(), userId);
        boolean isFavorite = userId != null && favoriteRepository.existsByUserIdAndPollId(userId, poll.getId());

        final boolean showResults;
        final boolean isTimeBased = poll.getPollType() == Poll.PollType.TIME_BASED;
        final LocalDateTime endTime = poll.getEndTime();
        
        System.err.println("mapToDTO - Poll " + poll.getId() + ": pollType=" + poll.getPollType() + ", isTimeBased=" + isTimeBased + ", endTime=" + endTime);
        
        if (isTimeBased && endTime != null) {
            showResults = LocalDateTime.now().isAfter(endTime);
        } else {
            showResults = true;
        }

        final long displayTotalVotes = showResults ? totalVotes : 0L;

        List<PollOptionDTO> options = poll.getOptions().stream()
                .map(opt -> {
                    double percentage = displayTotalVotes > 0 ? (opt.getVoteCount() * 100.0 / displayTotalVotes) : 0;
                    return PollOptionDTO.builder()
                            .id(opt.getId())
                            .optionText(opt.getOptionText())
                            .voteCount(showResults ? opt.getVoteCount() : 0)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());

        return PollDTO.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .description(poll.getDescription())
                .categoryId(poll.getCategory() != null ? poll.getCategory().getId() : null)
                .categoryName(poll.getCategory() != null ? poll.getCategory().getName() : null)
                .createdById(poll.getCreatedBy() != null ? poll.getCreatedBy().getId() : null)
                .createdByName(poll.getCreatedBy() != null ? poll.getCreatedBy().getFullName() : null)
                .options(options)
                .totalVotes(displayTotalVotes)
                .hasVoted(hasVoted)
                .isFavorite(isFavorite)
                .createdAt(poll.getCreatedAt())
                .pollType(poll.getPollType() != null ? poll.getPollType().name() : "OPEN")
                .endTime(poll.getEndTime())
                .showResults(showResults)
                .build();
    }

    private PollDTO mapToAdminDTO(Poll poll) {
        long totalVotes = poll.getOptions().stream()
                .mapToInt(PollOption::getVoteCount)
                .sum();

        boolean isTimeBased = poll.getPollType() == Poll.PollType.TIME_BASED;
        LocalDateTime endTime = poll.getEndTime();
        
        boolean showResults = true;
        if (isTimeBased && endTime != null) {
            showResults = LocalDateTime.now().isAfter(endTime);
        }

        List<PollOptionDTO> options = poll.getOptions().stream()
                .map(opt -> {
                    double percentage = totalVotes > 0 ? (opt.getVoteCount() * 100.0 / totalVotes) : 0;
                    return PollOptionDTO.builder()
                            .id(opt.getId())
                            .optionText(opt.getOptionText())
                            .voteCount(opt.getVoteCount())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());

        return PollDTO.builder()
                .id(poll.getId())
                .title(poll.getTitle())
                .description(poll.getDescription())
                .categoryId(poll.getCategory() != null ? poll.getCategory().getId() : null)
                .categoryName(poll.getCategory() != null ? poll.getCategory().getName() : null)
                .createdById(poll.getCreatedBy() != null ? poll.getCreatedBy().getId() : null)
                .createdByName(poll.getCreatedBy() != null ? poll.getCreatedBy().getFullName() : null)
                .options(options)
                .totalVotes(totalVotes)
                .hasVoted(false)
                .isFavorite(false)
                .createdAt(poll.getCreatedAt())
                .pollType(poll.getPollType() != null ? poll.getPollType().name() : "OPEN")
                .endTime(poll.getEndTime())
                .showResults(showResults)
                .hideResults(poll.getHideResults() != null ? poll.getHideResults() : false)
                .build();
    }
}
