package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "lore_player_grant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "lore_slug"})
)
public class LorePlayerGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "lore_slug", nullable = false, length = 64)
    private String loreSlug;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getLoreSlug() {
        return loreSlug;
    }

    public void setLoreSlug(String loreSlug) {
        this.loreSlug = loreSlug;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
