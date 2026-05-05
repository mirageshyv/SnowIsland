package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterFoodStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterFoodStockRepository extends JpaRepository<ShelterFoodStock, Integer> {
    List<ShelterFoodStock> findAllByOrderByItemKeyAsc();
}
