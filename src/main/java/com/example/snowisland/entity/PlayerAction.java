package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_action")
public class PlayerAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "player_name", length = 50)
    private String playerName;

    @Column(name = "player_faction", length = 20)
    private String playerFaction;

    @Column(name = "action_slot", nullable = false)
    private Integer actionSlot;

    @Column(name = "action_type", nullable = false, length = 30)
    private String actionType;

    @Column(name = "target_id")
    private Integer targetId;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "npc_id")
    private Integer npcId;

    @Column(name = "npc_name", length = 50)
    private String npcName;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ActionStatus status = ActionStatus.pending;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ActionStatus { pending, feedbacked }

    public enum ActionType {
        go_location, investigate_player, produce, use_trait, use_profession, hide
    }

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); updatedAt = LocalDateTime.now(); }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    public PlayerAction() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getPlayerFaction() { return playerFaction; }
    public void setPlayerFaction(String playerFaction) { this.playerFaction = playerFaction; }
    public Integer getActionSlot() { return actionSlot; }
    public void setActionSlot(Integer actionSlot) { this.actionSlot = actionSlot; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }
    public String getNpcName() { return npcName; }
    public void setNpcName(String npcName) { this.npcName = npcName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
