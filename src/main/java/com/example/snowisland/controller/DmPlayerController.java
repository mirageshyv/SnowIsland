package com.example.snowisland.controller;

import com.example.snowisland.service.DmPlayerInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dm")
@CrossOrigin(origins = "*")
public class DmPlayerController {

    @Autowired
    private DmPlayerInventoryService dmPlayerInventoryService;

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
