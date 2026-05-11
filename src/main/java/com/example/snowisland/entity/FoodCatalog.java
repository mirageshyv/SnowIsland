package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 全局食物种类定义（每单位大卡），库存见 {@link PlayerFoodStock}。
 */
@Entity
@Table(name = "food_catalog")
public class FoodCatalog {

    @Id
    @Column(name = "item_key", nullable = false, length = 64)
    private String itemKey;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 16)
    private String unit;

    @Column(name = "kcal_per_unit", nullable = false)
    private Integer kcalPerUnit;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getKcalPerUnit() {
        return kcalPerUnit;
    }

    public void setKcalPerUnit(Integer kcalPerUnit) {
        this.kcalPerUnit = kcalPerUnit;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
