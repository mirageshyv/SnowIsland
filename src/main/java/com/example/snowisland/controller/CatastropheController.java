package com.example.snowisland.controller;

import com.example.snowisland.entity.CatastropheCard;
import com.example.snowisland.service.CatastropheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catastrophe")
public class CatastropheController {

    @Autowired
    private CatastropheService catastropheService;

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getProgress() {
        return ResponseEntity.ok(catastropheService.getProgress());
    }

    @PostMapping("/progress")
    public ResponseEntity<Map<String, Object>> updateProgress(
            @RequestParam Integer value,
            @RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.updateProgress(value, userRole));
    }

    @PostMapping("/advance-day")
    public ResponseEntity<Map<String, Object>> advanceDay() {
        return ResponseEntity.ok(catastropheService.advanceDay());
    }

    @PostMapping("/draw-cards")
    public ResponseEntity<Map<String, Object>> drawCards(@RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.drawCards(userRole));
    }

    @GetMapping("/drawn-cards")
    public ResponseEntity<Map<String, Object>> getDrawnCards(@RequestParam(required = false) Integer round) {
        return ResponseEntity.ok(catastropheService.getDrawnCards(round));
    }

    @PostMapping("/confirm-cards")
    public ResponseEntity<Map<String, Object>> confirmCards(@RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.confirmCards(userRole));
    }

    @GetMapping("/selectable-cards")
    public ResponseEntity<Map<String, Object>> getSelectableCards() {
        return ResponseEntity.ok(catastropheService.getSelectableCards());
    }

    @PostMapping("/select-card")
    public ResponseEntity<Map<String, Object>> selectCard(
            @RequestParam Integer selectedId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.selectCard(selectedId, playerId, userRole));
    }

    @GetMapping("/game-state")
    public ResponseEntity<Map<String, Object>> getGameState() {
        return ResponseEntity.ok(catastropheService.getGameState());
    }

    @PostMapping("/extra-card-due")
    public ResponseEntity<Map<String, Object>> setExtraCardDue(
            @RequestParam Boolean extraCardDue,
            @RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.setExtraCardDue(extraCardDue, userRole));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<CatastropheCard>> getAllCards() {
        return ResponseEntity.ok(catastropheService.getAllCards());
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetCatastrophe(@RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(catastropheService.resetCatastrophe(userRole));
    }
}