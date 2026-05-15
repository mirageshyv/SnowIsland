package com.example.snowisland.repository;

import com.example.snowisland.entity.LocationNpc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationNpcRepository extends JpaRepository<LocationNpc, Integer> {
    List<LocationNpc> findByLocationId(Integer locationId);
    void deleteByLocationId(Integer locationId);
}
