package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkConstruction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArkConstructionRepository extends JpaRepository<ArkConstruction, Integer> {
    Optional<ArkConstruction> findById(Integer id);
}
