package com.SmartGG.SmartGG_backend.configuration;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.SmartGG.SmartGG_backend.services.ChampionService;
import com.SmartGG.SmartGG_backend.services.SpellService;
import com.SmartGG.SmartGG_backend.services.RuneService;

import jakarta.annotation.PostConstruct;

@Component
public class ChampionDataInitializer {

    @Autowired
    private ChampionService championService;
    
    @Autowired
    private SpellService spellService;

    @Autowired
    private RuneService runeService;

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

        String spellsUrl = "https://ddragon.leagueoflegends.com/cdn/15.24.1/data/en_US/summoner.json";
        String spellsJson = readFromUrl(spellsUrl);

        spellService.saveSummonerSpellData(spellsJson);
        System.out.println("Summoner spells data loaded into Redis!");

        String runeUrl = "https://ddragon.leagueoflegends.com/cdn/15.24.1/data/en_US/runesReforged.json";
        String runeJson = readFromUrl(runeUrl);

        runeService.loadRunesFromJson(runeJson);
        System.out.println("Runes data loaded into Redis!");


    }

    private String readFromUrl(String url) throws Exception {
        URI uri = URI.create(url);
        try (InputStream in = uri.toURL().openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

