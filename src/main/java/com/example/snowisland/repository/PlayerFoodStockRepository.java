package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerFoodStock;
import com.example.snowisland.entity.PlayerFoodStockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerFoodStockRepository extends JpaRepository<PlayerFoodStock, PlayerFoodStockId> {
    List<PlayerFoodStock> findById_PlayerId(Integer playerId);

    long countById_PlayerId(Integer playerId);
}
