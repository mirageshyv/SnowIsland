package com.example.snowisland.controller;

import com.example.snowisland.entity.Player;
import com.example.snowisland.service.PlayerService;
import com.example.snowisland.service.PlayerSupplyService;
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

    @Autowired
    private PlayerSupplyService playerSupplyService;

    @GetMapping
    public List<Player> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{id}")
    public Player getPlayerById(@PathVariable Integer id) {
        return playerService.getPlayerById(id);
    }

    @GetMapping("/{id}/details")
    public Map<String, Object> getPlayerDetails(@PathVariable Integer id) {
        return playerService.getPlayerDetails(id);
    }

    @GetMapping("/{id}/items")
    public List<Map<String, Object>> getPlayerItems(@PathVariable Integer id) {
        return playerService.getPlayerItems(id);
    }

    /**
     * 玩家个人食物/能量库存（与统治者避难所的公共库存区分）。
     */
    @GetMapping("/{id}/supplies")
    public Map<String, Object> getPlayerSupplies(@PathVariable Integer id) {
        // ensure defaults exist for new players
        playerSupplyService.ensureDefaultStocksForPlayer(id);
        return Map.of(
                "success", true,
                "playerId", id,
                "foodSupply", playerSupplyService.buildFoodSupply(id),
                "energyReserve", playerSupplyService.buildEnergyReserve(id)
        );
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
