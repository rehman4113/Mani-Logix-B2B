package com.manilogix.modulith.supplier.internal;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="suppliers")
public class Supplier {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public String email;
 @Column(nullable=false) public Long ownerUserId;
 @Column(nullable=false) public boolean active=true;
 @Column(nullable=false) public Instant createdAt=Instant.now();
 protected Supplier() {}
 Supplier(String name,String email,Long ownerUserId) { this.name=name;this.email=email;this.ownerUserId=ownerUserId; }
}

