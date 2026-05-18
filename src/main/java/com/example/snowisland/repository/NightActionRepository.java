package com.example.snowisland.repository;

import com.example.snowisland.entity.NightAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NightActionRepository extends JpaRepository<NightAction, Integer> {

    List<NightAction> findByPlayerIdAndGameDayOrderByCreatedAtDesc(Integer playerId, Integer gameDay);

    List<NightAction> findByGameDayOrderByCreatedAtAsc(Integer gameDay);

    List<NightAction> findByFactionAndGameDayOrderByCreatedAtAsc(String faction, Integer gameDay);
}
