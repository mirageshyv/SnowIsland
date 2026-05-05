package com.example.snowisland.repository;

import com.example.snowisland.entity.FoodCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodCatalogRepository extends JpaRepository<FoodCatalog, String> {
    List<FoodCatalog> findAllByOrderBySortOrderAsc();
}
