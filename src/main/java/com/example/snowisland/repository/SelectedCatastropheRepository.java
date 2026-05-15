package com.example.snowisland.repository;

import com.example.snowisland.entity.SelectedCatastrophe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SelectedCatastropheRepository extends JpaRepository<SelectedCatastrophe, Integer> {
    List<SelectedCatastrophe> findByIsActiveTrue();
    List<SelectedCatastrophe> findByPlayerId(Integer playerId);
}