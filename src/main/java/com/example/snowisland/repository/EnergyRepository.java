package com.example.snowisland.repository;

import com.example.snowisland.entity.Energy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnergyRepository extends JpaRepository<Energy, Integer> {
    List<Energy> findAllByOrderByIdAsc();
}
