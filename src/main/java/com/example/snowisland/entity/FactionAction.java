package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "faction_action")
public class FactionAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "player_name", length = 50)
    private String playerName;

    @Column(nullable = false, length = 20)
    private String faction;

    @Column(name = "action_type", nullable = false, length = 40)
    private String actionType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ActionStatus status = ActionStatus.pending;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay = 1;

    @Column(length = 20)
    private String phase = "day";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ActionStatus { pending, feedbacked }

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
    public String getFaction() { return faction; }
    public void setFaction(String faction) { this.faction = faction; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }
    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
