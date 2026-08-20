package com.example.snowisland.controller;

import com.example.snowisland.service.GameResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game-reset")
@CrossOrigin(origins = "*")
public class GameResetController {

    @Autowired
    private GameResetService gameResetService;

    @GetMapping("/preview")
    public ResponseEntity<Map<String, Object>> getResetPreview() {
        return ResponseEntity.ok(gameResetService.getResetPreview());
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetToInitialState(@RequestParam(required = false) String userRole) {
        try {
            return ResponseEntity.ok(gameResetService.resetToInitialState(userRole));
        } catch (Exception e) {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}