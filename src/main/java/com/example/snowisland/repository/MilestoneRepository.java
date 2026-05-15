package com.example.snowisland.repository;

import com.example.snowisland.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Integer> {

    List<Milestone> findAllByOrderByOrderNumberAsc();

    @Query("SELECT COUNT(m) FROM Milestone m WHERE m.isCompleted = true")
    long countCompleted();

    @Query("SELECT COUNT(m) FROM Milestone m")
    long countTotal();
}