package com.pollflow.service;

import com.pollflow.dto.AnalyticsDTO;
import com.pollflow.dto.UserDTO;
import com.pollflow.dto.PollDTO;
import com.pollflow.entity.User;
import com.pollflow.entity.User.UserStatus;
import com.pollflow.entity.Poll;
import com.pollflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PollRepository pollRepository;
    private final VoteRepository voteRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService notificationService;
    private final PollService pollService;

    public List<UserDTO> getPendingUsers() {
        return userRepository.findByStatus(UserStatus.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> searchUsers(String query) {
        return userRepository.searchUsers(query).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending");
        }

        user.setStatus(UserStatus.APPROVED);
        user.setRejectionReason(null);
        userRepository.save(user);

        notificationService.createNotification(user, 
                "Account Approved", 
                "Your account has been approved. You can now login to PollFlow.");

        return mapToDTO(user);
    }

    @Transactional
    public UserDTO rejectUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != UserStatus.PENDING) {
            throw new RuntimeException("User is not pending");
        }

        user.setStatus(UserStatus.REJECTED);
        user.setRejectionReason(reason);
        userRepository.save(user);

        notificationService.createNotification(user, 
                "Account Rejected", 
                "Your account has been rejected. Reason: " + reason);

        return mapToDTO(user);
    }

    public AnalyticsDTO getDashboardAnalytics() {
        long totalPolls = pollRepository.countActivePolls();
        long totalUsers = userRepository.count();
        long pendingUsers = userRepository.countByStatus(UserStatus.PENDING);
        long totalCategories = categoryRepository.count();
        long totalVotes = voteRepository.count();
        
        List<Poll> allPolls = pollRepository.findByIsDeletedFalse();
        long activePolls = allPolls.stream()
                .filter(p -> p.getPollType() == Poll.PollType.TIME_BASED && p.getEndTime() != null 
                    && p.getEndTime().isAfter(LocalDateTime.now()))
                .count();
        long openPolls = allPolls.stream()
                .filter(p -> p.getPollType() == Poll.PollType.OPEN || 
                    (p.getPollType() == Poll.PollType.TIME_BASED && 
                     (p.getEndTime() == null || p.getEndTime().isBefore(LocalDateTime.now()))))
                .count();
        activePolls = allPolls.size() - openPolls;
        
        List<PollDTO> topPolls = pollService.getAllPolls().stream()
                .sorted((a, b) -> Long.compare(b.getTotalVotes(), a.getTotalVotes()))
                .limit(5)
                .collect(Collectors.toList());

        return AnalyticsDTO.builder()
                .totalPolls(totalPolls)
                .totalUsers(totalUsers)
                .pendingUsers(pendingUsers)
                .totalCategories(totalCategories)
                .totalVotes(totalVotes)
                .activePolls(activePolls)
                .topPolls(topPolls)
                .build();
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .rejectionReason(user.getRejectionReason())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .mobile(user.getMobile())
                .gender(user.getGender())
                .age(user.getAge())
                .region(user.getRegion())
                .city(user.getCity())
                .postalCode(user.getPostalCode())
                .religion(user.getReligion())
                .address(user.getAddress())
                .maritalStatus(user.getMaritalStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}