package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "island_event")
public class IslandEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private Boolean triggered = false;

    @Column(length = 20)
    private String rarity = "normal";

    @Column(name = "event_difficulty", nullable = false)
    private Integer eventDifficulty = 5;

    @Column(name = "location_desc", columnDefinition = "TEXT")
    private String locationDesc;

    @Column(name = "lore_fragment", columnDefinition = "TEXT")
    private String loreFragment;

    @Column(name = "is_special", nullable = false)
    private Boolean isSpecial = false;

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
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getTriggered() { return triggered; }
    public void setTriggered(Boolean triggered) { this.triggered = triggered; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public Integer getEventDifficulty() { return eventDifficulty; }
    public void setEventDifficulty(Integer eventDifficulty) { this.eventDifficulty = eventDifficulty; }
    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }
    public String getLoreFragment() { return loreFragment; }
    public void setLoreFragment(String loreFragment) { this.loreFragment = loreFragment; }
    public Boolean getIsSpecial() { return isSpecial; }
    public void setIsSpecial(Boolean isSpecial) { this.isSpecial = isSpecial; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}