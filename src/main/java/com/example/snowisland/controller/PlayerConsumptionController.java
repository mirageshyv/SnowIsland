package com.example.snowisland.controller;

import com.example.snowisland.service.PlayerConsumptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/player-consumption")
@CrossOrigin(origins = "*")
public class PlayerConsumptionController {

    @Autowired
    private PlayerConsumptionService playerConsumptionService;

    @GetMapping("/context")
    public Map<String, Object> getContext(
            @RequestParam Integer playerId,
            @RequestParam(required = false) Integer gameDay) {
        if (gameDay == null) {
            gameDay = 1;
        }
        return playerConsumptionService.getConsumptionContext(playerId, gameDay);
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> body) {
        Integer playerId = toInt(body.get("playerId"));
        Integer gameDay = toInt(body.get("gameDay"));
        return playerConsumptionService.submitConsumption(playerId, gameDay, body);
    }

    private static Integer toInt(Object o) {
        if (o == null || "".equals(o)) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
