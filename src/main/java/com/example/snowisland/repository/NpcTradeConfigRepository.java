package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcTradeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcTradeConfigRepository extends JpaRepository<NpcTradeConfig, Integer> {
    List<NpcTradeConfig> findByNpcId(Integer npcId);
    List<NpcTradeConfig> findByNpcIdAndConfigType(Integer npcId, String configType);
    void deleteByNpcId(Integer npcId);
}