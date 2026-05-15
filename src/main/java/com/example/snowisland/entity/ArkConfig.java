package com.example.snowisland.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ark_config")
public class ArkConfig {

    public static final int SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private Integer id = SINGLETON_ID;

    @Column(name = "target_wood", precision = 10, scale = 2)
    private BigDecimal targetWood = new BigDecimal("250.00");

    @Column(name = "target_metal", precision = 10, scale = 2)
    private BigDecimal targetMetal = new BigDecimal("100.00");

    @Column(name = "target_asphalt", precision = 10, scale = 2)
    private BigDecimal targetAsphalt = new BigDecimal("100.00");

    @Column(name = "base_capacity")
    private Integer baseCapacity = 50;

    @Column(name = "daily_wood_limit", precision = 10, scale = 2)
    private BigDecimal dailyWoodLimit = new BigDecimal("30.00");

    @Column(name = "daily_metal_limit", precision = 10, scale = 2)
    private BigDecimal dailyMetalLimit = new BigDecimal("20.00");

    @Column(name = "daily_asphalt_limit", precision = 10, scale = 2)
    private BigDecimal dailyAsphaltLimit = new BigDecimal("20.00");

    @Column(name = "work_wood_per_unit", precision = 5, scale = 2)
    private BigDecimal workWoodPerUnit = new BigDecimal("5.00");

    @Column(name = "work_metal_per_unit", precision = 5, scale = 2)
    private BigDecimal workMetalPerUnit = new BigDecimal("5.00");

    @Column(name = "work_asphalt_per_unit", precision = 5, scale = 2)
    private BigDecimal workAsphaltPerUnit = new BigDecimal("5.00");

    @Column(name = "food_per_capacity")
    private Integer foodPerCapacity = 100;

    @Column(name = "fuel_per_capacity", precision = 5, scale = 2)
    private BigDecimal fuelPerCapacity = new BigDecimal("2.00");

    @Column(name = "sealant_per_capacity", precision = 5, scale = 2)
    private BigDecimal sealantPerCapacity = new BigDecimal("500.00");

    @Column(name = "sail_wood_per_capacity", precision = 5, scale = 2)
    private BigDecimal sailWoodPerCapacity = new BigDecimal("2.00");

    @Column(name = "sail_days_0_engine")
    private Integer sailDays0Engine = 10;

    @Column(name = "sail_days_1_engine")
    private Integer sailDays1Engine = 8;

    @Column(name = "sail_days_2_engine")
    private Integer sailDays2Engine = 6;

    @Column(name = "sail_days_3_engine")
    private Integer sailDays3Engine = 4;

    @Column(name = "sail_days_with_sail_0_engine")
    private Integer sailDaysWithSail0Engine = 10;

    @Column(name = "sail_days_with_sail_1_engine")
    private Integer sailDaysWithSail1Engine = 7;

    @Column(name = "sail_rope_required", precision = 10, scale = 2)
    private BigDecimal sailRopeRequired = new BigDecimal("100.00");

    @Column(name = "sail_canvas_required", precision = 10, scale = 2)
    private BigDecimal sailCanvasRequired = new BigDecimal("80.00");

    @Column(name = "current_game_day")
    private Integer currentGameDay = 1;

    @Column(name = "ark_status", length = 20)
    private String arkStatus = "building";

    @Column(name = "shortage_capacity_penalty")
    private Integer shortageCapacityPenalty = 3;

    @Column(name = "shortage_completion_penalty", precision = 4, scale = 2)
    private BigDecimal shortageCompletionPenalty = new BigDecimal("2.50");

    @Column(name = "initial_wood", precision = 10, scale = 2)
    private BigDecimal initialWood = new BigDecimal("10.00");

    @Column(name = "initial_metal", precision = 10, scale = 2)
    private BigDecimal initialMetal = new BigDecimal("20.00");

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

    public BigDecimal getTargetWood() { return targetWood; }
    public void setTargetWood(BigDecimal targetWood) { this.targetWood = targetWood; }

    public BigDecimal getTargetMetal() { return targetMetal; }
    public void setTargetMetal(BigDecimal targetMetal) { this.targetMetal = targetMetal; }

    public BigDecimal getTargetAsphalt() { return targetAsphalt; }
    public void setTargetAsphalt(BigDecimal targetAsphalt) { this.targetAsphalt = targetAsphalt; }

    public Integer getBaseCapacity() { return baseCapacity; }
    public void setBaseCapacity(Integer baseCapacity) { this.baseCapacity = baseCapacity; }

    public BigDecimal getDailyWoodLimit() { return dailyWoodLimit; }
    public void setDailyWoodLimit(BigDecimal dailyWoodLimit) { this.dailyWoodLimit = dailyWoodLimit; }

