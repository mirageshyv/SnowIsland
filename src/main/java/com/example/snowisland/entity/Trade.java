package com.example.snowisland.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trade")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "from_player_id", nullable = false)
    private Integer fromPlayerId;

    @Column(name = "to_player_id", nullable = false)
    private Integer toPlayerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status;

    private String remark;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "trade_id")
    private List<TradeItem> items = new ArrayList<>();

    public enum TradeStatus {
        pending, accepted, rejected, cancelled, completed
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

    public Integer getFromPlayerId() { return fromPlayerId; }
    public void setFromPlayerId(Integer fromPlayerId) { this.fromPlayerId = fromPlayerId; }

    public Integer getToPlayerId() { return toPlayerId; }
    public void setToPlayerId(Integer toPlayerId) { this.toPlayerId = toPlayerId; }

    public TradeStatus getStatus() { return status; }
    public void setStatus(TradeStatus status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<TradeItem> getItems() { return items; }
    public void setItems(List<TradeItem> items) { this.items = items; }
}
