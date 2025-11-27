package com.SmartGG.SmartGG_backend.Mysql.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "MysqlUser")
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private String gameName;

    private String tagLine;

    private String puuid;

    private Long iconId;

    private String level;

    private String tier;

    private String encryptedAccountId;

    @Column(name = "user_rank")
    private String rank;

    private Integer leaguePoints;


    public UserModel() {}

    public UserModel(String email, String password, String gameName, String tagLine, String puuid, Long iconId, String level, String tier, String rank, Integer leaguePoints, String encryptedAccountId) {
        this.email = email;
        this.password = password;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
        this.iconId = iconId;
        this.level = level;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.encryptedAccountId = encryptedAccountId;
    }
    public Long getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getGameName() {
        return gameName;
    }
    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
    public String getTagLine() {
        return tagLine;
    }
    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }
    public String getPuuid() {
        return puuid;
    }
    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }
    public Long getIconId() {
        return iconId;
    }
    public void setIconId(Long iconId) {
        this.iconId = iconId;
    }
    public String getLevel() {
        return level;
    }
    public void setLevel(String level) {
        this.level = level;
    }
    public String getTier() {
        return tier;
    }
    public void setTier(String tier) {
        this.tier = tier;
    }
    public String getRank() {
        return rank;
    }
    public void setRank(String rank) {
        this.rank = rank;
    }
    public Integer getLeaguePoints() {
        return leaguePoints;
    }
    public void setLeaguePoints(Integer leaguePoints) {
        this.leaguePoints = leaguePoints;
    }
    public String getEncryptedAccountId() {
        return encryptedAccountId;
    }
    public void setEncryptedAccountId(String encryptedAccountId) {
        this.encryptedAccountId = encryptedAccountId;
    }

}
