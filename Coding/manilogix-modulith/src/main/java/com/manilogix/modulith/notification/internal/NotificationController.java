package com.manilogix.modulith.notification.internal;
import com.manilogix.modulith.auth.CurrentUser;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/notifications")
class NotificationController {
 private final NotificationRepository repository;private final CurrentUser user;
 NotificationController(NotificationRepository repository,CurrentUser user){this.repository=repository;this.user=user;}
 record View(long id,long userId,long orderId,String kind,String message,Instant createdAt){}
 private View view(Notification n){return new View(n.id,n.userId,n.orderId,n.kind,n.message,n.createdAt);}
 @GetMapping List<View> own(){return repository.findByUserIdOrderByCreatedAtDesc(user.id()).stream().map(this::view).toList();}
 @GetMapping("/all") @PreAuthorize("hasRole('ADMIN')")
 List<View> all(){return repository.findAll().stream().map(this::view).toList();}
}

