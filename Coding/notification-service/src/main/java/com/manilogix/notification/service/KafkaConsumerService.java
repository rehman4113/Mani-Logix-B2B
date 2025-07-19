//package com.manilogix.notification.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.manilogix.notification.model.NotificationType;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class KafkaConsumerService {
//
//    private final NotificationService notificationService;
//    private final ObjectMapper objectMapper;
//
//    public KafkaConsumerService(NotificationService notificationService) {
//        this.notificationService = notificationService;
//        this.objectMapper = new ObjectMapper();
//    }
//
//    @KafkaListener(topics = "order-events", groupId = "notification-group")
//    public void listenOrderEvents(String message) {
//        try {
//            JsonNode json = objectMapper.readTree(message);
//            String userId = json.has("userId") ? json.get("userId").asText() : "user123";
//            String msg = json.has("message") ? json.get("message").asText() : "Order update received";
//            NotificationType type = NotificationType.INFO;
//
//            notificationService.sendNotification(userId, msg, type);
//        } catch (Exception e) {
//            System.err.println("Failed to process order-event: " + e.getMessage());
//        }
//    }
//
//    @KafkaListener(topics = "inventory-alerts", groupId = "notification-group")
//    public void listenInventoryAlerts(String message) {
//        try {
//            JsonNode json = objectMapper.readTree(message);
//            String userId = json.has("userId") ? json.get("userId").asText() : "admin";
//            String msg = json.has("message") ? json.get("message").asText() : "Inventory alert triggered";
//            NotificationType type = NotificationType.ALERT;
//
//            notificationService.sendNotification(userId, msg, type);
//        } catch (Exception e) {
//            System.err.println("Failed to process inventory-alert: " + e.getMessage());
//        }
//    }
//}
