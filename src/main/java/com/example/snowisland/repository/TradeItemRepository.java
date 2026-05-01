package com.example.snowisland.repository;

import com.example.snowisland.entity.TradeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeItemRepository extends JpaRepository<TradeItem, Integer> {
    List<TradeItem> findByTradeId(Integer tradeId);
}
