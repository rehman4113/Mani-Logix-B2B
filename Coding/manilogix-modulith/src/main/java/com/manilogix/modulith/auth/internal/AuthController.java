package com.manilogix.modulith.auth.internal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/auth")
class AuthController {
 private final UserAccountRepository users;
 private final PasswordEncoder encoder;
 private final JwtEncoder jwt;
 AuthController(UserAccountRepository users, PasswordEncoder encoder, JwtEncoder jwt) { this.users=users; this.encoder=encoder; this.jwt=jwt; }
 record Signup(@NotBlank @Email String email, @NotBlank @Size(min=10) String password) {}
 record Login(@NotBlank @Email String email, @NotBlank String password) {}
 record Token(String accessToken, long expiresInSeconds) {}
 record CreateUser(@NotBlank @Email String email,@NotBlank @Size(min=12) String password,@NotNull UserAccount.Role role) {}
 record CreatedUser(long id,String email,UserAccount.Role role) {}
 @PostMapping("/users") @ResponseStatus(HttpStatus.CREATED) @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") @Transactional
 public CreatedUser createUser(@Valid @RequestBody CreateUser request) {
  String email=request.email().trim().toLowerCase(Locale.ROOT);
  if(users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
  UserAccount saved=users.save(new UserAccount(email,encoder.encode(request.password()),request.role()));
  return new CreatedUser(saved.id,saved.email,saved.role);
 }
 @PostMapping("/signup") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public void signup(@Valid @RequestBody Signup request) {
  String email=request.email().trim().toLowerCase(Locale.ROOT);
  if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
  users.save(new UserAccount(email,encoder.encode(request.password()),UserAccount.Role.CUSTOMER));
 }
 @PostMapping("/login")
 public Token login(@Valid @RequestBody Login request) {
  UserAccount user=users.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
   .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
  if (!encoder.matches(request.password(), user.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
  Instant now=Instant.now();
  JwtClaimsSet claims=JwtClaimsSet.builder().subject(user.id.toString()).issuedAt(now).expiresAt(now.plus(12,ChronoUnit.HOURS))
   .claim("roles",List.of(user.role.name())).build();
  String token=jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
  return new Token(token,43200);
 }
 @Bean ApplicationRunner bootstrapAdmin(@Value("${security.bootstrap-admin-email:}") String email, @Value("${security.bootstrap-admin-password:}") String password) {
  return args -> {
   if (!email.isBlank() && !password.isBlank() && users.count()==0) {
    if (password.length()<12) throw new IllegalArgumentException("Bootstrap admin password must be at least 12 characters");
    users.save(new UserAccount(email.trim().toLowerCase(Locale.ROOT),encoder.encode(password),UserAccount.Role.ADMIN));
   }
  };
 }
}

