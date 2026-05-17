package com.example.snowisland.repository;

import com.example.snowisland.entity.LocationGovernance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationGovernanceRepository extends JpaRepository<LocationGovernance, Integer> {
    boolean existsByLocationIdAndGameDay(Integer locationId, Integer gameDay);
    Optional<LocationGovernance> findByLocationIdAndGameDay(Integer locationId, Integer gameDay);
    List<LocationGovernance> findByGameDay(Integer gameDay);
}
