package com.SmartGG.SmartGG_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChampionService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String CHAMPION_HASH = "champions";

    public void saveChampionData(String json) throws Exception {
        JsonNode root = objectMapper.readTree(json).get("data");

        root.properties().forEach(entry -> {
            String id = entry.getValue().get("key").asText();  
            String name = entry.getValue().get("id").asText(); 
            redisService.putHash(CHAMPION_HASH, id, name);
        });
    }

    public String getChampionName(String championId) {
        Object name = redisService.getHash(CHAMPION_HASH, championId);
        return name != null ? name.toString() : "Champion not found";
    }
}
