package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_governance")
public class LocationGovernance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "location_id", nullable = false)
    private Integer locationId;

    @Column(name = "location_name", length = 100)
    private String locationName;

    @Column(name = "actor_id")
    private Integer actorId;

    @Column(name = "actor_name", length = 50)
    private String actorName;

    @Column(name = "actor_kind", length = 10)
    private String actorKind = "player";

    @Column(name = "game_day", nullable = false)
    private Integer gameDay;

    @Column(name = "source_faction_action_id")
    private Integer sourceFactionActionId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public Integer getActorId() { return actorId; }
    public void setActorId(Integer actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getActorKind() { return actorKind; }
    public void setActorKind(String actorKind) { this.actorKind = actorKind; }
    public Integer getGameDay() { return gameDay; }
    public void setGameDay(Integer gameDay) { this.gameDay = gameDay; }
    public Integer getSourceFactionActionId() { return sourceFactionActionId; }
    public void setSourceFactionActionId(Integer sourceFactionActionId) { this.sourceFactionActionId = sourceFactionActionId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
