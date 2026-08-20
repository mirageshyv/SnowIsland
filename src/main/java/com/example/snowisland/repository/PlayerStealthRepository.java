package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerStealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PlayerStealthRepository extends JpaRepository<PlayerStealth, Integer> {
    Optional<PlayerStealth> findByPlayerIdAndGameDay(Integer playerId, Integer gameDay);
    boolean existsByPlayerIdAndGameDay(Integer playerId, Integer gameDay);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteBySourceActionId(Integer sourceActionId);
}
