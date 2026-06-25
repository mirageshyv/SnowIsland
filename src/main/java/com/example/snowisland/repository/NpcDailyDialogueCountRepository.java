package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcDailyDialogueCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcDailyDialogueCountRepository extends JpaRepository<NpcDailyDialogueCount, Integer> {

    Optional<NpcDailyDialogueCount> findByPlayerIdAndNpcId(Integer playerId, Integer npcId);

    List<NpcDailyDialogueCount> findByPlayerId(Integer playerId);

    List<NpcDailyDialogueCount> findByNpcId(Integer npcId);

    List<NpcDailyDialogueCount> findByLastGameDay(Integer lastGameDay);

    @Query("SELECT d FROM NpcDailyDialogueCount d WHERE d.playerId = :playerId AND d.lastGameDay < :gameDay")
    List<NpcDailyDialogueCount> findByPlayerIdAndLastGameDayLessThan(
            @Param("playerId") Integer playerId,
            @Param("gameDay") Integer gameDay);

    @Query("SELECT d FROM NpcDailyDialogueCount d WHERE d.lastGameDay < :gameDay")
    List<NpcDailyDialogueCount> findByGameDayBeforeReset(@Param("gameDay") Integer gameDay);

    @Modifying
    @Query("UPDATE NpcDailyDialogueCount d SET d.dialogueCount = 0, d.lastGameDay = :gameDay WHERE d.lastGameDay < :gameDay")
    int resetDialogueCountByGameDay(@Param("gameDay") Integer gameDay);

    @Modifying
    @Query("UPDATE NpcDailyDialogueCount d SET d.dialogueCount = 0, d.lastGameDay = :gameDay WHERE d.playerId = :playerId AND d.lastGameDay < :gameDay")
    int resetDialogueCountByPlayerAndGameDay(
            @Param("playerId") Integer playerId,
            @Param("gameDay") Integer gameDay);
}
