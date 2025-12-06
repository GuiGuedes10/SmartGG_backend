package com.SmartGG.SmartGG_backend.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

    private final RestTemplate rest = new RestTemplate();

    @Autowired
    private SpellService spellService;

    @Autowired
    private RuneService runeService;

    
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
        System.out.println("Using Riot API Key: " + API_KEY);

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
            user.getPuuid(), API_KEY
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
            "https://br1.api.riotgames.com/lol/league/v4/entries/by-puuid/%s?api_key=%s",
            user.getPuuid(), API_KEY
        );

        List<Map<String, Object>> leagueEntries = rest.getForObject(url, List.class);

        Integer wins = 0;
        Integer losses = 0;
        double winRate = 0.0;

        if (leagueEntries != null && !leagueEntries.isEmpty()) {

            Map<String, Object> soloQ = leagueEntries.stream()
                .filter(entry -> "RANKED_SOLO_5x5".equals(entry.get("queueType")))
                .findFirst()
                .orElse(null);

            if (soloQ != null) {

                boolean updated = false;

                String currentTier = (String) soloQ.get("tier");
                String currentRank = (String) soloQ.get("rank");
                Integer currentLP = soloQ.get("leaguePoints") != null
                        ? Integer.valueOf(String.valueOf(soloQ.get("leaguePoints")))
                        : null;

                if (currentTier != null && !currentTier.equals(user.getTier())) {
                    user.setTier(currentTier);
                    updated = true;
                }

                if (currentRank != null && !currentRank.equals(user.getRank())) {
                    user.setRank(currentRank);
                    updated = true;
                }

                if (currentLP != null && !currentLP.equals(user.getLeaguePoints())) {
                    user.setLeaguePoints(currentLP);
                    updated = true;
                }

                if (updated) {
                    userRepository.save(user);
                }

                wins = soloQ.get("wins") != null ? Integer.parseInt(soloQ.get("wins").toString()) : 0;
                losses = soloQ.get("losses") != null ? Integer.parseInt(soloQ.get("losses").toString()) : 0;

                winRate = (wins + losses) > 0
                    ? (wins * 100.0 / (wins + losses))
                    : 0.0;
            }
        }
        url = String.format(
            "https://br1.api.riotgames.com/lol/champion-mastery/v4/champion-masteries/by-puuid/%s/top?count=3&api_key=%s",
            user.getPuuid(), API_KEY
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
            item.put("lastPlayTime", mastery.get("lastPlayTime"));

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
            masteryWithNames,
            wins,
            losses,
            winRate
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
            "https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?queue=420&start=0&count=30&api_key=%s",
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
        double totalGoldPerMinute = 0;
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
            if (!playedChampionId.equals(championId)) continue;

            gamesPlayed++;

            totalKills += ((Number) player.get("kills")).doubleValue();
            totalDeaths += ((Number) player.get("deaths")).doubleValue();
            totalAssists += ((Number) player.get("assists")).doubleValue();
            totalDamage += ((Number) player.get("totalDamageDealtToChampions")).doubleValue();
            totalCs += ((Number) player.get("totalMinionsKilled")).doubleValue() 
                    + ((Number) player.get("neutralMinionsKilled")).doubleValue();

            double gameDurationMinutes = ((Number) info.get("gameDuration")).doubleValue() / 60.0;
            double gold = ((Number) player.get("goldEarned")).doubleValue();
            double goldPerMinute = gameDurationMinutes > 0 ? gold / gameDurationMinutes : 0;
            totalGoldPerMinute += goldPerMinute;
        }

        if (gamesPlayed < 5) {
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
        averages.put("goldPerMinute", totalGoldPerMinute / gamesPlayed);
        averages.put("cs", totalCs / gamesPlayed);

        return averages;
    }


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Map<String, Object>> getLastMatches(String puuid, int start) {

        String apiKey = riotConfig.getApiKey();
        RestTemplate restTemplate = new RestTemplate();

        try {
            String url = "https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                    + puuid
                    + "/ids?start=" + start
                    + "&count=10"
                    + "&type=ranked"
                    + "&api_key=" + apiKey;

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            List<String> matchIds = mapper.readValue(response.getBody(), List.class);

            List<Map<String, Object>> userMatches = new ArrayList<>();

            for (String matchId : matchIds) {

            String redisKey = "userMatches:" + puuid; 

            Boolean exists = redisTemplate.opsForHash().hasKey(redisKey, matchId);
            if (Boolean.TRUE.equals(exists)) {
                Map<String, Object> cachedMatch =
                        (Map<String, Object>) redisTemplate.opsForHash().get(redisKey, matchId);

                userMatches.add(cachedMatch);
                continue;
            }


                String matchUrl = "https://americas.api.riotgames.com/lol/match/v5/matches/"
                        + matchId
                        + "?api_key=" + apiKey;

                ResponseEntity<String> matchResponse = restTemplate.exchange(
                        matchUrl,
                        HttpMethod.GET,
                        null,
                        String.class
                );

                Map<String, Object> matchData = mapper.readValue(matchResponse.getBody(), Map.class);

                Map<String, Object> info = (Map<String, Object>) matchData.get("info");
                List<Map<String, Object>> participants = (List<Map<String, Object>>) info.get("participants");

                Map<String, Object> user = participants.stream()
                        .filter(p -> puuid.equals(p.get("puuid")))
                        .findFirst()
                        .orElse(null);

                if (user != null) {
                    Map<String, Object> filtered = new HashMap<>();

                    Integer summoner1Id = (Integer) user.get("summoner1Id");
                    Integer summoner2Id = (Integer) user.get("summoner2Id");

                    filtered.put("summoner1Id", summoner1Id);
                    filtered.put("summoner2Id", summoner2Id);

                    String summoner1Name = spellService.getSpellName(summoner1Id);
                    String summoner2Name = spellService.getSpellName(summoner2Id);

                    filtered.put("summoner1Name", summoner1Name);
                    filtered.put("summoner2Name", summoner2Name);

                    filtered.put("matchId", matchId);
                    filtered.put("championName", user.get("championName"));
                    filtered.put("championId", user.get("championId"));
                    filtered.put("kills", user.get("kills"));
                    filtered.put("deaths", user.get("deaths"));
                    filtered.put("assists", user.get("assists"));
                    filtered.put("win", user.get("win"));
                    filtered.put("role", user.get("teamPosition"));
                    filtered.put("lane", user.get("lane"));
                    filtered.put("gameDuration", info.get("gameDuration"));
                    filtered.put("queueId", info.get("queueId"));
                    filtered.put("gameEndTimestamp", info.get("gameEndTimestamp"));

                    filtered.put("item0", user.get("item0"));
                    filtered.put("item1", user.get("item1"));
                    filtered.put("item2", user.get("item2"));
                    filtered.put("item3", user.get("item3"));
                    filtered.put("item4", user.get("item4"));
                    filtered.put("item5", user.get("item5"));
                    filtered.put("item6", user.get("item6"));

                    Map<String, Object> perks = (Map<String, Object>) user.get("perks");
                    if (perks != null) {
                        List<Map<String, Object>> styles = (List<Map<String, Object>>) perks.get("styles");

                        if (styles != null && !styles.isEmpty()) {

                            Map<String, Object> primary = styles.get(0);
                            filtered.put("primaryStyle", primary.get("style"));

                            List<Map<String, Object>> primarySelections =
                                    (List<Map<String, Object>>) primary.get("selections");

                            if (primarySelections != null && !primarySelections.isEmpty()) {
                                String primaryRuneId = String.valueOf(primarySelections.get(0).get("perk"));
                                filtered.put("primaryRune", primaryRuneId);

                                Map<String, String> primaryRuneObj = runeService.getRuneById(primaryRuneId);
                                if (primaryRuneObj != null) {
                                    filtered.put("primaryRuneName", primaryRuneObj.get("name"));
                                    filtered.put("primaryRuneIcon", primaryRuneObj.get("icon"));
                                }
                            }

                            if (styles.size() > 1) {
                                Map<String, Object> secondary = styles.get(1);
                                filtered.put("secondaryStyle", secondary.get("style"));

                                List<Map<String, Object>> secondarySelections =
                                        (List<Map<String, Object>>) secondary.get("selections");

                                if (secondarySelections != null && !secondarySelections.isEmpty()) {
                                    String secondaryRuneId = String.valueOf(secondarySelections.get(0).get("perk"));
                                    filtered.put("secondaryRune", secondaryRuneId);

                                    Map<String, String> secondaryRuneObj = runeService.getRuneById(secondaryRuneId);
                                    if (secondaryRuneObj != null) {
                                        filtered.put("secondaryRuneName", secondaryRuneObj.get("name"));
                                        filtered.put("secondaryRuneIcon", secondaryRuneObj.get("icon"));
                                    }
                                }
                            }
                        }
                    }

                    redisTemplate.opsForHash().put(redisKey, matchId, filtered);

                    userMatches.add(filtered);
                }
            }

            return userMatches;

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }



    public Map<String, Object> getUserAverages(String puuid) {
        String API_KEY = riotConfig.getApiKey();

        String matchListUrl = String.format(
            "https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/%s/ids?queue=420&start=0&count=30&api_key=%s",
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
        double totalGoldPerMinute = 0;
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

            gamesPlayed++;

            totalKills += ((Number) player.get("kills")).doubleValue();
            totalDeaths += ((Number) player.get("deaths")).doubleValue();
            totalAssists += ((Number) player.get("assists")).doubleValue();
            totalDamage += ((Number) player.get("totalDamageDealtToChampions")).doubleValue();
            totalCs += ((Number) player.get("totalMinionsKilled")).doubleValue() 
                    + ((Number) player.get("neutralMinionsKilled")).doubleValue();

            double gameDurationMinutes = ((Number) info.get("gameDuration")).doubleValue() / 60.0;
            double gold = ((Number) player.get("goldEarned")).doubleValue();
            double goldPerMinute = gameDurationMinutes > 0 ? gold / gameDurationMinutes : 0;
            totalGoldPerMinute += goldPerMinute;
        }

        if (gamesPlayed == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Nenhuma partida válida encontrada para o jogador.");
        }

        Map<String, Object> averages = new HashMap<>();
        averages.put("gamesPlayed", gamesPlayed);
        averages.put("kills", totalKills / gamesPlayed);
        averages.put("deaths", totalDeaths / gamesPlayed);
        averages.put("assists", totalAssists / gamesPlayed);
        averages.put("kda", (totalKills + totalAssists) / Math.max(1, totalDeaths));
        averages.put("damage", totalDamage / gamesPlayed);
        averages.put("goldPerMinute", totalGoldPerMinute / gamesPlayed); 
        averages.put("cs", totalCs / gamesPlayed);

        return averages;
    }


    public List<Map<String, Object>> getMatchDetails(String matchId) {

        String apiKey = riotConfig.getApiKey();
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String url = String.format("https://americas.api.riotgames.com/lol/match/v5/matches/%s?api_key=%s",
                    matchId, apiKey);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, String.class
            );

            Map<String, Object> match = mapper.readValue(response.getBody(), Map.class);
            Map<String, Object> info = (Map<String, Object>) match.get("info");

            List<Map<String, Object>> participants =
                    (List<Map<String, Object>>) info.get("participants");

            List<Map<String, Object>> result = new ArrayList<>();

            for (Map<String, Object> user : participants) {

                Map<String, Object> filtered = new HashMap<>();

                Integer summoner1Id = (Integer) user.get("summoner1Id");
                Integer summoner2Id = (Integer) user.get("summoner2Id");

                filtered.put("summoner1Id", summoner1Id);
                filtered.put("summoner2Id", summoner2Id);

                filtered.put("summoner1Name", spellService.getSpellName(summoner1Id));
                filtered.put("summoner2Name", spellService.getSpellName(summoner2Id));

                filtered.put("puuid", user.get("puuid"));
                filtered.put("matchId", matchId);
                filtered.put("championName", user.get("championName"));
                filtered.put("championId", user.get("championId"));
                filtered.put("kills", user.get("kills"));
                filtered.put("deaths", user.get("deaths"));
                filtered.put("assists", user.get("assists"));
                filtered.put("win", user.get("win"));
                filtered.put("role", user.get("teamPosition"));
                filtered.put("lane", user.get("lane"));

                filtered.put("gameDuration", info.get("gameDuration"));
                filtered.put("queueId", info.get("queueId"));
                filtered.put("gameEndTimestamp", info.get("gameEndTimestamp"));

                filtered.put("item0", user.get("item0"));
                filtered.put("item1", user.get("item1"));
                filtered.put("item2", user.get("item2"));
                filtered.put("item3", user.get("item3"));
                filtered.put("item4", user.get("item4"));
                filtered.put("item5", user.get("item5"));
                filtered.put("item6", user.get("item6"));

                Map<String, Object> perks = (Map<String, Object>) user.get("perks");
                if (perks != null) {
                    List<Map<String, Object>> styles =
                            (List<Map<String, Object>>) perks.get("styles");

                    if (styles != null && !styles.isEmpty()) {

                        Map<String, Object> primary = styles.get(0);
                        filtered.put("primaryStyle", primary.get("style"));

                        List<Map<String, Object>> primarySelections =
                                (List<Map<String, Object>>) primary.get("selections");

                        if (primarySelections != null && !primarySelections.isEmpty()) {
                            String primaryRuneId = String.valueOf(primarySelections.get(0).get("perk"));
                            filtered.put("primaryRune", primaryRuneId);

                            Map<String, String> r1 = runeService.getRuneById(primaryRuneId);
                            if (r1 != null) {
                                filtered.put("primaryRuneName", r1.get("name"));
                                filtered.put("primaryRuneIcon", r1.get("icon"));
                            }
                        }

                        if (styles.size() > 1) {
                            Map<String, Object> secondary = styles.get(1);
                            filtered.put("secondaryStyle", secondary.get("style"));

                            List<Map<String, Object>> secondarySelections =
                                    (List<Map<String, Object>>) secondary.get("selections");

                            if (secondarySelections != null && !secondarySelections.isEmpty()) {
                                String secondaryRuneId = String.valueOf(secondarySelections.get(0).get("perk"));
                                filtered.put("secondaryRune", secondaryRuneId);

                                Map<String, String> r2 = runeService.getRuneById(secondaryRuneId);
                                if (r2 != null) {
                                    filtered.put("secondaryRuneName", r2.get("name"));
                                    filtered.put("secondaryRuneIcon", r2.get("icon"));
                                }
                            }
                        }
                    }
                }

                result.add(filtered);
            }

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}