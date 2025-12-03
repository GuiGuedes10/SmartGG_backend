package com.SmartGG.SmartGG_backend.dto;

import java.util.List;
import java.util.Map;

public class UserWithMasteryDTO {
    private UserResponseDTO user;
    private List<Map<String, Object>> masteries;
    private Integer wins;
    private Integer losses;
    private Double winRate;

    public UserWithMasteryDTO(UserResponseDTO user, List<Map<String, Object>> masteries, Integer wins, Integer losses, Double winRate) {
        this.user = user;
        this.masteries = masteries;
        this.wins = wins;
        this.losses = losses;
        this.winRate = winRate;
    }

    public UserResponseDTO getUser() {
        return user;
    }

    public List<Map<String, Object>> getMasteries() {
        return masteries;
    }
    public Integer getWins() {
        return wins;
    }
    public Integer getLosses() {
        return losses;
    }
    public Double getWinRate() {
        return winRate;
    }
}
