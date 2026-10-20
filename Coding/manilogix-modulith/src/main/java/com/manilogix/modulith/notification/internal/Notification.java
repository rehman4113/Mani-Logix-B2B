package com.manilogix.modulith.notification.internal;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="notifications",uniqueConstraints=@UniqueConstraint(columnNames={"order_id","kind"}))
class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public Long userId;
 @Column(nullable=false) public Long orderId;
 @Column(nullable=false) public String kind;
 @Column(nullable=false) public String message;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected Notification(){}
 Notification(long userId,long orderId,String kind,String message) {
  this.userId=userId;this.orderId=orderId;this.kind=kind;this.message=message;
 }
}

