package com.SmartGG.SmartGG_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class envConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./")       
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }
}