package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerDailyConsumption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerDailyConsumptionRepository extends JpaRepository<PlayerDailyConsumption, Integer> {

    Optional<PlayerDailyConsumption> findByPlayerIdAndGameDay(Integer playerId, Integer gameDay);

    List<PlayerDailyConsumption> findByGameDay(Integer gameDay);
}
