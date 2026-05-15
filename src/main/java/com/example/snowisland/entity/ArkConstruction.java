package com.example.snowisland.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ark_construction")
public class ArkConstruction {

    public static final int SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private Integer id = SINGLETON_ID;

    @Column(name = "current_wood", nullable = false)
    private Integer currentWood = 0;

    @Column(name = "current_metal", nullable = false)
    private Integer currentMetal = 0;

    @Column(name = "current_sealant", nullable = false)
    private Integer currentSealant = 0;

    @Column(name = "engine_count", nullable = false)
    private Integer engineCount = 0;

    @Column(name = "propeller_count", nullable = false)
    private Integer propellerCount = 0;

    @Column(name = "generator_count", nullable = false)
    private Integer generatorCount = 0;

    @Column(name = "current_cargo_capacity", nullable = false)
    private Integer currentCargoCapacity = 0;

    @Column(name = "completion_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal completionPercentage = BigDecimal.ZERO;

    @Column(name = "has_sail", nullable = false)
    private Boolean hasSail = false;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCurrentWood() {
        return currentWood;
    }

    public void setCurrentWood(Integer currentWood) {
        this.currentWood = currentWood;
    }

    public Integer getCurrentMetal() {
        return currentMetal;
    }

    public void setCurrentMetal(Integer currentMetal) {
        this.currentMetal = currentMetal;
    }

    public Integer getCurrentSealant() {
        return currentSealant;
    }

    public void setCurrentSealant(Integer currentSealant) {
        this.currentSealant = currentSealant;
    }

    public Integer getEngineCount() {
        return engineCount;
    }

    public void setEngineCount(Integer engineCount) {
        this.engineCount = engineCount;
    }

    public Integer getPropellerCount() {
        return propellerCount;
    }

    public void setPropellerCount(Integer propellerCount) {
        this.propellerCount = propellerCount;
    }

    public Integer getGeneratorCount() {
        return generatorCount;
    }

    public void setGeneratorCount(Integer generatorCount) {
        this.generatorCount = generatorCount;
    }

    public Integer getCurrentCargoCapacity() {
        return currentCargoCapacity;
    }

    public void setCurrentCargoCapacity(Integer currentCargoCapacity) {
        this.currentCargoCapacity = currentCargoCapacity;
    }

    public BigDecimal getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(BigDecimal completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public Boolean getHasSail() {
        return hasSail;
    }

    public void setHasSail(Boolean hasSail) {
        this.hasSail = hasSail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
