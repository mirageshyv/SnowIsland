package com.example.snowisland.controller;

import com.example.snowisland.service.DmPlayerInventoryService;
import com.example.snowisland.service.DmPlayerManagementService;
import com.example.snowisland.service.PlayerNotebookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private PlayerNotebookService playerNotebookService;

    @GetMapping("/players")
    public Map<String, Object> listPlayers(@RequestParam String userRole) {
        return dmPlayerManagementService.listPlayersForDm(userRole);
    }

    @GetMapping("/jobs/{jobId}/starting-inventory-preview")
    public Map<String, Object> previewStartingInventory(
            @PathVariable Integer jobId,
            @RequestParam(required = false) Integer hiddenJobId,
            @RequestParam String userRole) {
        return dmPlayerManagementService.previewJobStartingInventory(jobId, hiddenJobId, userRole);
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

    @GetMapping("/weapons")
    public Map<String, Object> listWeapons(@RequestParam String userRole) {
        return dmPlayerInventoryService.listWeapons(userRole);
    }

    @PutMapping("/weapons/{weaponId}")
    public Map<String, Object> updateWeapon(
            @PathVariable Integer weaponId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        Integer threatLevel = body.get("threatLevel") instanceof Number
                ? ((Number) body.get("threatLevel")).intValue() : null;
        String remark = body.get("remark") != null ? String.valueOf(body.get("remark")) : null;
        return dmPlayerInventoryService.updateWeapon(weaponId, threatLevel, remark, userRole);
    }

    @PutMapping("/catalog/{itemType}/{itemId}")
    public Map<String, Object> updateCatalogRemark(
            @PathVariable String itemType,
            @PathVariable Integer itemId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        String remark = body.get("remark") != null ? String.valueOf(body.get("remark")) : null;
        return dmPlayerInventoryService.updateCatalogRemark(itemType, itemId, remark, userRole);
    }

    @PutMapping("/catalog-entry/{itemType}/{itemId}")
    public Map<String, Object> updateCatalogEntry(
            @PathVariable String itemType,
            @PathVariable Integer itemId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        String tag = null;
        String remark = null;
        if (body.containsKey("tag")) {
            tag = body.get("tag") != null ? String.valueOf(body.get("tag")) : "";
        }
        if (body.containsKey("remark")) {
            remark = body.get("remark") != null ? String.valueOf(body.get("remark")) : "";
        }
        return dmPlayerInventoryService.updateCatalogEntry(itemType, itemId, tag, remark, userRole);
    }

    @PostMapping("/catalog-image/{itemType}/{itemId}")
    public Map<String, Object> uploadCatalogImage(
            @PathVariable String itemType,
            @PathVariable Integer itemId,
            @RequestParam String userRole,
            @RequestParam("file") MultipartFile file) {
        return dmPlayerInventoryService.uploadCatalogImage(itemType, itemId, file, userRole);
    }

    @GetMapping("/players/{playerId}/inventory")
    public Map<String, Object> getPlayerInventory(
            @PathVariable Integer playerId,
            @RequestParam String userRole) {
        return dmPlayerInventoryService.getPlayerInventory(playerId, userRole);
    }

    @PutMapping("/players/{playerId}/inventory")
    public Map<String, Object> setPlayerInventoryItem(
            @PathVariable Integer playerId,
            @RequestParam String userRole,
            @RequestBody Map<String, Object> body) {
        String itemType = body.get("itemType") != null ? String.valueOf(body.get("itemType")) : null;
        Integer itemId = toInt(body.get("itemId"));
        Integer quantity = toInt(body.get("quantity"));
        return dmPlayerInventoryService.setPlayerItemQuantity(playerId, itemType, itemId, quantity, userRole);
    }

    private static Integer toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/players/{playerId}/notebook")
    public Map<String, Object> listNotebook(@PathVariable Integer playerId) {
        return playerNotebookService.listForPlayer(playerId);
    }

    @GetMapping("/players/{playerId}/notebook/{noteId}")
    public Map<String, Object> getNotebookPage(
            @PathVariable Integer playerId,
            @PathVariable Integer noteId) {
        return playerNotebookService.getForPlayer(playerId, noteId);
    }
}
