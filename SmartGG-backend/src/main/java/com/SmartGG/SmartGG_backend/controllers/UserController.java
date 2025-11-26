package com.SmartGG.SmartGG_backend.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import com.SmartGG.SmartGG_backend.domain.user;
import com.SmartGG.SmartGG_backend.services.testeService;
import com.SmartGG.SmartGG_backend.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @GetMapping
    public java.util.List<User> list() {
        return userService.findAll();
    }
}
