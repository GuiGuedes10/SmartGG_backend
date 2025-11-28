package com.SmartGG.SmartGG_backend.configuration;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.SmartGG.SmartGG_backend.services.ChampionService;

import jakarta.annotation.PostConstruct;

@Component
public class ChampionDataInitializer {

    @Autowired
    private ChampionService championService;

    @PostConstruct
    public void init() throws Exception {
        String url = "https://ddragon.leagueoflegends.com/cdn/15.23.1/data/en_US/champion.json";
        URI uri = URI.create(url);
        String json;
        try (java.io.InputStream in = uri.toURL().openStream()) {
            json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        championService.saveChampionData(json);
        System.out.println("Champion data loaded into Redis!");
    }
}

