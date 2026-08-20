package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcTradeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcTradeRecordRepository extends JpaRepository<NpcTradeRecord, Integer> {
    List<NpcTradeRecord> findByPlayerIdOrderByCreatedAtDesc(Integer playerId);
    List<NpcTradeRecord> findByPlayerIdAndNpcIdOrderByCreatedAtDesc(Integer playerId, Integer npcId);
    
    @Query("SELECT t FROM NpcTradeRecord t WHERE t.npcId = :npcId AND t.playerId = :playerId AND t.gameDay = :gameDay")
    NpcTradeRecord findTodayTrade(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("gameDay") Integer gameDay);
    
    @Query("SELECT COUNT(t) FROM NpcTradeRecord t WHERE t.npcId = :npcId AND t.playerId = :playerId AND t.gameDay = :gameDay")
    long countTodayTrades(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("gameDay") Integer gameDay);
    
    /** 挚友免费奖励：玩家未付出物资（demand 为空）、NPC 给出物资 */
    @Query("SELECT COUNT(t) FROM NpcTradeRecord t WHERE t.npcId = :npcId AND t.playerId = :playerId AND t.gameDay = :gameDay "
            + "AND t.favorChange = 0 AND (t.demandItems IS NULL OR t.demandItems = '[]') "
            + "AND t.supplyItems IS NOT NULL AND t.supplyItems <> '[]'")
    long countFreeRewardToday(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("gameDay") Integer gameDay);

    /** 赠予：玩家付出物资、NPC 不给回物资（全程累计好感） */
    @Query("SELECT COALESCE(SUM(t.favorChange), 0) FROM NpcTradeRecord t WHERE t.npcId = :npcId AND t.playerId = :playerId "
            + "AND (t.supplyItems IS NULL OR t.supplyItems = '[]') "
            + "AND t.demandItems IS NOT NULL AND t.demandItems <> '[]'")
    int sumGiftFavor(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId);
}