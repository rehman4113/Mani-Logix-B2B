package com.manilogix.inventory.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity  // Enables @PreAuthorize and similar annotations
public class SecurityMethodConfig {
}
