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
    
    /** 检查今日是否已领取免费奖励（favorChange=0） */
    @Query("SELECT COUNT(t) FROM NpcTradeRecord t WHERE t.npcId = :npcId AND t.playerId = :playerId AND t.gameDay = :gameDay AND t.favorChange = 0")
    long countFreeRewardToday(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("gameDay") Integer gameDay);
}