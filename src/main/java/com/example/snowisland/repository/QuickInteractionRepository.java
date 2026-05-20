package com.example.snowisland.repository;

import com.example.snowisland.entity.QuickInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuickInteractionRepository extends JpaRepository<QuickInteraction, Integer> {

    List<QuickInteraction> findByPlayerIdAndGameDayOrderByCreatedAtDesc(Integer playerId, Integer gameDay);

    List<QuickInteraction> findByGameDayOrderByCreatedAtAsc(Integer gameDay);

    List<QuickInteraction> findByFactionAndGameDayOrderByCreatedAtAsc(String faction, Integer gameDay);

    List<QuickInteraction> findByStatusAndGameDayOrderByCreatedAtAsc(QuickInteraction.InteractionStatus status, Integer gameDay);

    List<QuickInteraction> findByInteractionTypeAndGameDayOrderByCreatedAtAsc(String interactionType, Integer gameDay);
}
