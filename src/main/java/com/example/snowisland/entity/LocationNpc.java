package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "location_npc")
public class LocationNpc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 50)
    private String job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(name = "location_id", nullable = false)
    private Integer locationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "attitude_ruler", nullable = false, length = 5)
    private Attitude attitudeRuler = Attitude.忽视;

    @Enumerated(EnumType.STRING)
    @Column(name = "attitude_rebel", nullable = false, length = 5)
    private Attitude attitudeRebel = Attitude.忽视;

    @Enumerated(EnumType.STRING)
    @Column(name = "attitude_adventurer", nullable = false, length = 5)
    private Attitude attitudeAdventurer = Attitude.忽视;

    @Enumerated(EnumType.STRING)
    @Column(name = "attitude_scourge", nullable = false, length = 5)
    private Attitude attitudeScourge = Attitude.忽视;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Gender {
        男, 女
    }

    public enum Attitude {
        喜好, 厌恶, 忽视
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public LocationNpc() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public Integer getLocationId() { return locationId; }
    public void setLocationId(Integer locationId) { this.locationId = locationId; }

    public Attitude getAttitudeRuler() { return attitudeRuler; }
    public void setAttitudeRuler(Attitude attitudeRuler) { this.attitudeRuler = attitudeRuler; }

    public Attitude getAttitudeRebel() { return attitudeRebel; }
    public void setAttitudeRebel(Attitude attitudeRebel) { this.attitudeRebel = attitudeRebel; }

    public Attitude getAttitudeAdventurer() { return attitudeAdventurer; }
    public void setAttitudeAdventurer(Attitude attitudeAdventurer) { this.attitudeAdventurer = attitudeAdventurer; }

    public Attitude getAttitudeScourge() { return attitudeScourge; }
    public void setAttitudeScourge(Attitude attitudeScourge) { this.attitudeScourge = attitudeScourge; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
