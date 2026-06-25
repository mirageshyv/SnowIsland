package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_trade_record")
public class NpcTradeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "demand_items", columnDefinition = "TEXT")
    private String demandItems;

    @Column(name = "supply_items", columnDefinition = "TEXT")
    private String supplyItems;

    @Column(name = "favor_change")
    private Integer favorChange = 0;

    @Column(name = "trade_time")
    private LocalDateTime tradeTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        tradeTime = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }

    public String getDemandItems() { return demandItems; }
    public void setDemandItems(String demandItems) { this.demandItems = demandItems; }

    public String getSupplyItems() { return supplyItems; }
    public void setSupplyItems(String supplyItems) { this.supplyItems = supplyItems; }

    public Integer getFavorChange() { return favorChange; }
    public void setFavorChange(Integer favorChange) { this.favorChange = favorChange; }

    public LocalDateTime getTradeTime() { return tradeTime; }
    public void setTradeTime(LocalDateTime tradeTime) { this.tradeTime = tradeTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}