package com.manilogix.modulith.auth.internal;
import jakarta.persistence.*;
@Entity @Table(name="app_users")
public class UserAccount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false, unique=true, length=190) String email;
 @Column(nullable=false) String passwordHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) Role role;
 protected UserAccount() {}
 UserAccount(String email, String passwordHash, Role role) { this.email=email; this.passwordHash=passwordHash; this.role=role; }
 public enum Role { ADMIN, MANAGER, SUPPLIER, CUSTOMER }
}

