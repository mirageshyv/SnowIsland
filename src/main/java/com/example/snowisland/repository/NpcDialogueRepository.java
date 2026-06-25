package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcDialogue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcDialogueRepository extends JpaRepository<NpcDialogue, Integer> {

    List<NpcDialogue> findByPlayerIdAndNpcIdOrderByCreatedAtDesc(Integer playerId, Integer npcId);

    List<NpcDialogue> findByPlayerIdOrderByCreatedAtDesc(Integer playerId);

    List<NpcDialogue> findByNpcIdOrderByCreatedAtDesc(Integer npcId);

    List<NpcDialogue> findByPlayerIdAndNpcIdOrderByCreatedAtAsc(Integer playerId, Integer npcId);
}