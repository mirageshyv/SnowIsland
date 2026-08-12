package com.example.snowisland.controller;

import com.example.snowisland.service.PlayerMarkerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/player-markers")
@CrossOrigin(origins = "*")
public class PlayerMarkerController {

    @Autowired
    private PlayerMarkerService playerMarkerService;

    @GetMapping("")
    public Map<String, Object> list(@RequestParam(required = false) String userRole) {
        return playerMarkerService.listAll(userRole);
    }

    @PostMapping("/add")
    public Map<String, Object> add(
            @RequestParam Integer playerId,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "false") Boolean visibleToPlayer,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) String userRole) {
        return playerMarkerService.add(playerId, name, visibleToPlayer, note, userRole);
    }

    @PostMapping("/remove")
    public Map<String, Object> remove(
            @RequestParam Integer markerId,
            @RequestParam(required = false) String userRole) {
        return playerMarkerService.remove(markerId, userRole);
    }
}
