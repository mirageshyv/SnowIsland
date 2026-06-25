package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_help_config")
public class NpcHelpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "help_type", nullable = false, length = 50)
    private String helpType;

    @Column(name = "help_name", nullable = false, length = 100)
    private String helpName;

    @Column(name = "help_description", columnDefinition = "TEXT")
    private String helpDescription;

    @Column(name = "base_cost_type", nullable = false, length = 20)
    private String baseCostType;

    @Column(name = "base_cost_item_id", nullable = false)
    private Integer baseCostItemId;

    @Column(name = "base_cost_quantity", nullable = false)
    private Integer baseCostQuantity = 0;

    @Column(name = "cost_min_modifier", precision = 5, scale = 2)
    private Double costMinModifier = 0.80;

    @Column(name = "cost_max_modifier", precision = 5, scale = 2)
    private Double costMaxModifier = 1.20;

    @Column(name = "min_favor_level", nullable = false, length = 20)
    private String minFavorLevel = "neutral";

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(name = "success_rate", precision = 5, scale = 2)
    private Double successRate = 0.90;

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
    public String getHelpType() { return helpType; }
    public void setHelpType(String helpType) { this.helpType = helpType; }
    public String getHelpName() { return helpName; }
    public void setHelpName(String helpName) { this.helpName = helpName; }
    public String getHelpDescription() { return helpDescription; }
    public void setHelpDescription(String helpDescription) { this.helpDescription = helpDescription; }
    public String getBaseCostType() { return baseCostType; }
    public void setBaseCostType(String baseCostType) { this.baseCostType = baseCostType; }
    public Integer getBaseCostItemId() { return baseCostItemId; }
    public void setBaseCostItemId(Integer baseCostItemId) { this.baseCostItemId = baseCostItemId; }
    public Integer getBaseCostQuantity() { return baseCostQuantity; }
    public void setBaseCostQuantity(Integer baseCostQuantity) { this.baseCostQuantity = baseCostQuantity; }
    public Double getCostMinModifier() { return costMinModifier; }
    public void setCostMinModifier(Double costMinModifier) { this.costMinModifier = costMinModifier; }
    public Double getCostMaxModifier() { return costMaxModifier; }
    public void setCostMaxModifier(Double costMaxModifier) { this.costMaxModifier = costMaxModifier; }
    public String getMinFavorLevel() { return minFavorLevel; }
    public void setMinFavorLevel(String minFavorLevel) { this.minFavorLevel = minFavorLevel; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}