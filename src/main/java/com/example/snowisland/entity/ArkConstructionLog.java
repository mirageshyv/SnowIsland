package com.example.snowisland.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ark_construction_log")
public class ArkConstructionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "player_name", length = 50)
    private String playerName;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "action_type", length = 30, nullable = false)
    private String actionType;

    @Column(name = "action_description")
    private String actionDescription;

    @Column(name = "wood_change", precision = 10, scale = 2)
    private BigDecimal woodChange = BigDecimal.ZERO;

    @Column(name = "metal_change", precision = 10, scale = 2)
    private BigDecimal metalChange = BigDecimal.ZERO;

    @Column(name = "asphalt_change", precision = 10, scale = 2)
    private BigDecimal asphaltChange = BigDecimal.ZERO;

    @Column(name = "work_units")
    private Integer workUnits = 0;

    @Column(name = "engine_installed")
    private Integer engineInstalled = 0;

    @Column(name = "propeller_installed")
    private Integer propellerInstalled = 0;

    @Column(name = "generator_installed")
    private Integer generatorInstalled = 0;

    @Column(name = "previous_stage", length = 30)
    private String previousStage;

    @Column(name = "current_stage", length = 30)
    private String currentStage;

    @Column(name = "previous_capacity")
    private Integer previousCapacity;

    @Column(name = "current_capacity")
    private Integer currentCapacity;

    @Column(name = "previous_completion", precision = 5, scale = 2)
    private BigDecimal previousCompletion;

    @Column(name = "current_completion", precision = 5, scale = 2)
    private BigDecimal currentCompletion;

    @Column(name = "capacity_change")
    private Integer capacityChange = 0;

    @Column(name = "completion_change", precision = 5, scale = 2)
    private BigDecimal completionChange = BigDecimal.ZERO;

    @Column(name = "sail_built")
    private Boolean sailBuilt = false;

    @Column(name = "sail_rope_used", precision = 10, scale = 2)
    private BigDecimal sailRopeUsed = BigDecimal.ZERO;

    @Column(name = "sail_canvas_used", precision = 10, scale = 2)
    private BigDecimal sailCanvasUsed = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }

    public BigDecimal getWoodChange() { return woodChange; }
    public void setWoodChange(BigDecimal woodChange) { this.woodChange = woodChange; }

    public BigDecimal getMetalChange() { return metalChange; }
    public void setMetalChange(BigDecimal metalChange) { this.metalChange = metalChange; }

    public BigDecimal getAsphaltChange() { return asphaltChange; }
    public void setAsphaltChange(BigDecimal asphaltChange) { this.asphaltChange = asphaltChange; }

    public Integer getWorkUnits() { return workUnits; }
    public void setWorkUnits(Integer workUnits) { this.workUnits = workUnits; }

    public Integer getEngineInstalled() { return engineInstalled; }
    public void setEngineInstalled(Integer engineInstalled) { this.engineInstalled = engineInstalled; }

    public Integer getPropellerInstalled() { return propellerInstalled; }
    public void setPropellerInstalled(Integer propellerInstalled) { this.propellerInstalled = propellerInstalled; }

    public Integer getGeneratorInstalled() { return generatorInstalled; }
    public void setGeneratorInstalled(Integer generatorInstalled) { this.generatorInstalled = generatorInstalled; }

    public String getPreviousStage() { return previousStage; }
    public void setPreviousStage(String previousStage) { this.previousStage = previousStage; }

    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }

    public Integer getPreviousCapacity() { return previousCapacity; }
    public void setPreviousCapacity(Integer previousCapacity) { this.previousCapacity = previousCapacity; }

    public Integer getCurrentCapacity() { return currentCapacity; }
    public void setCurrentCapacity(Integer currentCapacity) { this.currentCapacity = currentCapacity; }

    public BigDecimal getPreviousCompletion() { return previousCompletion; }
    public void setPreviousCompletion(BigDecimal previousCompletion) { this.previousCompletion = previousCompletion; }

    public BigDecimal getCurrentCompletion() { return currentCompletion; }
    public void setCurrentCompletion(BigDecimal currentCompletion) { this.currentCompletion = currentCompletion; }

    public Integer getCapacityChange() { return capacityChange; }
    public void setCapacityChange(Integer capacityChange) { this.capacityChange = capacityChange; }

    public BigDecimal getCompletionChange() { return completionChange; }
    public void setCompletionChange(BigDecimal completionChange) { this.completionChange = completionChange; }

    public Boolean getSailBuilt() { return sailBuilt; }
    public void setSailBuilt(Boolean sailBuilt) { this.sailBuilt = sailBuilt; }

    public BigDecimal getSailRopeUsed() { return sailRopeUsed; }
    public void setSailRopeUsed(BigDecimal sailRopeUsed) { this.sailRopeUsed = sailRopeUsed; }

    public BigDecimal getSailCanvasUsed() { return sailCanvasUsed; }
    public void setSailCanvasUsed(BigDecimal sailCanvasUsed) { this.sailCanvasUsed = sailCanvasUsed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
