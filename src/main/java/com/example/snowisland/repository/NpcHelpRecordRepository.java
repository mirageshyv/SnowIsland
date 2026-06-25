package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcHelpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcHelpRecordRepository extends JpaRepository<NpcHelpRecord, Integer> {
    List<NpcHelpRecord> findByPlayerIdOrderByCreatedAtDesc(Integer playerId);
    List<NpcHelpRecord> findByPlayerIdAndNpcIdOrderByCreatedAtDesc(Integer playerId, Integer npcId);
    List<NpcHelpRecord> findByStatus(String status);
    List<NpcHelpRecord> findByPlayerIdAndStatusOrderByCreatedAtDesc(Integer playerId, String status);
}