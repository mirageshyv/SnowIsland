package com.example.snowisland.repository;

import com.example.snowisland.entity.ClueTriggerLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClueTriggerLogRepository extends JpaRepository<ClueTriggerLog, Integer> {
    List<ClueTriggerLog> findByPlayerIdAndClueIdOrderByTriggerTimeDesc(Integer playerId, Integer clueId);
    List<ClueTriggerLog> findByClueIdOrderByTriggerTimeDesc(Integer clueId);
    List<ClueTriggerLog> findByPlayerIdOrderByTriggerTimeDesc(Integer playerId);
    List<ClueTriggerLog> findAllByOrderByTriggerTimeDesc();
    
    @Query("SELECT t FROM ClueTriggerLog t WHERE t.playerId = :playerId AND t.clueId = :clueId AND t.triggerTime >= :since")
    List<ClueTriggerLog> findRecentTriggersByPlayerAndClue(@Param("playerId") Integer playerId, @Param("clueId") Integer clueId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM ClueTriggerLog t WHERE t.playerId = :playerId AND t.clueId = :clueId ORDER BY t.triggerTime DESC")
    List<ClueTriggerLog> findRecentTriggers(@Param("playerId") Integer playerId, @Param("clueId") Integer clueId);
}