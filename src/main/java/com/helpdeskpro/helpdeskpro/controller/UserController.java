package com.helpdeskpro.helpdeskpro.controller;

import com.helpdeskpro.helpdeskpro.model.Role;
import com.helpdeskpro.helpdeskpro.model.User;
import com.helpdeskpro.helpdeskpro.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/support")
    public List<User> supportUsers() {
        return userRepository.findByRole(Role.SUPPORT);
    }
}