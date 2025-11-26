package com.SmartGG.SmartGG_backend.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.SmartGG.SmartGG_backend.dto.RegisterUserDTO;
import com.SmartGG.SmartGG_backend.Mysql.repositories.UserRepository;
import com.SmartGG.SmartGG_backend.dto.UserResponseDTO;
import com.SmartGG.SmartGG_backend.dto.LoginDTO;
import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RestTemplate rest = new RestTemplate();
    private final String API_KEY = "RGAPI-07b683e5-2b6e-4b0c-89bf-58fbb5a5cc58";
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            passwordEncoder.encode(dto.getPassword()),
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

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
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
