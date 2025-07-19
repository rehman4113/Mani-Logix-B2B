//package com.manilogix.inventory.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/inventory/**").permitAll()
////
////                        .requestMatchers("/api/inventory/add", "/api/inventory/update/**", "/api/inventory/delete/**").hasAuthority("SUPPLIER")
////                        .requestMatchers("/api/inventory/all", "/api/inventory/**").hasAnyAuthority("SUPPLIER", "MANAGER", "ADMIN")
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}
package com.manilogix.inventory.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                                .requestMatchers("/api/inventory/**").permitAll()  // Auth endpoints

                        // SUPPLIER-only endpoints
//                        .requestMatchers("/api/inventory/add").hasAuthority("SUPPLIER")
//                        .requestMatchers("/api/inventory/update/**").hasAuthority("SUPPLIER")
//                        .requestMatchers("/api/inventory/delete/**").hasAuthority("SUPPLIER")
//
//                        // Endpoints accessible by SUPPLIER, MANAGER, ADMIN
//                        .requestMatchers("/api/inventory/all").hasAnyAuthority("SUPPLIER", "MANAGER", "ADMIN")
//                        .requestMatchers("/api/inventory/{id}").hasAnyAuthority("SUPPLIER", "MANAGER", "ADMIN")
//                        .requestMatchers("/api/inventory/by-category/**").hasAnyAuthority("SUPPLIER", "MANAGER", "ADMIN")
//
//                        // MANAGER only
//                        .requestMatchers("/api/inventory/low-stock").hasAuthority("MANAGER")
//
//                        // ADMIN only
//                        .requestMatchers("/api/inventory/by-supplier/**").hasAuthority("ADMIN")

                        // Any other requests require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

