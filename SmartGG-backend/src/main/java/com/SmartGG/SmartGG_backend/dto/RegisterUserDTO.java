package com.SmartGG.SmartGG_backend.dto;

public class RegisterUserDTO {

    private String email;
    private String password;
    private String gameName;
    private String tagLine;
    private String puuid;

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPuuid(String puuid) {
        this.puuid = puuid;
    }
    public String getPuuid() {
        return puuid;
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
}