package com.SmartGG.SmartGG_backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SpellService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SPELLS_HASH = "spells";

    public void saveSummonerSpellData(String json) {
        try {
            JsonNode root = objectMapper.readTree(json).get("data");

            root.fields().forEachRemaining(entry -> {
                JsonNode spell = entry.getValue();
                String key = spell.get("key").asText();

                SpellInfo info = new SpellInfo(
                        spell.get("name").asText(),
                        spell.get("description").asText(),
                        "https://ddragon.leagueoflegends.com/cdn/15.1.1/img/spell/" +
                                spell.get("image").get("full").asText()
                );

                redisService.putHash(SPELLS_HASH, key, info);
            });

            System.out.println("Summoner spells salvas no Redis com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao salvar Summoner Spells: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getSpellName(int spellId) {
        System.out.println("Teste, Fetching spell with ID: " + spellId);
        
        Object spellObj = redisService.getHash(SPELLS_HASH, String.valueOf(spellId));
        if (spellObj == null) System.out.println("Spell not found for ID: " + spellId);

        SpellInfo spell = objectMapper.convertValue(spellObj, SpellInfo.class);
        System.out.println("Retrieved spell: " + spell);
        return spell.getName();
    }

    public static class SpellInfo {
        private String name;
        private String description;
        private String image;

        public SpellInfo() {} 

        public SpellInfo(String name, String description, String image) {
            this.name = name;
            this.description = description;
            this.image = image;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getImage() { return image; }

        public void setName(String name) { this.name = name; }
        public void setDescription(String description) { this.description = description; }
        public void setImage(String image) { this.image = image; }
    }
}
