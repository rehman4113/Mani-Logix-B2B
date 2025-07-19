package com.manilogix.gateway.config;

import com.manilogix.gateway.filter.JwtAuthFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    public GlobalFilter jwtAuthFilter() {
        return new JwtAuthFilter();
    }
}