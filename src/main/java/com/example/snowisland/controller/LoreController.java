package com.example.snowisland.controller;

import com.example.snowisland.service.LoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lore")
@CrossOrigin(origins = "*")
public class LoreController {

    @Autowired
    private LoreService loreService;

    @GetMapping("/catalog")
    public Map<String, Object> catalog(
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Integer playerId) {
        return loreService.listForClient(userRole, playerId);
    }

    @PostMapping("/grant-player")
    public Map<String, Object> grantPlayer(
            @RequestParam String loreSlug,
            @RequestParam Integer playerId,
            @RequestParam(required = false) String userRole) {
        return loreService.grantToPlayer(loreSlug, playerId, userRole);
    }

    @PostMapping("/revoke-player")
    public Map<String, Object> revokePlayer(
            @RequestParam String loreSlug,
            @RequestParam Integer playerId,
            @RequestParam(required = false) String userRole) {
        return loreService.revokeFromPlayer(loreSlug, playerId, userRole);
    }

    @GetMapping("/access")
    public Map<String, Object> access(
            @RequestParam String loreSlug,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Integer playerId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("success", true);
        out.put("allowed", loreService.canAccess(loreSlug, userRole, playerId));
        return out;
    }

    @PostMapping("/acknowledge")
    public Map<String, Object> acknowledge(
            @RequestParam String loreSlug,
            @RequestParam Integer playerId) {
        return loreService.acknowledgeView(loreSlug, playerId);
    }
}
