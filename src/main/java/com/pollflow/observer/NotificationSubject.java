package com.pollflow.observer;

import com.pollflow.entity.Notification;
import java.util.ArrayList;
import java.util.List;

public class NotificationSubject {
    private final List<NotificationObserver> observers = new ArrayList<>();

    public void attach(NotificationObserver observer) {
        observers.add(observer);
    }

    public void detach(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Notification notification) {
        for (NotificationObserver observer : observers) {
            observer.update(notification);
        }
    }
}
