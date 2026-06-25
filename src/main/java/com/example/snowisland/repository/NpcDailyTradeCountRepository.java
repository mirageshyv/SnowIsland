package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcDailyTradeCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NpcDailyTradeCountRepository extends JpaRepository<NpcDailyTradeCount, Integer> {
    Optional<NpcDailyTradeCount> findByNpcIdAndPlayerIdAndGameDay(Integer npcId, Integer playerId, Integer gameDay);
    
    @Modifying
    @Query("UPDATE NpcDailyTradeCount c SET c.tradeCount = c.tradeCount + 1 WHERE c.npcId = :npcId AND c.playerId = :playerId AND c.gameDay = :gameDay")
    int incrementTradeCount(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("gameDay") Integer gameDay);
}