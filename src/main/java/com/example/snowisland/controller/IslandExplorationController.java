package com.example.snowisland.controller;

import com.example.snowisland.service.IslandExplorationService;
import com.example.snowisland.service.ExplorationDataInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exploration")
@CrossOrigin(origins = "*")
public class IslandExplorationController {

    @Autowired
    private IslandExplorationService islandExplorationService;

    @Autowired
    private ExplorationDataInitService explorationDataInitService;

    @PostMapping("/submit")
    public Map<String, Object> submitExploration(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer gameDay = (Integer) request.get("gameDay");
        @SuppressWarnings("unchecked")
        Map<String, Integer> investItemsRaw = (Map<String, Integer>) request.get("investItems");
        
        Map<Integer, Integer> investItems = new HashMap<>();
        if (investItemsRaw != null) {
            for (Map.Entry<String, Integer> entry : investItemsRaw.entrySet()) {
                try {
                    int itemId = Integer.parseInt(entry.getKey());
                    int quantity = entry.getValue() != null ? entry.getValue() : 0;
                    if (quantity > 0) {
                        investItems.put(itemId, quantity);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        return islandExplorationService.submitExploration(playerId, gameDay, investItems);
    }

    @GetMapping("/player/{playerId}")
    public Map<String, Object> getPlayerExplorations(@PathVariable Integer playerId) {
        return islandExplorationService.getPlayerExplorations(playerId);
    }

    @GetMapping("/pending/{gameDay}")
    public Map<String, Object> getPendingExplorations(@PathVariable Integer gameDay) {
        return islandExplorationService.getPendingExplorations(gameDay);
    }

    @GetMapping("/events")
    public List<Map<String, Object>> getAllEvents() {
        return islandExplorationService.getAllIslandEvents();
    }

    @PostMapping("/events")
    public Map<String, Object> createEvent(@RequestBody Map<String, Object> payload) {
        return islandExplorationService.createEvent(payload);
    }

    @PutMapping("/events/{eventId}")
    public Map<String, Object> updateEvent(@PathVariable Integer eventId, @RequestBody Map<String, Object> payload) {
        return islandExplorationService.updateEvent(eventId, payload);
    }

    @DeleteMapping("/events/{eventId}")
    public Map<String, Object> deleteEvent(@PathVariable Integer eventId) {
        return islandExplorationService.deleteEvent(eventId);
    }

    @PostMapping("/{explorationId}/trigger")
    public Map<String, Object> triggerEvent(@PathVariable Integer explorationId, @RequestBody Map<String, Object> request) {
        Integer eventId = (Integer) request.get("eventId");
        if (eventId != null) {
            return islandExplorationService.triggerEvent(explorationId, eventId);
        } else {
            return islandExplorationService.triggerRandomEvent(explorationId);
        }
    }

    @PostMapping("/{explorationId}/settle")
    public Map<String, Object> settleExploration(@PathVariable Integer explorationId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rewards = (List<Map<String, Object>>) request.get("rewards");
        return islandExplorationService.settleExploration(explorationId, rewards);
    }

    @PostMapping("/admin/reimport")
    public Map<String, Object> reimportEvents() {
        Map<String, Object> result = new HashMap<>();
        try {
            explorationDataInitService.reimportEvents();
            result.put("success", true);
            result.put("message", "事件数据重新导入成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重新导入失败: " + e.getMessage());
        }
        return result;
    }
}