package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 统治者避难所公共能量（燃料）库存（与玩家个人 {@link PlayerEnergyStock} 分离）。
 */
@Entity
@Table(name = "shelter_energy_stock")
public class ShelterEnergyStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "item_key", nullable = false, unique = true, length = 64)
    private String itemKey;

    @Column(nullable = false)
    private Integer quantity = 0;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
