package com.SmartGG.SmartGG_backend.controllers;

import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;
import com.SmartGG.SmartGG_backend.dto.LoginDTO;
import com.SmartGG.SmartGG_backend.dto.RegisterUserDTO;
import com.SmartGG.SmartGG_backend.dto.UserResponseDTO;
import com.SmartGG.SmartGG_backend.services.UserService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserModel register(@RequestBody RegisterUserDTO dto) {
        return service.register(dto);
    }

    @GetMapping
    public List<UserModel> getAllUsers() {
        return service.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }
    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginDTO dto) {
        return service.login(dto);
    }
}
