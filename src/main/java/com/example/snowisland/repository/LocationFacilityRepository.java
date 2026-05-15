package com.example.snowisland.repository;

import com.example.snowisland.entity.LocationFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationFacilityRepository extends JpaRepository<LocationFacility, Integer> {
    List<LocationFacility> findByLocationId(Integer locationId);
    void deleteByLocationId(Integer locationId);
}
