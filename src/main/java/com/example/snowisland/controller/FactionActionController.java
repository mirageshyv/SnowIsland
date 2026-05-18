package com.example.snowisland.controller;

import com.example.snowisland.service.FactionActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faction-actions")
public class FactionActionController {

    @Autowired
    private FactionActionService factionActionService;

    @GetMapping("/context/{playerId}")
    public ResponseEntity<Map<String, Object>> getContext(
            @PathVariable Integer playerId,
            @RequestParam(defaultValue = "1") Integer gameDay) {
        return ResponseEntity.ok(factionActionService.getContext(playerId, gameDay));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Integer playerId = toInt(body.get("playerId"));
        String actionType = body.get("actionType") != null ? body.get("actionType").toString() : null;
        Integer gameDay = toInt(body.get("gameDay"));
        if (gameDay == null) gameDay = 1;
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = body.get("payload") instanceof Map
                ? (Map<String, Object>) body.get("payload") : null;
        return ResponseEntity.ok(factionActionService.submitAction(playerId, actionType, payload, gameDay));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Map<String, Object>>> getPlayerHistory(
            @PathVariable Integer playerId,
            @RequestParam(defaultValue = "1") Integer gameDay) {
        return ResponseEntity.ok(factionActionService.getPlayerHistory(playerId, gameDay));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Integer gameDay,
            @RequestParam(required = false) String faction,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(factionActionService.getAllActions(gameDay, faction, status));
    }

    @PostMapping("/{actionId}/feedback")
    public ResponseEntity<Map<String, Object>> feedback(
            @PathVariable Integer actionId,
            @RequestBody Map<String, Object> body) {
        String feedback = body.get("feedback") != null ? body.get("feedback").toString() : "";
        return ResponseEntity.ok(factionActionService.feedbackAction(actionId, feedback));
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
