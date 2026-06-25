package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcFavorAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcFavorAdjustmentRepository extends JpaRepository<NpcFavorAdjustment, Integer> {

    List<NpcFavorAdjustment> findByNpcIdAndPlayerIdOrderByCreatedAtDesc(Integer npcId, Integer playerId);

    Page<NpcFavorAdjustment> findByNpcIdOrderByCreatedAtDesc(Integer npcId, Pageable pageable);

    Page<NpcFavorAdjustment> findByPlayerIdOrderByCreatedAtDesc(Integer playerId, Pageable pageable);

    Page<NpcFavorAdjustment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT fa FROM NpcFavorAdjustment fa WHERE " +
           "(:npcId IS NULL OR fa.npcId = :npcId) AND " +
           "(:playerId IS NULL OR fa.playerId = :playerId) " +
           "ORDER BY fa.createdAt DESC")
    Page<NpcFavorAdjustment> findByFilters(
            @Param("npcId") Integer npcId,
            @Param("playerId") Integer playerId,
            Pageable pageable);

    List<NpcFavorAdjustment> findTop50ByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(fa) FROM NpcFavorAdjustment fa WHERE fa.operatorId = :operatorId")
    long countByOperatorId(@Param("operatorId") Integer operatorId);
}