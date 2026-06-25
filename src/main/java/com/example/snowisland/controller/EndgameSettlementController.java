package com.example.snowisland.controller;

import com.example.snowisland.service.EndgameSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/endgame")
@CrossOrigin(origins = "*")
public class EndgameSettlementController {

    @Autowired
    private EndgameSettlementService endgameSettlementService;

    @GetMapping("/shelter/draw")
    public ResponseEntity<Map<String, Object>> drawShelterEvent() {
        return ResponseEntity.ok(endgameSettlementService.drawShelterEvent());
    }

    @GetMapping("/ark/draw")
    public ResponseEntity<Map<String, Object>> drawArkEvent() {
        return ResponseEntity.ok(endgameSettlementService.drawArkEvent());
    }

    @GetMapping("/shelter/all")
    public ResponseEntity<List<Map<String, Object>>> getAllShelterEvents() {
        return ResponseEntity.ok(endgameSettlementService.getAllShelterEvents());
    }

    @GetMapping("/ark/all")
    public ResponseEntity<List<Map<String, Object>>> getAllArkEvents() {
        return ResponseEntity.ok(endgameSettlementService.getAllArkEvents());
    }
}
