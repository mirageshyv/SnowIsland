package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerItemRepository extends JpaRepository<PlayerItem, Integer> {
    List<PlayerItem> findByPlayerId(Integer playerId);
    List<PlayerItem> findByPlayerIdAndItemType(Integer playerId, PlayerItem.ItemType itemType);

    Optional<PlayerItem> findByPlayerIdAndItemTypeAndItemId(Integer playerId, PlayerItem.ItemType itemType, Integer itemId);

    @Modifying
    @Query("UPDATE PlayerItem p SET p.quantity = p.quantity + :delta WHERE p.playerId = :playerId AND p.itemType = :itemType AND p.itemId = :itemId")
    int updateQuantity(@Param("playerId") Integer playerId, @Param("itemType") PlayerItem.ItemType itemType, @Param("itemId") Integer itemId, @Param("delta") Integer delta);
}