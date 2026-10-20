package com.manilogix.modulith.notification.internal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
interface NotificationRepository extends JpaRepository<Notification,Long> {
 List<Notification> findByUserIdOrderByCreatedAtDesc(long userId);
 boolean existsByOrderIdAndKind(long orderId,String kind);
}

