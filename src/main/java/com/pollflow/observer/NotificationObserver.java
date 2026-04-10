package com.pollflow.observer;

import com.pollflow.entity.Notification;

public interface NotificationObserver {
    void update(Notification notification);
    Long getUserId();
}
