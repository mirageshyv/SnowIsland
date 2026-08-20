package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcItem;
import com.example.snowisland.entity.TradeItem.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcItemRepository extends JpaRepository<NpcItem, Integer> {

    List<NpcItem> findByNpcId(Integer npcId);

    Optional<NpcItem> findByNpcIdAndItemTypeAndItemId(Integer npcId, ItemType itemType, Integer itemId);

    void deleteByNpcId(Integer npcId);
}
