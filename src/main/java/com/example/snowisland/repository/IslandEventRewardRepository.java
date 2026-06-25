package com.example.snowisland.repository;

import com.example.snowisland.entity.IslandEventReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IslandEventRewardRepository extends JpaRepository<IslandEventReward, Integer> {

    List<IslandEventReward> findByEventId(Integer eventId);

    void deleteByEventId(Integer eventId);
}