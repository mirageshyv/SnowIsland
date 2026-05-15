package com.example.snowisland.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ark_voyage")
public class ArkVoyage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "voyage_number", nullable = false)
    private Integer voyageNumber;

    @Column(name = "departure_day", nullable = false)
    private Integer departureDay;

    @Column(name = "planned_return_day")
    private Integer plannedReturnDay;

    @Column(name = "actual_return_day")
    private Integer actualReturnDay;

    @Column(name = "engine_count")
    private Integer engineCount = 0;

    @Column(name = "has_sail")
    private Boolean hasSail = false;

    @Column(name = "has_generator")
    private Boolean hasGenerator = false;

    @Column(name = "propeller_count")
    private Integer propellerCount = 0;

    @Column(name = "base_sail_days")
    private Integer baseSailDays;

    @Column(name = "final_sail_days")
    private Integer finalSailDays;

    @Column(name = "total_capacity")
    private Integer totalCapacity = 0;

    @Column(name = "passenger_count")
    private Integer passengerCount = 0;

    @Column(name = "passenger_list", columnDefinition = "TEXT")
    private String passengerList;

    @Column(name = "food_loaded", precision = 10, scale = 2)
    private BigDecimal foodLoaded = BigDecimal.ZERO;

    @Column(name = "fuel_loaded", precision = 10, scale = 2)
    private BigDecimal fuelLoaded = BigDecimal.ZERO;

    @Column(name = "sealant_loaded", precision = 10, scale = 2)
    private BigDecimal sealantLoaded = BigDecimal.ZERO;

    @Column(name = "wood_loaded", precision = 10, scale = 2)
    private BigDecimal woodLoaded = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "preparing";

    @Column(name = "current_day_offset")
    private Integer currentDayOffset = 0;

    @Column(name = "crisis_events", columnDefinition = "TEXT")
    private String crisisEvents;

    @Column(name = "crisis_count")
    private Integer crisisCount = 0;

    @Column(name = "has_navigator")
    private Boolean hasNavigator = false;

    @Column(name = "has_weather_watcher")
    private Boolean hasWeatherWatcher = false;

    @Column(name = "has_fisher")
    private Boolean hasFisher = false;

    @Column(name = "voyage_result", length = 50)
    private String voyageResult;

    @Column(name = "final_bonus")
    private Integer finalBonus = 0;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "departed_at")
    private LocalDateTime departedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "created_at", updatable = false)
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

    public Integer getVoyageNumber() { return voyageNumber; }
    public void setVoyageNumber(Integer voyageNumber) { this.voyageNumber = voyageNumber; }

    public Integer getDepartureDay() { return departureDay; }
    public void setDepartureDay(Integer departureDay) { this.departureDay = departureDay; }

    public Integer getPlannedReturnDay() { return plannedReturnDay; }
    public void setPlannedReturnDay(Integer plannedReturnDay) { this.plannedReturnDay = plannedReturnDay; }

    public Integer getActualReturnDay() { return actualReturnDay; }
    public void setActualReturnDay(Integer actualReturnDay) { this.actualReturnDay = actualReturnDay; }

    public Integer getEngineCount() { return engineCount; }
    public void setEngineCount(Integer engineCount) { this.engineCount = engineCount; }

    public Boolean getHasSail() { return hasSail; }
    public void setHasSail(Boolean hasSail) { this.hasSail = hasSail; }

    public Boolean getHasGenerator() { return hasGenerator; }
    public void setHasGenerator(Boolean hasGenerator) { this.hasGenerator = hasGenerator; }

    public Integer getPropellerCount() { return propellerCount; }
    public void setPropellerCount(Integer propellerCount) { this.propellerCount = propellerCount; }

    public Integer getBaseSailDays() { return baseSailDays; }
    public void setBaseSailDays(Integer baseSailDays) { this.baseSailDays = baseSailDays; }

    public Integer getFinalSailDays() { return finalSailDays; }
    public void setFinalSailDays(Integer finalSailDays) { this.finalSailDays = finalSailDays; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }

    public String getPassengerList() { return passengerList; }
    public void setPassengerList(String passengerList) { this.passengerList = passengerList; }

    public BigDecimal getFoodLoaded() { return foodLoaded; }
    public void setFoodLoaded(BigDecimal foodLoaded) { this.foodLoaded = foodLoaded; }

    public BigDecimal getFuelLoaded() { return fuelLoaded; }
    public void setFuelLoaded(BigDecimal fuelLoaded) { this.fuelLoaded = fuelLoaded; }

    public BigDecimal getSealantLoaded() { return sealantLoaded; }
    public void setSealantLoaded(BigDecimal sealantLoaded) { this.sealantLoaded = sealantLoaded; }

    public BigDecimal getWoodLoaded() { return woodLoaded; }
    public void setWoodLoaded(BigDecimal woodLoaded) { this.woodLoaded = woodLoaded; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getCurrentDayOffset() { return currentDayOffset; }
    public void setCurrentDayOffset(Integer currentDayOffset) { this.currentDayOffset = currentDayOffset; }

    public String getCrisisEvents() { return crisisEvents; }
    public void setCrisisEvents(String crisisEvents) { this.crisisEvents = crisisEvents; }

    public Integer getCrisisCount() { return crisisCount; }
    public void setCrisisCount(Integer crisisCount) { this.crisisCount = crisisCount; }

    public Boolean getHasNavigator() { return hasNavigator; }
    public void setHasNavigator(Boolean hasNavigator) { this.hasNavigator = hasNavigator; }

    public Boolean getHasWeatherWatcher() { return hasWeatherWatcher; }
    public void setHasWeatherWatcher(Boolean hasWeatherWatcher) { this.hasWeatherWatcher = hasWeatherWatcher; }

    public Boolean getHasFisher() { return hasFisher; }
    public void setHasFisher(Boolean hasFisher) { this.hasFisher = hasFisher; }

    public String getVoyageResult() { return voyageResult; }
    public void setVoyageResult(String voyageResult) { this.voyageResult = voyageResult; }

    public Integer getFinalBonus() { return finalBonus; }
    public void setFinalBonus(Integer finalBonus) { this.finalBonus = finalBonus; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getDepartedAt() { return departedAt; }
    public void setDepartedAt(LocalDateTime departedAt) { this.departedAt = departedAt; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
