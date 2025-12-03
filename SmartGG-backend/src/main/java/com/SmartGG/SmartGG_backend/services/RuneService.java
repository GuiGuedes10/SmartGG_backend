package com.SmartGG.SmartGG_backend.services;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RuneService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String RUNES_HASH = "runes";
    private static final String DATA_DRAGON_BASE = "https://ddragon.leagueoflegends.com/cdn/";
    private static final String DATA_DRAGON_IMG = "https://ddragon.leagueoflegends.com/cdn/img/";


    public void loadRunes(String version) {
        try {
            String url = DATA_DRAGON_BASE + version + "/data/en_US/runesReforged.json";
            URI uri = URI.create(url);
            String json;

            try (InputStream in = uri.toURL().openStream()) {
                json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }

            JsonNode root = objectMapper.readTree(json);

            for (JsonNode pathNode : root) {
                JsonNode slots = pathNode.get("slots");
                if (slots != null) {
                    for (JsonNode slot : slots) {
                        JsonNode runes = slot.get("runes");
                        if (runes != null) {
                            for (JsonNode rune : runes) {
                                String id = rune.get("id").asText();
                                Map<String, String> info = new HashMap<>();
                                info.put("name", rune.get("name").asText());
                                info.put("icon", DATA_DRAGON_IMG + rune.get("icon").asText());

                                redisService.putHash(RUNES_HASH, id, info);
                            }
                        }
                    }
                }
            }

            System.out.println("Runas carregadas com sucesso no Redis!");
        } catch (Exception e) {
            System.out.println("Erro ao carregar runas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, String> getRuneById(String runeId) {
        Object obj = redisService.getHash(RUNES_HASH, runeId);
        if (obj == null) return null;

        return objectMapper.convertValue(obj, Map.class);
    }

    public void loadRunesFromJson(String json) {
    try {
        JsonNode root = objectMapper.readTree(json);

        for (JsonNode pathNode : root) {
            JsonNode slots = pathNode.get("slots");
            if (slots != null) {
                for (JsonNode slot : slots) {
                    JsonNode runes = slot.get("runes");
                    if (runes != null) {
                        for (JsonNode rune : runes) {
                            String id = rune.get("id").asText();
                            Map<String, String> info = new HashMap<>();
                            info.put("name", rune.get("name").asText());
                            info.put("icon", "https://ddragon.leagueoflegends.com/cdn/img/" + rune.get("icon").asText());

                            redisService.putHash("runes", id, info);
                        }
                    }
                }
            }
        }
        System.out.println("Runes loaded successfully in Redis!");
    } catch (Exception e) {
        e.printStackTrace();
    }
}
}