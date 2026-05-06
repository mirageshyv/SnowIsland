package com.example.snowisland.controller;

import com.example.snowisland.entity.Trade;
import com.example.snowisland.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*")
public class TradeController {
    @Autowired
    private TradeService tradeService;

    @GetMapping("/player/{playerId}")
    public List<Trade> getTradesByPlayerId(@PathVariable Integer playerId) {
        return tradeService.getTradesByPlayerId(playerId);
    }

    @GetMapping("/incoming/{playerId}/pending-count")
    public Map<String, Long> getIncomingPendingCount(@PathVariable Integer playerId) {
        return Map.of("count", tradeService.countIncomingPendingTrades(playerId));
    }

    @GetMapping("/incoming/{playerId}")
    public List<Trade> getIncomingTrades(@PathVariable Integer playerId) {
        return tradeService.getIncomingTrades(playerId);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getTradeDetail(@PathVariable Integer id) {
        return tradeService.getTradeDetail(id);
    }

    @PostMapping
    public Map<String, Object> createTrade(@RequestBody Map<String, Object> request) {
        Trade trade = new Trade();
        trade.setFromPlayerId((Integer) request.get("fromPlayerId"));
        trade.setToPlayerId((Integer) request.get("toPlayerId"));
        trade.setRemark((String) request.get("remark"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        
        return tradeService.createTrade(trade, items);
    }

    @PostMapping("/{id}/accept")
    public Map<String, Object> acceptTrade(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        return tradeService.acceptTrade(id, playerId);
    }

    @PostMapping("/{id}/reject")
    public Map<String, Object> rejectTrade(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        return tradeService.rejectTrade(id, playerId);
    }
}
