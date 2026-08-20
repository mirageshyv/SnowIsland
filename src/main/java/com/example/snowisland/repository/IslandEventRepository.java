package com.example.snowisland.repository;

import com.example.snowisland.entity.IslandEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    long countByPackId(Integer packId);

    List<IslandEvent> findByPackIdOrderByIdAsc(Integer packId);

    @Query(value = "SELECT e.* FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 WHERE e.triggered = FALSE ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomUntriggered();

    @Query(value = "SELECT e.* FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandom();

    List<IslandEvent> findAllByOrderByIdAsc();

    List<IslandEvent> findByEventDifficulty(Integer eventDifficulty);

    @Query(value = "SELECT e.* FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 WHERE e.event_difficulty = :difficulty AND (e.triggered = FALSE OR e.is_special = TRUE) ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomByDifficulty(@Param("difficulty") Integer difficulty);

    @Query(value = "SELECT e.* FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 WHERE e.event_difficulty >= :difficulty AND (e.triggered = FALSE OR e.is_special = TRUE) ORDER BY e.event_difficulty ASC, RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomByMinDifficulty(@Param("difficulty") Integer difficulty);

    @Query(value = "SELECT COUNT(*) FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 WHERE e.event_difficulty = :difficulty AND (e.triggered = FALSE OR e.is_special = TRUE)", nativeQuery = true)
    long countAvailableByDifficulty(@Param("difficulty") Integer difficulty);

    /**
     * 查找特定难度的未触发特殊事件（仅已启用卡包）
     */
    @Query(value = "SELECT e.* FROM island_event e INNER JOIN event_pack p ON e.pack_id = p.id AND p.enabled = 1 WHERE e.event_difficulty = :difficulty AND e.is_special = TRUE AND e.triggered = FALSE ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<IslandEvent> findRandomSpecialByDifficulty(@Param("difficulty") Integer difficulty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE IslandEvent e SET e.triggered = false")
    int resetAllTriggered();
}