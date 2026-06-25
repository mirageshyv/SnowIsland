package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcFavor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcFavorRepository extends JpaRepository<NpcFavor, Integer> {

    Optional<NpcFavor> findByNpcIdAndPlayerId(Integer npcId, Integer playerId);

    List<NpcFavor> findByPlayerId(Integer playerId);

    List<NpcFavor> findByNpcId(Integer npcId);

    @Modifying
    @Query("UPDATE NpcFavor f SET f.favorValue = f.favorValue + :change WHERE f.npcId = :npcId AND f.playerId = :playerId")
    int updateFavorValue(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("change") Integer change);

    @Modifying
    @Query("UPDATE NpcFavor f SET f.favorValue = :newValue WHERE f.npcId = :npcId AND f.playerId = :playerId")
    int setFavorValue(@Param("npcId") Integer npcId, @Param("playerId") Integer playerId, @Param("newValue") Integer newValue);
}