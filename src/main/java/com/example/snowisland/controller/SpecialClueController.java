package com.example.snowisland.controller;

import com.example.snowisland.entity.SpecialClue;
import com.example.snowisland.service.SpecialClueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/special-clue")
@CrossOrigin(origins = "*")
public class SpecialClueController {

    @Autowired
    private SpecialClueService clueService;

    @GetMapping("/all")
    public Map<String, Object> getAllClues() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", clueService.getAllClues().stream().map(this::clueToMap).collect(Collectors.toList()));
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getClueById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        SpecialClue clue = clueService.getClueById(id);
        if (clue == null) {
            result.put("success", false);
            result.put("message", "线索不存在");
        } else {
            result.put("success", true);
            result.put("data", clueToMap(clue));
        }
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> createClue(@RequestBody Map<String, Object> data) {
        return clueService.createClue(data);
    }

    @PostMapping("/update/{id}")
    public Map<String, Object> updateClue(@PathVariable Integer id, @RequestBody Map<String, Object> data) {
        return clueService.updateClue(id, data);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteClue(@PathVariable Integer id) {
        return clueService.deleteClue(id);
    }

    @GetMapping("/logs")
    public Map<String, Object> getTriggerLogs(
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) Integer clueId) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", clueService.getTriggerLogs(playerId, clueId));
        return result;
    }

    @GetMapping("/export")
    public Map<String, Object> exportClues() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", clueService.exportClues());
        return result;
    }

    @PostMapping("/import")
    public Map<String, Object> importClues(@RequestBody List<Map<String, Object>> data) {
        return clueService.importClues(data);
    }

    private Map<String, Object> clueToMap(SpecialClue clue) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", clue.getId());
        map.put("clueCode", clue.getClueCode());
        map.put("keywords", clue.getKeywords());
        map.put("content", clue.getContent());
        map.put("probabilityWeight", clue.getProbabilityWeight());
        map.put("cooldownMinutes", clue.getCooldownMinutes());
        map.put("priority", clue.getPriority());
        map.put("matchMode", clue.getMatchMode().name());
        map.put("isActive", clue.getIsActive());
        map.put("description", clue.getDescription());
        map.put("createdAt", clue.getCreatedAt());
        map.put("updatedAt", clue.getUpdatedAt());
        return map;
    }
}