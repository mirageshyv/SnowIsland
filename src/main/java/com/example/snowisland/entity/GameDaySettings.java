package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_day_settings")
public class GameDaySettings {

    @Id
    @Column(name = "game_day")
    private Integer gameDay;

    @Column(name = "required_food_units", nullable = false)
    private Integer requiredFoodUnits = 2;

    @Column(name = "required_fuel_kg", nullable = false)
    private Integer requiredFuelKg = 15;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }

    public Integer getRequiredFoodUnits() { return requiredFoodUnits; }
    public void setRequiredFoodUnits(Integer requiredFoodUnits) { this.requiredFoodUnits = requiredFoodUnits; }

    public Integer getRequiredFuelKg() { return requiredFuelKg; }
    public void setRequiredFuelKg(Integer requiredFuelKg) { this.requiredFuelKg = requiredFuelKg; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
