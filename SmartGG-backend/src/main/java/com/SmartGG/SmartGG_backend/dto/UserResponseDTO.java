package com.SmartGG.SmartGG_backend.dto;

public class UserResponseDTO {
    private Long id;
    private String email;
    private String gameName;
    private String tagLine;
    private String puuid;

    public UserResponseDTO(Long id, String email, String gameName, String tagLine, String puuid) {
        this.id = id;
        this.email = email;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getGameName() { return gameName; }
    public String getTagLine() { return tagLine; }
    public String getPuuid() { return puuid; }
}
