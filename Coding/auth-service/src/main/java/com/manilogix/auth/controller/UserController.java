package com.manilogix.auth.controller;

import com.manilogix.auth.model.User;
import com.manilogix.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    public AuthService authService;

//    public UserController(AuthService authService) {
//        this.authService = authService;
//    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.getUserFromToken(token));
    }
}