package com.SmartGG.SmartGG_backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // desativa CSRF para facilitar teste
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/users/**").permitAll() // libera todos endpoints /users/*
                .requestMatchers("/champion/**").permitAll() // libera todos endpoints /champion/*
                .requestMatchers("/spells/**").permitAll()
                .requestMatchers("/runes/**").permitAll()
                .anyRequest().authenticated() // outros endpoints exigem login
            )
            .httpBasic(); // habilita autenticação básica (opcional)

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
