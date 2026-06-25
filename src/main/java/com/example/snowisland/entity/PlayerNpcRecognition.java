package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_npc_recognition")
public class PlayerNpcRecognition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "npc_id", nullable = false)
    private Integer npcId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "recognized_at")
    private LocalDateTime recognizedAt;

    @PrePersist
    protected void onCreate() {
        recognizedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPlayerId() { return playerId; }
    public void setPlayerId(Integer playerId) { this.playerId = playerId; }
    public Integer getNpcId() { return npcId; }
    public void setNpcId(Integer npcId) { this.npcId = npcId; }
    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }
    public LocalDateTime getRecognizedAt() { return recognizedAt; }
    public void setRecognizedAt(LocalDateTime recognizedAt) { this.recognizedAt = recognizedAt; }
}