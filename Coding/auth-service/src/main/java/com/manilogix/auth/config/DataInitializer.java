package com.manilogix.auth.config;

import com.manilogix.auth.model.Role;
import com.manilogix.auth.model.RoleType;
import com.manilogix.auth.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            for (RoleType roleType : RoleType.values()) {
                if (!roleRepository.findByName(roleType).isPresent()) {
                    Role role = new Role();
                    role.setName(roleType);
                    roleRepository.save(role);
                }
            }
        };
    }
}
