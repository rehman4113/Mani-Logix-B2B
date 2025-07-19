package com.manilogix.notification.controller;

import com.manilogix.notification.model.Notification;
import com.manilogix.notification.model.NotificationType;
import com.manilogix.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public Notification sendNotification(@RequestBody Notification notification) {
        String userId = notification.getUserId();
        String message = notification.getMessage();
        NotificationType type = notification.getType();
        return notificationService.sendNotification(userId, message, type);
    }

    @GetMapping("/all")
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @GetMapping("/user/{id}")
    public List<Notification> getNotificationsByUser(@PathVariable("id") String userId) {
        return notificationService.getNotificationsByUserId(userId);
    }
}