package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_weak")
    private Boolean isWeak = false;

    @Column(name = "is_overworked")
    private Boolean isOverworked = false;

    @Column(name = "is_injured")
    private Boolean isInjured = false;

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "skill_id")
    private Integer skillId;

    @Enumerated(EnumType.STRING)
    private Faction faction;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum Faction {
        统治者, 反叛者, 冒险者, 天灾使者, 平民
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

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsWeak() { return isWeak; }
    public void setIsWeak(Boolean isWeak) { this.isWeak = isWeak; }

    public Boolean getIsOverworked() { return isOverworked; }
    public void setIsOverworked(Boolean isOverworked) { this.isOverworked = isOverworked; }

    public Boolean getIsInjured() { return isInjured; }
    public void setIsInjured(Boolean isInjured) { this.isInjured = isInjured; }

    public Integer getJobId() { return jobId; }
    public void setJobId(Integer jobId) { this.jobId = jobId; }

    public Integer getSkillId() { return skillId; }
    public void setSkillId(Integer skillId) { this.skillId = skillId; }

    public Faction getFaction() { return faction; }
    public void setFaction(Faction faction) { this.faction = faction; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
