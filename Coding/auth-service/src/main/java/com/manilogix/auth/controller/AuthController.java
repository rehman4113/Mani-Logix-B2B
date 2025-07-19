package com.manilogix.auth.controller;

import com.manilogix.auth.dto.LoginRequest;
import com.manilogix.auth.dto.SignupRequest;
import com.manilogix.auth.dto.JwtResponse;
import com.manilogix.auth.model.User;
import com.manilogix.auth.service.AuthService;
import com.manilogix.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
//    @PreAuthorize("hasAuthority('SUPPLIER')")
    public ResponseEntity<String> registerUser(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenRequest) {
        String token = tokenRequest.get("token");
        if (jwtService.validateToken(token)) {
            String email = jwtService.extractUsername(token);
            User user = authService.getUserByEmail(email);

            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "roles", user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .toList()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // ✅ NEW: Get all users (ADMIN only)
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

}
