package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_exploration")
public class PlayerExploration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "player_name", length = 50)
    private String playerName;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "invest_points")
    private Integer investPoints = 0;

    @Column(name = "dice_result")
    private Integer diceResult = 0;

    @Column(name = "total_exploration_value")
    private Integer totalExplorationValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExplorationStatus status = ExplorationStatus.pending;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ExplorationStatus {
        pending, explored, settled
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }
    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public Integer getInvestPoints() { return investPoints; }
    public void setInvestPoints(Integer investPoints) { this.investPoints = investPoints; }
    public Integer getDiceResult() { return diceResult; }
    public void setDiceResult(Integer diceResult) { this.diceResult = diceResult; }
    public Integer getTotalExplorationValue() { return totalExplorationValue; }
    public void setTotalExplorationValue(Integer totalExplorationValue) { this.totalExplorationValue = totalExplorationValue; }
    public ExplorationStatus getStatus() { return status; }
    public void setStatus(ExplorationStatus status) { this.status = status; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}