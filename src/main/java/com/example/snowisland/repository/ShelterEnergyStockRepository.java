package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterEnergyStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelterEnergyStockRepository extends JpaRepository<ShelterEnergyStock, Integer> {
    List<ShelterEnergyStock> findAllByOrderByItemKeyAsc();
}
