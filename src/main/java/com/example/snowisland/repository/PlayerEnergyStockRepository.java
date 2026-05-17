package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerEnergyStock;
import com.example.snowisland.entity.PlayerEnergyStockId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerEnergyStockRepository extends JpaRepository<PlayerEnergyStock, PlayerEnergyStockId> {
    List<PlayerEnergyStock> findById_PlayerId(Integer playerId);

    long countById_PlayerId(Integer playerId);
}
