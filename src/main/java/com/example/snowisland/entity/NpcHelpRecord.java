package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_help_record")
public class NpcHelpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "help_type", nullable = false, length = 50)
    private String helpType;

    @Column(name = "help_name", nullable = false, length = 100)
    private String helpName;

    @Column(name = "cost_type", nullable = false, length = 20)
    private String costType;

    @Column(name = "cost_item_id", nullable = false)
    private Integer costItemId;

    @Column(name = "cost_quantity", nullable = false)
    private Integer costQuantity;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "result_description", columnDefinition = "TEXT")
    private String resultDescription;

    @Column(name = "favor_change")
    private Integer favorChange = 0;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public String getHelpType() { return helpType; }
    public void setHelpType(String helpType) { this.helpType = helpType; }
    public String getHelpName() { return helpName; }
    public void setHelpName(String helpName) { this.helpName = helpName; }
    public String getCostType() { return costType; }
    public void setCostType(String costType) { this.costType = costType; }
    public Integer getCostItemId() { return costItemId; }
    public void setCostItemId(Integer costItemId) { this.costItemId = costItemId; }
    public Integer getCostQuantity() { return costQuantity; }
    public void setCostQuantity(Integer costQuantity) { this.costQuantity = costQuantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getResultDescription() { return resultDescription; }
    public void setResultDescription(String resultDescription) { this.resultDescription = resultDescription; }
    public Integer getFavorChange() { return favorChange; }
    public void setFavorChange(Integer favorChange) { this.favorChange = favorChange; }
    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}