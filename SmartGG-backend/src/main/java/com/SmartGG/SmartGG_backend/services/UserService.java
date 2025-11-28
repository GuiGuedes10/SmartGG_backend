package com.SmartGG.SmartGG_backend.services;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.SmartGG.SmartGG_backend.dto.UserWithMasteryDTO;

@Service
public class UserService {

    private final RestTemplate rest = new RestTemplate();
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ChampionService championService;

    private final riotConfig riotConfig;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, riotConfig riotConfig, ChampionService championService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.riotConfig = riotConfig;
        this.championService = championService;
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

    public UserWithMasteryDTO getUserById(Long id) {
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

        url = String.format(
            "https://br1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/%s/top?count=3&api_key=%s",
            user.getPuuid(),
            API_KEY
        );

        List<Map<String, Object>> masteryResponse = rest.getForObject(url, List.class);

        List<Map<String, Object>> masteryWithNames = new ArrayList<>();

        for (Map<String, Object> mastery : masteryResponse) {
            String championId = String.valueOf(mastery.get("championId"));
            String championName = championService.getChampionName(championId);

            Map<String, Object> item = new HashMap<>();
            item.put("championId", championId);
            item.put("championName", championName);
            item.put("championLevel", mastery.get("championLevel"));
            item.put("championPoints", mastery.get("championPoints"));
            masteryWithNames.add(item);
        }


        return new UserWithMasteryDTO(
            new UserResponseDTO(
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
            ),
            masteryWithNames
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

    public Map<String, Object> getChampionAverages(String puuid, String championId) {
    String API_KEY = riotConfig.getApiKey();

        String matchListUrl = String.format(
            "https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?queue=420&start=0&count=60&api_key=%s",
            puuid, API_KEY
        );

        List<String> matchIds = rest.getForObject(matchListUrl, List.class);
        if (matchIds == null || matchIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhuma partida encontrada");
        }

        int gamesPlayed = 0;
        double totalKills = 0;
        double totalDeaths = 0;
        double totalAssists = 0;
        double totalDamage = 0;
        double totalGold = 0;
        double totalCs = 0;

        for (String matchId : matchIds) {

            String matchUrl = String.format(
                "https://americas.api.riotgames.com/lol/match/v5/matches/%s?api_key=%s", 
                matchId, API_KEY
            );

            Map<String, Object> match = rest.getForObject(matchUrl, Map.class);
            if (match == null) continue;

            Map<String, Object> info = (Map<String, Object>) match.get("info");
            List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");

            Map<String, Object> player = participants.stream()
                .filter(p -> puuid.equals(p.get("puuid")))
                .findFirst()
                .orElse(null);

            if (player == null) continue;

            String playedChampionId = String.valueOf(player.get("championId"));

            if (!playedChampionId.equals(championId)) {
                continue;
            }

            gamesPlayed++;

            totalKills += ((Number) player.get("kills")).doubleValue();
            totalDeaths += ((Number) player.get("deaths")).doubleValue();
            totalAssists += ((Number) player.get("assists")).doubleValue();
            totalDamage += ((Number) player.get("totalDamageDealtToChampions")).doubleValue();
            totalGold += ((Number) player.get("goldEarned")).doubleValue();
            totalCs += ((Number) player.get("totalMinionsKilled")).doubleValue() 
                    + ((Number) player.get("neutralMinionsKilled")).doubleValue();
        }

        if (gamesPlayed < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Usuário precisa ter jogado pelo menos 10 partidas com esse campeão nas últimas 100 solo/duo");
        }

        Map<String, Object> averages = new HashMap<>();
        averages.put("gamesPlayed", gamesPlayed);
        averages.put("kills", totalKills / gamesPlayed);
        averages.put("deaths", totalDeaths / gamesPlayed);
        averages.put("assists", totalAssists / gamesPlayed);
        averages.put("kda", (totalKills + totalAssists) / Math.max(1, totalDeaths / gamesPlayed));
        averages.put("damage", totalDamage / gamesPlayed);
        averages.put("gold", totalGold / gamesPlayed);
        averages.put("cs", totalCs / gamesPlayed);

        return averages;
    }

}
