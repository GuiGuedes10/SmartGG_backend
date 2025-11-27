package com.SmartGG.SmartGG_backend.dto;

public class UserResponseDTO {
    private Long id;
    private String email;
    private String gameName;
    private String tagLine;
    private String puuid;
    private Long iconId;
    private String level;
    private String tier;
    private String rank;
    private Integer leaguePoints;


    public UserResponseDTO(Long id, String email, String gameName, String tagLine, String puuid, Long iconId, String level, String tier, String rank, Integer leaguePoints) {
        this.id = id;
        this.email = email;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
        this.iconId = iconId;
        this.level = level;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getGameName() { return gameName; }
    public String getTagLine() { return tagLine; }
    public String getPuuid() { return puuid; }
    public Long getIconId() { return iconId; }
    public String getLevel() { return level; }
    public String getTier() { return tier; }
    public String getRank() { return rank; }
    public Integer getLeaguePoints() { return leaguePoints; }
}
