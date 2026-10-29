package com.helpdeskpro.helpdeskpro.controller;

import com.helpdeskpro.helpdeskpro.model.User;
import com.helpdeskpro.helpdeskpro.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(String name, String email, String password) {}

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User saved = userService.register(request.name(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}