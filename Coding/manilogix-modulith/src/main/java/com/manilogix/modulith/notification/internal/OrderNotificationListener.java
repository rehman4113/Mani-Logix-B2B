package com.manilogix.modulith.notification.internal;
import com.manilogix.modulith.ordering.OrderPlaced;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
@Component
class OrderNotificationListener {
 private final NotificationRepository notifications;
 OrderNotificationListener(NotificationRepository notifications) {this.notifications=notifications;}
 @ApplicationModuleListener
 void on(OrderPlaced event) {
  if (!notifications.existsByOrderIdAndKind(event.orderId(),"ORDER_PLACED")) {
   notifications.save(new Notification(event.customerId(),event.orderId(),"ORDER_PLACED","Order "+event.orderId()+" was placed"));
  }
 }
}

