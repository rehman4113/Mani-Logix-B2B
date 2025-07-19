package com.manilogix.auth.repository;

import com.manilogix.auth.model.Role;
import com.manilogix.auth.model.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);
}