    public BigDecimal getDailyMetalLimit() { return dailyMetalLimit; }
    public void setDailyMetalLimit(BigDecimal dailyMetalLimit) { this.dailyMetalLimit = dailyMetalLimit; }

    public BigDecimal getDailyAsphaltLimit() { return dailyAsphaltLimit; }
    public void setDailyAsphaltLimit(BigDecimal dailyAsphaltLimit) { this.dailyAsphaltLimit = dailyAsphaltLimit; }

    public BigDecimal getWorkWoodPerUnit() { return workWoodPerUnit; }
    public void setWorkWoodPerUnit(BigDecimal workWoodPerUnit) { this.workWoodPerUnit = workWoodPerUnit; }

    public BigDecimal getWorkMetalPerUnit() { return workMetalPerUnit; }
    public void setWorkMetalPerUnit(BigDecimal workMetalPerUnit) { this.workMetalPerUnit = workMetalPerUnit; }

    public BigDecimal getWorkAsphaltPerUnit() { return workAsphaltPerUnit; }
    public void setWorkAsphaltPerUnit(BigDecimal workAsphaltPerUnit) { this.workAsphaltPerUnit = workAsphaltPerUnit; }

    public Integer getFoodPerCapacity() { return foodPerCapacity; }
    public void setFoodPerCapacity(Integer foodPerCapacity) { this.foodPerCapacity = foodPerCapacity; }

    public BigDecimal getFuelPerCapacity() { return fuelPerCapacity; }
    public void setFuelPerCapacity(BigDecimal fuelPerCapacity) { this.fuelPerCapacity = fuelPerCapacity; }

    public BigDecimal getSealantPerCapacity() { return sealantPerCapacity; }
    public void setSealantPerCapacity(BigDecimal sealantPerCapacity) { this.sealantPerCapacity = sealantPerCapacity; }

    public BigDecimal getSailWoodPerCapacity() { return sailWoodPerCapacity; }
    public void setSailWoodPerCapacity(BigDecimal sailWoodPerCapacity) { this.sailWoodPerCapacity = sailWoodPerCapacity; }

    public Integer getSailDays0Engine() { return sailDays0Engine; }
    public void setSailDays0Engine(Integer sailDays0Engine) { this.sailDays0Engine = sailDays0Engine; }

    public Integer getSailDays1Engine() { return sailDays1Engine; }
    public void setSailDays1Engine(Integer sailDays1Engine) { this.sailDays1Engine = sailDays1Engine; }

    public Integer getSailDays2Engine() { return sailDays2Engine; }
    public void setSailDays2Engine(Integer sailDays2Engine) { this.sailDays2Engine = sailDays2Engine; }

    public Integer getSailDays3Engine() { return sailDays3Engine; }
    public void setSailDays3Engine(Integer sailDays3Engine) { this.sailDays3Engine = sailDays3Engine; }

    public Integer getSailDaysWithSail0Engine() { return sailDaysWithSail0Engine; }
    public void setSailDaysWithSail0Engine(Integer sailDaysWithSail0Engine) { this.sailDaysWithSail0Engine = sailDaysWithSail0Engine; }

    public Integer getSailDaysWithSail1Engine() { return sailDaysWithSail1Engine; }
    public void setSailDaysWithSail1Engine(Integer sailDaysWithSail1Engine) { this.sailDaysWithSail1Engine = sailDaysWithSail1Engine; }

    public BigDecimal getSailRopeRequired() { return sailRopeRequired; }
    public void setSailRopeRequired(BigDecimal sailRopeRequired) { this.sailRopeRequired = sailRopeRequired; }

    public BigDecimal getSailCanvasRequired() { return sailCanvasRequired; }
    public void setSailCanvasRequired(BigDecimal sailCanvasRequired) { this.sailCanvasRequired = sailCanvasRequired; }

    public Integer getCurrentGameDay() { return currentGameDay; }
    public void setCurrentGameDay(Integer currentGameDay) { this.currentGameDay = currentGameDay; }

    public String getArkStatus() { return arkStatus; }
    public void setArkStatus(String arkStatus) { this.arkStatus = arkStatus; }

    public Integer getShortageCapacityPenalty() { return shortageCapacityPenalty; }
    public void setShortageCapacityPenalty(Integer shortageCapacityPenalty) { this.shortageCapacityPenalty = shortageCapacityPenalty; }

    public BigDecimal getShortageCompletionPenalty() { return shortageCompletionPenalty; }
    public void setShortageCompletionPenalty(BigDecimal shortageCompletionPenalty) { this.shortageCompletionPenalty = shortageCompletionPenalty; }

    public BigDecimal getInitialWood() { return initialWood; }
    public void setInitialWood(BigDecimal initialWood) { this.initialWood = initialWood; }

    public BigDecimal getInitialMetal() { return initialMetal; }
    public void setInitialMetal(BigDecimal initialMetal) { this.initialMetal = initialMetal; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
