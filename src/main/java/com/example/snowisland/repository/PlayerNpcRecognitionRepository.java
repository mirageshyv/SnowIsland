package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerNpcRecognition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerNpcRecognitionRepository extends JpaRepository<PlayerNpcRecognition, Integer> {
    Optional<PlayerNpcRecognition> findByPlayerIdAndNpcId(Integer playerId, Integer npcId);
    List<PlayerNpcRecognition> findByPlayerId(Integer playerId);
    List<PlayerNpcRecognition> findByNpcId(Integer npcId);
    void deleteByPlayerIdAndNpcId(Integer playerId, Integer npcId);
    boolean existsByPlayerIdAndNpcId(Integer playerId, Integer npcId);
    List<PlayerNpcRecognition> findByPlayerIdAndLocationId(Integer playerId, Integer locationId);
}