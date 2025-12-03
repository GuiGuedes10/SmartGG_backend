package com.SmartGG.SmartGG_backend.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;
import com.SmartGG.SmartGG_backend.dto.LoginDTO;
import com.SmartGG.SmartGG_backend.dto.RegisterUserDTO;
import com.SmartGG.SmartGG_backend.dto.UserResponseDTO;
import com.SmartGG.SmartGG_backend.dto.UserWithMasteryDTO;
import com.SmartGG.SmartGG_backend.services.UserService;


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
    public UserWithMasteryDTO getUserById(@PathVariable Long id) {
        return service.getUserById(id);
    }
    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginDTO dto) {
        return service.login(dto);
    }

    @GetMapping("/champion/average/{puuid}")
    public Map<String, Object> getChampionAverages(@PathVariable String puuid, @RequestParam String championId) {
        return service.getChampionAverages(puuid, championId);
    }

    @GetMapping("/matches/{puuid}")
    public List<Map<String, Object>> getLastMatches(@PathVariable String puuid, @RequestParam int start) {
        return service.getLastMatches(puuid, start);
    }

    @GetMapping("/averages/{puuid}")
    public Map<String, Object> getUserAverages(@PathVariable String puuid) {
        return service.getUserAverages(puuid);
    }
    
    @GetMapping("/match/details/{matchId}")
    public List<Map<String, Object>> getMatchDetails(@PathVariable String matchId) {
        return service.getMatchDetails(matchId);
    }

}
