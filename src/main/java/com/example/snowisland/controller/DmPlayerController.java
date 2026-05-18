package com.example.snowisland.controller;

import com.example.snowisland.service.DmPlayerInventoryService;
import com.example.snowisland.service.DmPlayerManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dm")
@CrossOrigin(origins = "*")
public class DmPlayerController {

    @Autowired
    private DmPlayerInventoryService dmPlayerInventoryService;

    @Autowired
    private DmPlayerManagementService dmPlayerManagementService;

    @GetMapping("/players")
    public Map<String, Object> listPlayers(@RequestParam String userRole) {
        return dmPlayerManagementService.listPlayersForDm(userRole);
    }

    @GetMapping("/jobs/{jobId}/starting-inventory-preview")
    public Map<String, Object> previewStartingInventory(
            @PathVariable Integer jobId,
            @RequestParam String userRole) {
        return dmPlayerManagementService.previewJobStartingInventory(jobId, userRole);
    }

    @PostMapping("/players")
    public Map<String, Object> createPlayer(
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        return dmPlayerManagementService.createPlayerForDm(body, userRole);
    }

    @PutMapping("/players/{playerId}")
    public Map<String, Object> updatePlayer(
            @PathVariable Integer playerId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        return dmPlayerManagementService.updatePlayerForDm(playerId, body, userRole);
    }

    @DeleteMapping("/players/{playerId}")
    public Map<String, Object> deletePlayer(
            @PathVariable Integer playerId,
            @RequestParam String userRole) {
        return dmPlayerManagementService.deletePlayerForDm(playerId, userRole);
    }

    @PostMapping("/players/{playerId}/grant-starting-inventory")
    public Map<String, Object> grantStartingInventory(
            @PathVariable Integer playerId,
            @RequestParam String userRole,
            @RequestParam(defaultValue = "add") String mode) {
        return dmPlayerManagementService.grantJobStartingInventory(playerId, mode, userRole);
    }

    @PutMapping("/players/{playerId}/inventory/bulk")
    public Map<String, Object> applyInventoryBulk(
            @PathVariable Integer playerId,
            @RequestParam String userRole,
            @RequestParam(defaultValue = "set") String mode,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        return dmPlayerManagementService.applyInventoryItems(playerId, items, mode, userRole);
    }

    @GetMapping("/item-catalog")
    public Map<String, Object> getItemCatalog(@RequestParam String userRole) {
        return dmPlayerInventoryService.getItemCatalog(userRole);
    }

    @GetMapping("/players/{playerId}/inventory")
    public Map<String, Object> getPlayerInventory(
            @PathVariable Integer playerId,
            @RequestParam String userRole) {
        return dmPlayerInventoryService.getPlayerInventory(playerId, userRole);
    }

    @PutMapping("/players/{playerId}/inventory")
    public Map<String, Object> setPlayerItemQuantity(
            @PathVariable Integer playerId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        String itemType = body.get("itemType") != null ? String.valueOf(body.get("itemType")) : null;
        Integer itemId = body.get("itemId") instanceof Number
                ? ((Number) body.get("itemId")).intValue() : null;
        Integer quantity = body.get("quantity") instanceof Number
                ? ((Number) body.get("quantity")).intValue() : null;
        return dmPlayerInventoryService.setPlayerItemQuantity(playerId, itemType, itemId, quantity, userRole);
    }
}
