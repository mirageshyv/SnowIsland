package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerExploration;
import com.example.snowisland.entity.PlayerExploration.ExplorationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerExplorationRepository extends JpaRepository<PlayerExploration, Integer> {

    Optional<PlayerExploration> findByPlayerIdAndGameDay(Integer playerId, Integer gameDay);

    List<PlayerExploration> findByGameDay(Integer gameDay);

    List<PlayerExploration> findByStatus(ExplorationStatus status);

    List<PlayerExploration> findByGameDayAndStatus(Integer gameDay, ExplorationStatus status);

    List<PlayerExploration> findByPlayerIdOrderByGameDayDesc(Integer playerId);

    List<PlayerExploration> findByEventId(Integer eventId);

    long countByGameDayAndStatus(Integer gameDay, ExplorationStatus status);
}