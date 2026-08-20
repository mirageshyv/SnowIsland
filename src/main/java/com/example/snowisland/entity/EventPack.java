package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_pack")
public class EventPack {

    public static final String PACK_BASE = "基础包";
    public static final String PACK_OLD_WORLD = "旧世界的末尾";
    public static final String PACK_FOURTH_ERA = "第四纪元";
    public static final String PACK_HIDDEN_DWELLING = "隐秘居所";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TINYINT(1) NOT NULL DEFAULT 0")
    private Boolean enabled = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "parent_id")
    private Integer parentId;

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

    /**
     * Map a source event number from exploration_events.txt to a built-in pack.
     * 1–60 基础包, 61–77 旧世界的末尾, 78–84 第四纪元, 85–89 隐秘居所.
     */
    public static String nameForEventNumber(Integer eventNumber) {
        if (eventNumber == null) {
            return PACK_BASE;
        }
        int n = eventNumber;
        if (n >= 85 && n <= 89) {
            return PACK_HIDDEN_DWELLING;
        }
        if (n >= 78 && n <= 84) {
            return PACK_FOURTH_ERA;
        }
        if (n >= 61 && n <= 77) {
            return PACK_OLD_WORLD;
        }
        return PACK_BASE;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
