package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterStockRepository extends JpaRepository<ShelterStock, Integer> {

    List<ShelterStock> findAllByOrderByItemKeyAsc();
}
