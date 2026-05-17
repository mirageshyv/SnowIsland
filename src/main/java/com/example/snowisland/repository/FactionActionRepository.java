package com.example.snowisland.repository;

import com.example.snowisland.entity.FactionAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactionActionRepository extends JpaRepository<FactionAction, Integer> {
    List<FactionAction> findByPlayerIdAndGameDayOrderByCreatedAtDesc(Integer playerId, Integer gameDay);
    List<FactionAction> findByGameDayOrderByCreatedAtAsc(Integer gameDay);
    List<FactionAction> findByFactionAndGameDayOrderByCreatedAtAsc(String faction, Integer gameDay);
    boolean existsByPlayerIdAndGameDayAndActionType(Integer playerId, Integer gameDay, String actionType);
    long countByPlayerIdAndGameDay(Integer playerId, Integer gameDay);
}
