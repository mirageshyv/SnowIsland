package com.example.snowisland.repository;

import com.example.snowisland.entity.Trade;
import com.example.snowisland.entity.Trade.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {

    @Query("SELECT DISTINCT t FROM Trade t LEFT JOIN FETCH t.items WHERE t.fromPlayerId = :fromId OR t.toPlayerId = :toId")
    List<Trade> findByFromPlayerIdOrToPlayerId(@Param("fromId") Integer fromPlayerId, @Param("toId") Integer toPlayerId);

    @Query("SELECT DISTINCT t FROM Trade t LEFT JOIN FETCH t.items WHERE t.toPlayerId = :playerId")
    List<Trade> findByToPlayerId(@Param("playerId") Integer toPlayerId);

    long countByToPlayerIdAndStatus(Integer toPlayerId, TradeStatus status);

    List<Trade> findByFromPlayerId(Integer fromPlayerId);
}
