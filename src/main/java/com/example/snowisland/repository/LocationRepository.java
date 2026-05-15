package com.example.snowisland.repository;

import com.example.snowisland.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    List<Location> findAllByOrderByOrderNumberAsc();
    List<Location> findByAreaOrderByOrderNumberAsc(String area);
}
