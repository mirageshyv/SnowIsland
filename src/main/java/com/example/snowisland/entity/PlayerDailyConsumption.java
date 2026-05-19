package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_daily_consumption")
public class PlayerDailyConsumption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "required_food_units", nullable = false)
    private Integer requiredFoodUnits = 2;

    @Column(name = "required_fuel_kg", nullable = false)
    private Integer requiredFuelKg = 15;

    @Column(name = "consumed_food_units", nullable = false)
    private Integer consumedFoodUnits = 0;

    @Column(name = "consumed_fuel_kg", nullable = false)
    private Integer consumedFuelKg = 0;

    @Column(name = "fuel_from_wood_kg", nullable = false)
    private Integer fuelFromWoodKg = 0;

    @Column(name = "fuel_from_fuel_kg", nullable = false)
    private Integer fuelFromFuelKg = 0;

    @Column(nullable = false)
    private Boolean submitted = false;

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

    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }

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

    public Boolean getSubmitted() { return submitted; }
    public void setSubmitted(Boolean submitted) { this.submitted = submitted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
