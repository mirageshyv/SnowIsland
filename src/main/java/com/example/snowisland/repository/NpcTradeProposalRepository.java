package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcTradeProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcTradeProposalRepository extends JpaRepository<NpcTradeProposal, Integer> {

    Optional<NpcTradeProposal> findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
            Integer npcId, Integer playerId, Integer gameDay, String status);

    List<NpcTradeProposal> findByNpcIdAndPlayerIdAndGameDay(Integer npcId, Integer playerId, Integer gameDay);

    boolean existsByNpcIdAndPlayerIdAndGameDayAndStatus(Integer npcId, Integer playerId, Integer gameDay, String status);

    List<NpcTradeProposal> findByNpcIdAndStatus(Integer npcId, String status);

    List<NpcTradeProposal> findByStatusAndGameDay(String status, Integer gameDay);

    List<NpcTradeProposal> findByStatus(String status);
}
