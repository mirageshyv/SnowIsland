package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkVoyage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArkVoyageRepository extends JpaRepository<ArkVoyage, Integer> {

    List<ArkVoyage> findAllByOrderByVoyageNumberDesc();

    Optional<ArkVoyage> findTopByOrderByVoyageNumberDesc();

    Optional<ArkVoyage> findByStatus(String status);

    List<ArkVoyage> findByDepartureDay(Integer departureDay);
}
