package com.example.snowisland.repository;

import com.example.snowisland.entity.WarehouseConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseConfigRepository extends JpaRepository<WarehouseConfig, Integer> {
    List<WarehouseConfig> findAllByOrderBySortOrderAsc();
    Optional<WarehouseConfig> findByWarehouseKey(String warehouseKey);
}
