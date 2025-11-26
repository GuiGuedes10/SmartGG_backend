package com.SmartGG.SmartGG_backend.Mysql.model;

import jakarta.persistence.*;

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

    public UserModel() {}

    public UserModel(String email, String password, String gameName, String tagLine, String puuid) {
        this.email = email;
        this.password = password;
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.puuid = puuid;
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
}
