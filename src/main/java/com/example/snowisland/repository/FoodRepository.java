package com.example.snowisland.repository;

import com.example.snowisland.entity.Food;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRepository extends JpaRepository<Food, Integer> {
    List<Food> findAllByOrderByIdAsc();
}
