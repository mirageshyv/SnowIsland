package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterFoodStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShelterFoodStockRepository extends JpaRepository<ShelterFoodStock, Integer> {
    List<ShelterFoodStock> findAllByOrderByItemKeyAsc();
}
