package com.example.snowisland.controller;

import com.example.snowisland.service.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game-state")
public class GameStateController {

    @Autowired
    private GameStateService gameStateService;

    @GetMapping
    public Map<String, Object> getGameState() {
        return gameStateService.getGameState();
    }

    @PutMapping
    public Map<String, Object> updateGameState(
            @RequestBody Map<String, Object> body,
            @RequestParam(required = false, defaultValue = "") String userRole) {
        return gameStateService.updateGameState(body, userRole);
    }
}
