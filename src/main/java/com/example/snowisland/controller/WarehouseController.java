package com.example.snowisland.controller;

import com.example.snowisland.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAccessibleWarehouses(
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {
        if ("dm".equalsIgnoreCase(userRole)) {
            return ResponseEntity.ok(warehouseService.getAllWarehouses());
        }
        return ResponseEntity.ok(warehouseService.getAccessibleWarehouses(playerId));
    }

    @GetMapping("/{warehouseKey}/stock")
    public ResponseEntity<Map<String, Object>> getWarehouseStock(
            @PathVariable String warehouseKey,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {
        return ResponseEntity.ok(warehouseService.getWarehouseStock(warehouseKey, playerId, userRole));
    }

    @PutMapping("/{warehouseKey}/stock")
    public ResponseEntity<Map<String, Object>> updateWarehouseStock(
            @PathVariable String warehouseKey,
            @RequestBody Map<String, Object> body,
            @RequestParam String userRole) {
        String itemType = (String) body.get("itemType");
        Integer itemId = body.get("itemId") != null ? ((Number) body.get("itemId")).intValue() : null;
        Integer quantity = body.get("quantity") != null ? ((Number) body.get("quantity")).intValue() : null;
        return ResponseEntity.ok(warehouseService.updateWarehouseStock(warehouseKey, itemType, itemId, quantity, userRole));
    }
}
