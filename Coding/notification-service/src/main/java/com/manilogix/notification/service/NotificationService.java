package com.manilogix.notification.service;

import com.manilogix.notification.model.Notification;
import com.manilogix.notification.model.NotificationType;
import com.manilogix.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository repository;

    public Notification sendNotification(String userId, String message, NotificationType type) {
        Notification notification = new Notification(userId, message, type, LocalDateTime.now());
        return repository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return repository.findAll();
    }

    public List<Notification> getNotificationsByUserId(String userId) {
        return repository.findByUserId(userId);
    }
}
