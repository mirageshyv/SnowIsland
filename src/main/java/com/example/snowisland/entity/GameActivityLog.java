package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_activity_log", indexes = {
        @Index(name = "idx_activity_created", columnList = "created_at"),
        @Index(name = "idx_activity_day", columnList = "game_day")
})
public class GameActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "player_id")
    private Integer playerId;

    @Column(name = "player_name", length = 50)
    private String playerName;

    @Column(name = "player_faction", length = 20)
    private String playerFaction;

    @Column(nullable = false, length = 24)
    private String category;

    @Column(nullable = false, length = 400)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (gameDay == null) {
            gameDay = 1;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getGameDay() {
        return gameDay;
    }

    public void setGameDay(Integer gameDay) {
        this.gameDay = gameDay;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerFaction() {
        return playerFaction;
    }

    public void setPlayerFaction(String playerFaction) {
        this.playerFaction = playerFaction;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
