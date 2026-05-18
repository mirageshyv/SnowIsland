package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_daily_labor")
public class ShelterDailyLabor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "build_value", nullable = false)
    private Integer buildValue = 0;

    @Column(name = "is_exploited", nullable = false)
    private Boolean exploited = false;

    @Column(name = "is_escaped", nullable = false)
    private Boolean escaped = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Integer getBuildValue() {
        return buildValue;
    }

    public void setBuildValue(Integer buildValue) {
        this.buildValue = buildValue;
    }

    public Boolean getExploited() {
        return exploited;
    }

    public void setExploited(Boolean exploited) {
        this.exploited = exploited;
    }

    public Boolean getEscaped() {
        return escaped;
    }

    public void setEscaped(Boolean escaped) {
        this.escaped = escaped;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
