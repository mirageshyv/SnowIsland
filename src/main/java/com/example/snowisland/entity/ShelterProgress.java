package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 统治者避难所全局建造进度（单行，主键固定为 1）。
 */
@Entity
@Table(name = "shelter_progress")
public class ShelterProgress {

    public static final int SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private Integer id = SINGLETON_ID;

    @Column(name = "current_build_value", nullable = false)
    private Integer currentBuildValue = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = SINGLETON_ID;
        }
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
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

    public Integer getCurrentBuildValue() {
        return currentBuildValue;
    }

    public void setCurrentBuildValue(Integer currentBuildValue) {
        this.currentBuildValue = currentBuildValue;
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
