package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "catastrophe_card")
public class CatastropheCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "card_number", nullable = false, unique = true)
    private Integer cardNumber;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "effect_type", length = 50)
    private String effectType;

    @Column(name = "effect_param1")
    private Integer effectParam1 = 0;

    @Column(name = "effect_param2")
    private Integer effectParam2 = 0;

    @Column(name = "effect_param3", length = 100)
    private String effectParam3;

    @Column(name = "is_unique")
    private Boolean isUnique = false;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(Integer cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEffectType() {
        return effectType;
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public Integer getEffectParam1() {
        return effectParam1;
    }

    public void setEffectParam1(Integer effectParam1) {
        this.effectParam1 = effectParam1;
    }

    public Integer getEffectParam2() {
        return effectParam2;
    }

    public void setEffectParam2(Integer effectParam2) {
        this.effectParam2 = effectParam2;
    }

    public String getEffectParam3() {
        return effectParam3;
    }

    public void setEffectParam3(String effectParam3) {
        this.effectParam3 = effectParam3;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
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