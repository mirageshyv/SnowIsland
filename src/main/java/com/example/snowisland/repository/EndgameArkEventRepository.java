package com.example.snowisland.repository;

import com.example.snowisland.entity.EndgameArkEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EndgameArkEventRepository extends JpaRepository<EndgameArkEvent, Integer> {
}
