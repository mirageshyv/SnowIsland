package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_dialogue")
public class NpcDialogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "player_message", columnDefinition = "TEXT", nullable = false)
    private String playerMessage;

    @Column(name = "npc_reply", columnDefinition = "TEXT", nullable = false)
    private String npcReply;

    @Column(name = "favor_change")
    private Integer favorChange = 0;

    @Column(name = "dialogue_round", nullable = false)
    private Integer dialogueRound = 1;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public String getPlayerMessage() { return playerMessage; }
    public void setPlayerMessage(String playerMessage) { this.playerMessage = playerMessage; }
    public String getNpcReply() { return npcReply; }
    public void setNpcReply(String npcReply) { this.npcReply = npcReply; }
    public Integer getFavorChange() { return favorChange; }
    public void setFavorChange(Integer favorChange) { this.favorChange = favorChange; }
    public Integer getDialogueRound() { return dialogueRound; }
    public void setDialogueRound(Integer dialogueRound) { this.dialogueRound = dialogueRound; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}