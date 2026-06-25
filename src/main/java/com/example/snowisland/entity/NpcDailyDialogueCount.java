package com.example.snowisland.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "npc_daily_dialogue_count")
public class NpcDailyDialogueCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "dialogue_count", nullable = false)
    private Integer dialogueCount = 0;

    @Column(name = "last_game_day", nullable = false)
    private Integer lastGameDay = 0;

    @Column(name = "last_dialogue_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastDialogueTime;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getNpcId() {
        return npcId;
    }

    public void setNpcId(Integer npcId) {
        this.npcId = npcId;
    }

    public Integer getDialogueCount() {
        return dialogueCount;
    }

    public void setDialogueCount(Integer dialogueCount) {
        this.dialogueCount = dialogueCount;
    }

    public Integer getLastGameDay() {
        return lastGameDay;
    }

    public void setLastGameDay(Integer lastGameDay) {
        this.lastGameDay = lastGameDay;
    }

    public Date getLastDialogueTime() {
        return lastDialogueTime;
    }

    public void setLastDialogueTime(Date lastDialogueTime) {
        this.lastDialogueTime = lastDialogueTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
