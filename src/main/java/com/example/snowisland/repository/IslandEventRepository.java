package com.example.snowisland.repository;

import com.example.snowisland.entity.IslandEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IslandEventRepository extends JpaRepository<IslandEvent, Integer> {

    List<IslandEvent> findByTriggered(Boolean triggered);

    List<IslandEvent> findByRarity(String rarity);

    Optional<IslandEvent> findByName(String name);

    @Query(value = "SELECT * FROM island_event WHERE triggered = FALSE ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomUntriggered();

    @Query(value = "SELECT * FROM island_event ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandom();

    List<IslandEvent> findAllByOrderByIdAsc();

    List<IslandEvent> findByEventDifficulty(Integer eventDifficulty);

    @Query(value = "SELECT * FROM island_event WHERE event_difficulty = :difficulty AND (triggered = FALSE OR is_special = TRUE) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomByDifficulty(@Param("difficulty") Integer difficulty);

    @Query(value = "SELECT * FROM island_event WHERE event_difficulty >= :difficulty AND (triggered = FALSE OR is_special = TRUE) ORDER BY event_difficulty ASC, RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomByMinDifficulty(@Param("difficulty") Integer difficulty);

    @Query(value = "SELECT COUNT(*) FROM island_event WHERE event_difficulty = :difficulty AND (triggered = FALSE OR is_special = TRUE)", nativeQuery = true)
    long countAvailableByDifficulty(@Param("difficulty") Integer difficulty);

    /**
     * 查找特定难度的未触发特殊事件
     */
    @Query(value = "SELECT * FROM island_event WHERE event_difficulty = :difficulty AND is_special = TRUE AND triggered = FALSE ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomSpecialByDifficulty(@Param("difficulty") Integer difficulty);
}