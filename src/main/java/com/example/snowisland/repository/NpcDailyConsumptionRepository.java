package com.example.snowisland.repository;

import com.example.snowisland.entity.NpcDailyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NpcDailyConsumptionRepository extends JpaRepository<NpcDailyConsumption, Integer> {

    Optional<NpcDailyConsumption> findByNpcIdAndGameDay(Integer npcId, Integer gameDay);

    List<NpcDailyConsumption> findByNpcIdOrderByGameDayDesc(Integer npcId);
}
