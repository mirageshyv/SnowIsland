package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_state")
public class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "current_day", nullable = false)
    private Integer currentDay = 1;

    @Column(name = "current_phase", nullable = false, length = 20)
    private String currentPhase = "DAY";

    @Column(name = "is_game_over")
    private Boolean isGameOver = false;

    @Column(name = "catastrophe_triggered")
    private Boolean catastropheTriggered = false;

    @Column(name = "extra_card_due")
    private Boolean extraCardDue = false;

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

    public Integer getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(Integer currentDay) {
        this.currentDay = currentDay;
    }

    public String getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(String currentPhase) {
        this.currentPhase = currentPhase;
    }

    public Boolean getIsGameOver() {
        return isGameOver;
    }

    public void setIsGameOver(Boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    public Boolean getCatastropheTriggered() {
        return catastropheTriggered;
    }

    public void setCatastropheTriggered(Boolean catastropheTriggered) {
        this.catastropheTriggered = catastropheTriggered;
    }

    public Boolean getExtraCardDue() {
        return extraCardDue;
    }

    public void setExtraCardDue(Boolean extraCardDue) {
        this.extraCardDue = extraCardDue;
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