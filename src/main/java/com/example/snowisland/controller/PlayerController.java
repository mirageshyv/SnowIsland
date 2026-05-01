package com.example.snowisland.controller;

import com.example.snowisland.entity.Player;
import com.example.snowisland.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Integer id) {
        return playerService.getPlayerById(id);
    }

    @GetMapping("/{id}/items")
    public List<Map<String, Object>> getPlayerItems(@PathVariable Integer id) {
        return playerService.getPlayerItems(id);
    }

    @PostMapping
    public Map<String, Object> createPlayer(@RequestBody Player player, 
                                             @RequestParam(required = false) String loginUsername) {
        return playerService.createPlayer(player, loginUsername);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updatePlayer(@PathVariable Integer id, @RequestBody Player player) {
        return playerService.updatePlayer(id, player);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deletePlayer(@PathVariable Integer id) {
        return playerService.deletePlayer(id);
    }
}
