package com.example.snowisland.repository;

import com.example.snowisland.entity.SpecialClue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialClueRepository extends JpaRepository<SpecialClue, Integer> {
    Optional<SpecialClue> findByClueCode(String clueCode);
    List<SpecialClue> findByIsActiveTrueOrderByPriorityDesc();
    List<SpecialClue> findByIsActiveTrue();
    List<SpecialClue> findAllByOrderByPriorityDesc();
    boolean existsByClueCode(String clueCode);
}