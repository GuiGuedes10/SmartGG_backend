package com.SmartGG.SmartGG_backend.services;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.SmartGG.SmartGG_backend.Mysql.model.UserModel;
import com.SmartGG.SmartGG_backend.Mysql.repositories.UserRepository;
import com.SmartGG.SmartGG_backend.configuration.riotConfig;
import com.SmartGG.SmartGG_backend.dto.LoginDTO;
import com.SmartGG.SmartGG_backend.dto.RegisterUserDTO;
import com.SmartGG.SmartGG_backend.dto.UserResponseDTO;

@Service
public class UserService {

    private final RestTemplate rest = new RestTemplate();
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final riotConfig riotConfig;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, riotConfig riotConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.riotConfig = riotConfig;
    }

    @SuppressWarnings("unchecked")
    public UserModel register(RegisterUserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        String API_KEY = riotConfig.getApiKey();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", API_KEY);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String urlAccount = String.format(
            "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/%s/%s",
            dto.getGameName(),
            dto.getTagLine()
        );

        ResponseEntity<Map> accountResponseEntity = rest.exchange(urlAccount, HttpMethod.GET, entity, Map.class);
        Map<String, Object> accountResponse = accountResponseEntity.getBody();

        if (accountResponse == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao buscar dados da conta Riot");
        }

        String puuid = String.valueOf(accountResponse.get("puuid"));

        String urlSummoner = String.format(
            "https://br1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s",
            puuid,
            API_KEY
        );

        ResponseEntity<Map> summonerResponseEntity = rest.exchange(urlSummoner, HttpMethod.GET, null, Map.class);
        Map<String, Object> summonerResponse = summonerResponseEntity.getBody();

        if (summonerResponse == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Falha ao buscar dados do Summoner");
        }

        String encryptedAccountId = String.valueOf(summonerResponse.get("accountId"));
        Integer profileIconId = (Integer) summonerResponse.get("profileIconId");
        Integer summonerLevel = (Integer) summonerResponse.get("summonerLevel");

        String urlLeague = String.format(
            "https://br1.api.riotgames.com/lol/league/v4/entries/by-puuid/%s",
            puuid
        );

        ResponseEntity<List> leagueResponseEntity = rest.exchange(urlLeague, HttpMethod.GET, entity, List.class);
        List<Map<String, Object>> leagueEntries = leagueResponseEntity.getBody();

        String tier = null;
        String rank = null;
        Integer leaguePoints = null;

        if (leagueEntries != null && !leagueEntries.isEmpty()) {
            Map<String, Object> soloQueue = leagueEntries.stream()
                .filter(e -> "RANKED_SOLO_5x5".equals(e.get("queueType")))
                .findFirst()
                .orElse(leagueEntries.get(0));

            tier = soloQueue.get("tier") != null ? String.valueOf(soloQueue.get("tier")) : null;
            rank = soloQueue.get("rank") != null ? String.valueOf(soloQueue.get("rank")) : null;
            Object lpObj = soloQueue.get("leaguePoints");
            if (lpObj instanceof Number) {
                leaguePoints = ((Number) lpObj).intValue();
            } else if (lpObj != null) {
                try {
                    leaguePoints = Integer.valueOf(String.valueOf(lpObj));
                } catch (NumberFormatException ex) {
                    leaguePoints = null;
                }
            } else {
                leaguePoints = null;
            }
        }

        UserModel user = new UserModel();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setGameName(dto.getGameName());
        user.setTagLine(dto.getTagLine());
        user.setPuuid(puuid);
        user.setEncryptedAccountId(encryptedAccountId);
        user.setLevel(summonerLevel != null ? summonerLevel.toString() : null);
        user.setIconId(profileIconId != null ? profileIconId.longValue() : null);
        user.setTier(tier);
        user.setRank(rank);
        user.setLeaguePoints(leaguePoints);

        return userRepository.save(user);
    }


    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponseDTO getUserById(Long id) {
        UserModel user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

             String API_KEY = riotConfig.getApiKey();

        String url = String.format(
            "https://br1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s",
            user.getPuuid(),
            API_KEY
        );

        Map<String, Object> summonerResponse = rest.getForObject(url, Map.class);
        if (summonerResponse != null) {
            Long currentIconId = summonerResponse.get("profileIconId") != null
                ? Long.valueOf(String.valueOf(summonerResponse.get("profileIconId"))) : null;
            String currentLevel = summonerResponse.get("summonerLevel") != null
                ? String.valueOf(summonerResponse.get("summonerLevel")) : null;

            boolean updated = false;

            if (currentIconId != null && !currentIconId.equals(user.getIconId())) {
                user.setIconId(currentIconId);
                updated = true;
            }

            if (currentLevel != null && !currentLevel.equals(user.getLevel())) {
                user.setLevel(currentLevel);
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getGameName(),
            user.getTagLine(),
            user.getPuuid(),
            user.getIconId(),
            user.getLevel(),
            user.getTier(),
            user.getRank(),
            user.getLeaguePoints()
        );
    }

    public UserResponseDTO login(LoginDTO dto) {
        UserModel user = userRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha incorreta");
        }

        String API_KEY = riotConfig.getApiKey();

        String url = String.format(
            "https://br1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/%s?api_key=%s",
            user.getPuuid(),
            API_KEY
        );

        Map<String, Object> summonerResponse = rest.getForObject(url, Map.class);
        if (summonerResponse != null) {
            Long currentIconId = summonerResponse.get("profileIconId") != null
                ? Long.valueOf(String.valueOf(summonerResponse.get("profileIconId"))) : null;
            String currentLevel = summonerResponse.get("summonerLevel") != null
                ? String.valueOf(summonerResponse.get("summonerLevel")) : null;

            boolean updated = false;

            if (currentIconId != null && !currentIconId.equals(user.getIconId())) {
                user.setIconId(currentIconId);
                updated = true;
            }

            if (currentLevel != null && !currentLevel.equals(user.getLevel())) {
                user.setLevel(currentLevel);
                updated = true;
            }

            if (updated) {
                userRepository.save(user);
            }
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getGameName(),
            user.getTagLine(),
            user.getPuuid(),
            user.getIconId(),
            user.getLevel(),
            user.getTier(),
            user.getRank(),
            user.getLeaguePoints()
        );
    }
}
