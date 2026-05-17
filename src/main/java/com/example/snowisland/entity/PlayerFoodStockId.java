package com.example.snowisland.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlayerFoodStockId implements Serializable {

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerFoodStockId that = (PlayerFoodStockId) o;
        return Objects.equals(playerId, that.playerId) && Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, itemId);
    }
}
