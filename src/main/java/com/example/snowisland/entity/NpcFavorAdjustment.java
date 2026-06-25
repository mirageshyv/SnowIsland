package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_favor_adjustment")
public class NpcFavorAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "operator_id")
    private Integer operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(name = "old_value", nullable = false)
    private Integer oldValue;

    @Column(name = "new_value", nullable = false)
    private Integer newValue;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "adjustment_reason", columnDefinition = "TEXT")
    private String adjustmentReason;

    @Column(name = "adjustment_type", length = 20)
    private String adjustmentType = "manual";

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
    public Integer getOperatorId() { return operatorId; }
    public void setOperatorId(Integer operatorId) { this.operatorId = operatorId; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Integer getOldValue() { return oldValue; }
    public void setOldValue(Integer oldValue) { this.oldValue = oldValue; }
    public Integer getNewValue() { return newValue; }
    public void setNewValue(Integer newValue) { this.newValue = newValue; }
    public Integer getChangeAmount() { return changeAmount; }
    public void setChangeAmount(Integer changeAmount) { this.changeAmount = changeAmount; }
    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }
    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}