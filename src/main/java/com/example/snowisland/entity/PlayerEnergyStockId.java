package com.example.snowisland.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PlayerEnergyStockId implements Serializable {

    @Column(name = "player_id", nullable = false)
    private Integer playerId;

    @Column(name = "item_key", nullable = false, length = 64)
    private String itemKey;

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public String getItemKey() {
        return itemKey;
    }

    public void setItemKey(String itemKey) {
        this.itemKey = itemKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEnergyStockId that = (PlayerEnergyStockId) o;
        return Objects.equals(playerId, that.playerId) && Objects.equals(itemKey, that.itemKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, itemKey);
    }
}
