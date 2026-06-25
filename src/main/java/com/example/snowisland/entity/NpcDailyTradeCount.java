package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_daily_trade_count")
public class NpcDailyTradeCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "trade_count", nullable = false)
    private Integer tradeCount = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
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

    public Integer getTradeCount() { return tradeCount; }
    public void setTradeCount(Integer tradeCount) { this.tradeCount = tradeCount; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}