package com.SmartGG.SmartGG_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.SmartGG.SmartGG_backend.domain.User;
import com.SmartGG.SmartGG_backend.dto.RegisterUserDTO;
import com.SmartGG.SmartGG_backend.Mysql.repositories.UserRepository;
import com.SmartGG.SmartGG_backend.dto.UserResponseDTO;
import com.SmartGG.SmartGG_backend.dto.LoginDTO;
import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RestTemplate rest = new RestTemplate();
    private final String API_KEY = "RGAPI-07b683e5-2b6e-4b0c-89bf-58fbb5a5cc58";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserModel register(RegisterUserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
    }
        
    String url = String.format(
            "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s?api_key=%s",
            dto.getGameName(),
            dto.getTagLine(),
            API_KEY
        );

        Map<String, String> response = rest.getForObject(url, Map.class);

        UserModel user = new UserModel(
            dto.getEmail(),
            dto.getPassword(),
            response.get("gameName"),
            response.get("tagLine"),
            response.get("puuid")
        );

        return userRepository.save(user);
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponseDTO getUserById(Long id) {
        UserModel user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getGameName(),
            user.getTagLine(),
            user.getPuuid()
        );
    }

    public UserResponseDTO login(LoginDTO dto) {
        UserModel user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!user.getPassword().equals(dto.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getGameName(),
            user.getTagLine(),
            user.getPuuid()
        );
    }
}
