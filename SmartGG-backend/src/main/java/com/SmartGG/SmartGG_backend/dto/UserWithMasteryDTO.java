package com.SmartGG.SmartGG_backend.dto;

import java.util.List;
import java.util.Map;

public class UserWithMasteryDTO {
    private UserResponseDTO user;
    private List<Map<String, Object>> masteries;

    public UserWithMasteryDTO(UserResponseDTO user, List<Map<String, Object>> masteries) {
        this.user = user;
        this.masteries = masteries;
    }

    public UserResponseDTO getUser() {
        return user;
    }

    public List<Map<String, Object>> getMasteries() {
        return masteries;
    }
}
