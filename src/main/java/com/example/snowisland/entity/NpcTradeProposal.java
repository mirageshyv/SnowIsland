package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_trade_proposal", indexes = {
        @Index(name = "idx_npc_player_day_proposal", columnList = "npc_id, player_id, game_day, status"),
        @Index(name = "idx_npc_proposal_status", columnList = "npc_id, status")
})
public class NpcTradeProposal {

    public static final String STATUS_OPEN = "open";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_REJECTED = "rejected";
    public static final String STATUS_EXPIRED = "expired";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(nullable = false, length = 20)
    private String status = STATUS_OPEN;

    @Column(name = "give_items", columnDefinition = "TEXT")
    private String giveItems;

    @Column(name = "take_items", columnDefinition = "TEXT")
    private String takeItems;

    @Column(length = 255)
    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGiveItems() { return giveItems; }
    public void setGiveItems(String giveItems) { this.giveItems = giveItems; }

    public String getTakeItems() { return takeItems; }
    public void setTakeItems(String takeItems) { this.takeItems = takeItems; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
