package com.SmartGG.SmartGG_backend.configuration;

import org.springframework.stereotype.Component;

import io.github.cdimascio.dotenv.Dotenv;

@Component
public class riotConfig {

    private final String apiKey;

    public riotConfig(Dotenv dotenv) {
        this.apiKey = dotenv.get("RIOT_API_KEY");
        if (this.apiKey == null) {
            throw new IllegalStateException("RIOT_API_KEY não definido no .env!");
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
