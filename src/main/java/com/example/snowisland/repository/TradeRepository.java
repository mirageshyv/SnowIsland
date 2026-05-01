package com.example.snowisland.repository;

import com.example.snowisland.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {
    List<Trade> findByFromPlayerIdOrToPlayerId(Integer fromPlayerId, Integer toPlayerId);
    List<Trade> findByToPlayerId(Integer toPlayerId);
    List<Trade> findByFromPlayerId(Integer fromPlayerId);
}
