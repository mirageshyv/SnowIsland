package com.example.snowisland.controller;

import com.example.snowisland.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dm/activity-log")
@CrossOrigin(origins = "*")
public class DmActivityLogController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping
    public Map<String, Object> list(
            @RequestParam(required = false) Integer gameDay,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String userRole,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String faction) {
        return activityLogService.listForDm(gameDay, limit, userRole, playerId, faction);
    }
}
