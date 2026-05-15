package com.example.snowisland.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ark_sail")
public class ArkSail {

    public static final int SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private Integer id = SINGLETON_ID;

    @Column(name = "is_built")
    private Boolean isBuilt = false;

    @Column(name = "built_by_player_id")
    private Integer builtByPlayerId;

    @Column(name = "built_at_game_day")
    private Integer builtAtGameDay;

    @Column(name = "effect_0_engine_days")
    private Integer effect0EngineDays = 10;

    @Column(name = "effect_1_engine_days")
    private Integer effect1EngineDays = 7;

    @Column(name = "required_rope", precision = 10, scale = 2)
    private BigDecimal requiredRope = new BigDecimal("100.00");

    @Column(name = "required_canvas", precision = 10, scale = 2)
    private BigDecimal requiredCanvas = new BigDecimal("80.00");

    @Column(name = "collected_rope", precision = 10, scale = 2)
    private BigDecimal collectedRope = BigDecimal.ZERO;

    @Column(name = "collected_canvas", precision = 10, scale = 2)
    private BigDecimal collectedCanvas = BigDecimal.ZERO;

    @Column(name = "construction_progress", precision = 5, scale = 2)
    private BigDecimal constructionProgress = BigDecimal.ZERO;

    @Column(name = "condition_status", length = 20)
    private String conditionStatus = "normal";

    @Column(name = "last_repaired_day")
    private Integer lastRepairedDay;

    @Column(name = "requires_work_action")
    private Boolean requiresWorkAction = true;

    @Column(name = "work_action_player_id")
    private Integer workActionPlayerId;

    @Column(name = "is_work_completed")
    private Boolean isWorkCompleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = SINGLETON_ID;
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Boolean getIsBuilt() { return isBuilt; }
    public void setIsBuilt(Boolean isBuilt) { this.isBuilt = isBuilt; }

    public Integer getBuiltByPlayerId() { return builtByPlayerId; }
    public void setBuiltByPlayerId(Integer builtByPlayerId) { this.builtByPlayerId = builtByPlayerId; }

    public Integer getBuiltAtGameDay() { return builtAtGameDay; }
    public void setBuiltAtGameDay(Integer builtAtGameDay) { this.builtAtGameDay = builtAtGameDay; }

    public Integer getEffect0EngineDays() { return effect0EngineDays; }
    public void setEffect0EngineDays(Integer effect0EngineDays) { this.effect0EngineDays = effect0EngineDays; }

    public Integer getEffect1EngineDays() { return effect1EngineDays; }
    public void setEffect1EngineDays(Integer effect1EngineDays) { this.effect1EngineDays = effect1EngineDays; }

    public BigDecimal getRequiredRope() { return requiredRope; }
    public void setRequiredRope(BigDecimal requiredRope) { this.requiredRope = requiredRope; }

    public BigDecimal getRequiredCanvas() { return requiredCanvas; }
    public void setRequiredCanvas(BigDecimal requiredCanvas) { this.requiredCanvas = requiredCanvas; }

    public BigDecimal getCollectedRope() { return collectedRope; }
    public void setCollectedRope(BigDecimal collectedRope) { this.collectedRope = collectedRope; }

    public BigDecimal getCollectedCanvas() { return collectedCanvas; }
    public void setCollectedCanvas(BigDecimal collectedCanvas) { this.collectedCanvas = collectedCanvas; }

    public BigDecimal getConstructionProgress() { return constructionProgress; }
    public void setConstructionProgress(BigDecimal constructionProgress) { this.constructionProgress = constructionProgress; }

    public String getConditionStatus() { return conditionStatus; }
    public void setConditionStatus(String conditionStatus) { this.conditionStatus = conditionStatus; }

    public Integer getLastRepairedDay() { return lastRepairedDay; }
    public void setLastRepairedDay(Integer lastRepairedDay) { this.lastRepairedDay = lastRepairedDay; }

    public Boolean getRequiresWorkAction() { return requiresWorkAction; }
    public void setRequiresWorkAction(Boolean requiresWorkAction) { this.requiresWorkAction = requiresWorkAction; }

    public Integer getWorkActionPlayerId() { return workActionPlayerId; }
    public void setWorkActionPlayerId(Integer workActionPlayerId) { this.workActionPlayerId = workActionPlayerId; }

    public Boolean getIsWorkCompleted() { return isWorkCompleted; }
    public void setIsWorkCompleted(Boolean isWorkCompleted) { this.isWorkCompleted = isWorkCompleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
