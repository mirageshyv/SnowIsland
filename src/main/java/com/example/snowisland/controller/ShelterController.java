package com.example.snowisland.controller;

import com.example.snowisland.service.ShelterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 统治者避难所：建造值与库存。
 */
@RestController
@RequestMapping("/api/shelter")
@CrossOrigin(origins = "*")
public class ShelterController {

    @Autowired
    private ShelterService shelterService;

    @GetMapping
    public Map<String, Object> getSummary() {
        return shelterService.getSummary();
    }
}
