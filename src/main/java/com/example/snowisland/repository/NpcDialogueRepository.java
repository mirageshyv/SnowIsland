package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcDialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcDialogueRepository extends JpaRepository<NpcDialogue, Integer> {

    List<NpcDialogue> findByPlayerIdAndNpcIdOrderByCreatedAtDesc(Integer playerId, Integer npcId);

    List<NpcDialogue> findByPlayerIdOrderByCreatedAtDesc(Integer playerId);

    List<NpcDialogue> findByNpcIdOrderByCreatedAtDesc(Integer npcId);

    List<NpcDialogue> findByPlayerIdAndNpcIdOrderByCreatedAtAsc(Integer playerId, Integer npcId);

    /** 该玩家对该 NPC 对话产生的正向好感合计（全程累计） */
    @Query("SELECT COALESCE(SUM(d.favorChange), 0) FROM NpcDialogue d "
            + "WHERE d.playerId = :playerId AND d.npcId = :npcId AND d.favorChange > 0")
    int sumPositiveFavor(@Param("playerId") Integer playerId, @Param("npcId") Integer npcId);
}