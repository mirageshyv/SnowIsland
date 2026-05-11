package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterEnergyStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterEnergyStockRepository extends JpaRepository<ShelterEnergyStock, Integer> {
    List<ShelterEnergyStock> findAllByOrderByItemKeyAsc();
}
