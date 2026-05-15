package com.example.snowisland.controller;

import com.example.snowisland.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAction(@RequestBody Map<String, Object> body) {
        Integer playerId = toInt(body.get("playerId"));
        Integer actionSlot = toInt(body.get("actionSlot"));
        String actionType = body.get("actionType") != null ? body.get("actionType").toString() : null;
        Integer targetId = toInt(body.get("targetId"));
        Integer npcId = toInt(body.get("npcId"));
        String notes = body.get("notes") != null ? body.get("notes").toString() : null;
        Integer gameDay = toInt(body.get("gameDay"));
        if (gameDay == null) gameDay = 1;
        return ResponseEntity.ok(actionService.submitAction(playerId, actionSlot, actionType, targetId, npcId, notes, gameDay));
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<Map<String, Object>>> getPlayerActions(
            @PathVariable Integer playerId,
            @RequestParam(required = false, defaultValue = "1") Integer gameDay) {
        return ResponseEntity.ok(actionService.getPlayerActions(playerId, gameDay));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllActions(
            @RequestParam(required = false) Integer gameDay,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer playerId) {
        return ResponseEntity.ok(actionService.getAllActions(gameDay, actionType, status, playerId));
    }

    @PostMapping("/{actionId}/feedback")
    public ResponseEntity<Map<String, Object>> feedbackAction(
            @PathVariable Integer actionId,
            @RequestBody Map<String, Object> body) {
        String feedback = (String) body.get("feedback");
        return ResponseEntity.ok(actionService.feedbackAction(actionId, feedback));
    }

    @PostMapping("/resolve/investigate")
    public ResponseEntity<Map<String, Object>> batchResolveInvestigate(
            @RequestParam(defaultValue = "1") Integer gameDay) {
        return ResponseEntity.ok(actionService.batchResolveInvestigate(gameDay));
    }

    @PostMapping("/resolve/produce")
    public ResponseEntity<Map<String, Object>> batchResolveProduce(
            @RequestParam(defaultValue = "1") Integer gameDay) {
        return ResponseEntity.ok(actionService.batchResolveProduce(gameDay));
    }

    @PostMapping("/resolve/transport/{actionId}")
    public ResponseEntity<Map<String, Object>> resolveTransport(@PathVariable Integer actionId) {
        return ResponseEntity.ok(actionService.resolveTransport(actionId));
    }

    @GetMapping("/production-info/{playerId}")
    public ResponseEntity<Map<String, Object>> getProductionInfo(@PathVariable Integer playerId) {
        return ResponseEntity.ok(actionService.getProductionInfo(playerId));
    }

    @GetMapping("/stealth/{playerId}")
    public ResponseEntity<Map<String, Object>> checkStealth(
            @PathVariable Integer playerId,
            @RequestParam(defaultValue = "1") Integer gameDay) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("playerId", playerId);
        result.put("gameDay", gameDay);
        result.put("isStealthed", actionService.isPlayerStealthed(playerId, gameDay));
        return ResponseEntity.ok(result);
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
