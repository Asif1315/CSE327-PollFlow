package com.pollflow.service;

import com.pollflow.dto.NotificationDTO;
import com.pollflow.entity.Notification;
import com.pollflow.entity.User;
import com.pollflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public Page<NotificationDTO> getNotifications(Pageable pageable) {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToDTO);
    }

    public Page<NotificationDTO> getUnreadNotifications(Pageable pageable) {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToDTO);
    }

    public long getUnreadCount() {
        User user = userService.getCurrentUser();
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        User user = userService.getCurrentUser();
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setIsRead(true);
        return mapToDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead() {
        User user = userService.getCurrentUser();
        Page<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(
                user.getId(), Pageable.unpaged());
        
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread.getContent());
    }

    public void createNotification(User user, String title, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .build();
        
        notificationRepository.save(notification);
    }

    public Page<NotificationDTO> getAdminNotifications(Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userService.getCurrentUser().getId(), pageable)
                .map(this::mapToDTO);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
