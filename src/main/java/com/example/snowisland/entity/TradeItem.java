package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_items")
public class TradeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "trade_id", nullable = false)
    private Integer tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_key")
    private String itemKey;

    private Integer quantity = 1;

    private String name;

    private String unit;

    @Transient
    private Integer kcalPerUnit;

    @Transient
    private Long lineKcal;

    @Enumerated(EnumType.STRING)
    private Direction direction;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ItemType {
        item, weapon, ammo, material, food, energy
    }

    public enum Direction {
        give, take
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getTradeId() { return tradeId; }
    public void setTradeId(Integer tradeId) { this.tradeId = tradeId; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getItemKey() { return itemKey; }
    public void setItemKey(String itemKey) { this.itemKey = itemKey; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getKcalPerUnit() { return kcalPerUnit; }
    public void setKcalPerUnit(Integer kcalPerUnit) { this.kcalPerUnit = kcalPerUnit; }

    public Long getLineKcal() { return lineKcal; }
    public void setLineKcal(Long lineKcal) { this.lineKcal = lineKcal; }

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
