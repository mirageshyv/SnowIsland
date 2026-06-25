package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcHelpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NpcHelpConfigRepository extends JpaRepository<NpcHelpConfig, Integer> {
    List<NpcHelpConfig> findByNpcId(Integer npcId);
    List<NpcHelpConfig> findByNpcIdAndHelpType(Integer npcId, String helpType);
    void deleteByNpcId(Integer npcId);
}