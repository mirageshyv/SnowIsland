package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerStealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerStealthRepository extends JpaRepository<PlayerStealth, Integer> {
    Optional<PlayerStealth> findByPlayerIdAndGameDay(Integer playerId, Integer gameDay);
    boolean existsByPlayerIdAndGameDay(Integer playerId, Integer gameDay);
}
