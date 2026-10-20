package com.manilogix.modulith.auth.internal;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
@Configuration @EnableMethodSecurity
class SecurityConfiguration {
 @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
 @Bean JwtEncoder jwtEncoder(@Value("${security.jwt-secret}") String secret) {
  return new NimbusJwtEncoder(new ImmutableSecret<>(key(secret).getEncoded()));
 }
 @Bean JwtDecoder jwtDecoder(@Value("${security.jwt-secret}") String secret) {
  return NimbusJwtDecoder.withSecretKey(key(secret)).macAlgorithm(MacAlgorithm.HS256).build();
 }
 private SecretKeySpec key(String secret) {
  if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 UTF-8 bytes");
  return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
 }
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
  JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
  converter.setJwtGrantedAuthoritiesConverter(jwt -> {
   Collection<String> roles = jwt.getClaimAsStringList("roles");
   return roles == null ? java.util.List.of() : roles.stream().map(r -> (org.springframework.security.core.GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r)).toList();
  });
  return http.csrf(csrf -> csrf.disable())
    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/signup", "/api/auth/login").permitAll().anyRequest().authenticated())
    .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(converter))).build();
 }
}

