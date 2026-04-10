package com.pollflow.observer;

import com.pollflow.entity.Notification;
import com.pollflow.entity.User;
import com.pollflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserNotificationObserver implements NotificationObserver {
    private final NotificationRepository notificationRepository;
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void update(Notification notification) {
        if (user != null) {
            notification.setUser(user);
        }
        notificationRepository.save(notification);
    }

    @Override
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
}
