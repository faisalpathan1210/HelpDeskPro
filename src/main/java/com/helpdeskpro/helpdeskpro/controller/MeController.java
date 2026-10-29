package com.helpdeskpro.helpdeskpro.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "email", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );
    }
}