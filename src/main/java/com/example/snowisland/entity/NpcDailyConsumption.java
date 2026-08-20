package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "npc_daily_consumption", uniqueConstraints = {
        @UniqueConstraint(name = "uk_npc_day", columnNames = {"npc_id", "game_day"})
})
public class NpcDailyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "required_food_units", nullable = false)
    private Integer requiredFoodUnits = 2;

    @Column(name = "required_fuel_kg", nullable = false)
    private Integer requiredFuelKg = 25;

    @Column(name = "consumed_food_units", nullable = false)
    private Integer consumedFoodUnits = 0;

    @Column(name = "consumed_fuel_kg", nullable = false)
    private Integer consumedFuelKg = 0;

    @Column(name = "fuel_from_wood_kg", nullable = false)
    private Integer fuelFromWoodKg = 0;

    @Column(name = "fuel_from_fuel_kg", nullable = false)
    private Integer fuelFromFuelKg = 0;

    @Column(name = "requirements_met", nullable = false)
    private Boolean requirementsMet = false;

    @Column(name = "result_status", length = 50)
    private String resultStatus;

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

    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }

    public Integer getRequiredFoodUnits() { return requiredFoodUnits; }
    public void setRequiredFoodUnits(Integer requiredFoodUnits) { this.requiredFoodUnits = requiredFoodUnits; }

    public Integer getRequiredFuelKg() { return requiredFuelKg; }
    public void setRequiredFuelKg(Integer requiredFuelKg) { this.requiredFuelKg = requiredFuelKg; }

    public Integer getConsumedFoodUnits() { return consumedFoodUnits; }
    public void setConsumedFoodUnits(Integer consumedFoodUnits) { this.consumedFoodUnits = consumedFoodUnits; }

    public Integer getConsumedFuelKg() { return consumedFuelKg; }
    public void setConsumedFuelKg(Integer consumedFuelKg) { this.consumedFuelKg = consumedFuelKg; }

    public Integer getFuelFromWoodKg() { return fuelFromWoodKg; }
    public void setFuelFromWoodKg(Integer fuelFromWoodKg) { this.fuelFromWoodKg = fuelFromWoodKg; }

    public Integer getFuelFromFuelKg() { return fuelFromFuelKg; }
    public void setFuelFromFuelKg(Integer fuelFromFuelKg) { this.fuelFromFuelKg = fuelFromFuelKg; }

    public Boolean getRequirementsMet() { return requirementsMet; }
    public void setRequirementsMet(Boolean requirementsMet) { this.requirementsMet = requirementsMet; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
