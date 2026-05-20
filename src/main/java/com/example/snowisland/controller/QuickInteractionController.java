package com.example.snowisland.controller;

import com.example.snowisland.service.QuickInteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quick-interactions")
@CrossOrigin(origins = "*")
public class QuickInteractionController {

    @Autowired
    private QuickInteractionService quickInteractionService;

    @GetMapping("/context/{playerId}")
    public ResponseEntity<Map<String, Object>> getContext(
            @PathVariable Integer playerId,
            @RequestParam(required = false) Integer gameDay) {
        return ResponseEntity.ok(quickInteractionService.getPlayerContext(playerId, gameDay));
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submit(@RequestBody Map<String, Object> body) {
        Integer playerId = toInt(body.get("playerId"));
        String interactionType = body.get("interactionType") != null ? body.get("interactionType").toString() : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        Integer gameDay = toInt(body.get("gameDay"));
        return ResponseEntity.ok(quickInteractionService.submitInteraction(playerId, interactionType, content, gameDay));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) Integer gameDay,
            @RequestParam(required = false) String faction,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String interactionType) {
        return ResponseEntity.ok(quickInteractionService.getAllInteractions(gameDay, faction, status, interactionType));
    }

    @PostMapping("/{interactionId}/reply")
    public ResponseEntity<Map<String, Object>> reply(
            @PathVariable Integer interactionId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        String reply = body.get("reply") != null ? body.get("reply").toString() : "";
        return ResponseEntity.ok(quickInteractionService.replyInteraction(interactionId, reply, userRole));
    }

    @PostMapping("/{interactionId}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Integer interactionId,
            @RequestBody Map<String, Object> body,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        String status = body.get("status") != null ? body.get("status").toString() : null;
        return ResponseEntity.ok(quickInteractionService.updateStatus(interactionId, status, userRole));
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
