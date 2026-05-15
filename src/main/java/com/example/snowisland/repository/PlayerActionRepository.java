package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerActionRepository extends JpaRepository<PlayerAction, Integer> {
    List<PlayerAction> findByPlayerIdAndGameDayOrderByActionSlotAsc(Integer playerId, Integer gameDay);
    List<PlayerAction> findByGameDayOrderByCreatedAtAsc(Integer gameDay);
    List<PlayerAction> findByActionTypeAndGameDayOrderByCreatedAtAsc(String actionType, Integer gameDay);
    List<PlayerAction> findByStatusAndGameDayOrderByCreatedAtAsc(String status, Integer gameDay);
    List<PlayerAction> findByPlayerIdOrderByGameDayDescActionSlotAsc(Integer playerId);
    boolean existsByPlayerIdAndGameDayAndActionSlot(Integer playerId, Integer gameDay, Integer actionSlot);
}
