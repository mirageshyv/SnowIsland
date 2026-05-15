package com.example.snowisland.controller;

import com.example.snowisland.service.ArkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ark")
@CrossOrigin(origins = "*")
public class ArkController {

    @Autowired
    private ArkService arkService;

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return arkService.getStatus();
    }

    @PostMapping("/invest")
    public Map<String, Object> investResources(
            @RequestParam(required = false, defaultValue = "0") Integer wood,
            @RequestParam(required = false, defaultValue = "0") Integer metal,
            @RequestParam(required = false, defaultValue = "0") Integer sealant
    ) {
        return arkService.investResources(wood, metal, sealant);
    }

    @PostMapping("/component")
    public Map<String, Object> installComponent(
            @RequestParam String componentType,
            @RequestParam Integer count
    ) {
        return arkService.installComponent(componentType, count);
    }

    @PostMapping("/sail")
    public Map<String, Object> buildSail() {
        return arkService.buildSail();
    }

    @PostMapping("/reset")
    public Map<String, Object> reset() {
        return arkService.reset();
    }
}
