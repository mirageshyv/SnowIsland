package com.example.snowisland.repository;

import com.example.snowisland.entity.EndgameShelterEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndgameShelterEventRepository extends JpaRepository<EndgameShelterEvent, Integer> {
}
