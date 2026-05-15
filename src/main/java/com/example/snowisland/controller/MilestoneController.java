package com.example.snowisland.controller;

import com.example.snowisland.entity.Milestone;
import com.example.snowisland.service.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/milestones")
@CrossOrigin(origins = "*")
public class MilestoneController {

    @Autowired
    private MilestoneService milestoneService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMilestones(
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {

        Map<String, Object> response = new HashMap<>();

        if (playerId == null || userRole == null) {
            response.put("success", false);
            response.put("message", "缺少必要参数");
            return ResponseEntity.badRequest().body(response);
        }

        boolean hasAccess = milestoneService.hasAccess(playerId, userRole);

        if (!hasAccess) {
            response.put("success", false);
            response.put("message", "无访问权限");
            return ResponseEntity.status(403).body(response);
        }

        List<Milestone> milestones = milestoneService.getAllMilestones();
        Map<String, Object> progress = milestoneService.getMilestoneProgress();

        response.put("success", true);
        response.put("data", milestones);
        response.put("progress", progress);
        response.put("isDm", "dm".equalsIgnoreCase(userRole));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getProgress(
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {

        Map<String, Object> response = new HashMap<>();

        if (playerId == null || userRole == null) {
            response.put("success", false);
            response.put("message", "缺少必要参数");
            return ResponseEntity.badRequest().body(response);
        }

        boolean hasAccess = milestoneService.hasAccess(playerId, userRole);

        if (!hasAccess) {
            response.put("success", false);
            response.put("message", "无访问权限");
            return ResponseEntity.status(403).body(response);
        }

        Map<String, Object> progress = milestoneService.getMilestoneProgress();

        response.put("success", true);
        response.put("data", progress);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{milestoneId}/complete")
    public ResponseEntity<Map<String, Object>> completeMilestone(
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {

        Map<String, Object> response = new HashMap<>();

        if (playerId == null || userRole == null) {
            response.put("success", false);
            response.put("message", "缺少必要参数");
            return ResponseEntity.badRequest().body(response);
        }

        if (!"dm".equalsIgnoreCase(userRole)) {
            response.put("success", false);
            response.put("message", "只有DM可以操作");
            return ResponseEntity.status(403).body(response);
        }

        try {
            Milestone milestone = milestoneService.completeMilestone(milestoneId);
            Map<String, Object> progress = milestoneService.getMilestoneProgress();

            response.put("success", true);
            response.put("data", milestone);
            response.put("progress", progress);
            response.put("message", "里程碑已点亮");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{milestoneId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleMilestone(
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(required = false) String userRole) {

        Map<String, Object> response = new HashMap<>();

        if (playerId == null || userRole == null) {
            response.put("success", false);
            response.put("message", "缺少必要参数");
            return ResponseEntity.badRequest().body(response);
        }

        if (!"dm".equalsIgnoreCase(userRole)) {
            response.put("success", false);
            response.put("message", "只有DM可以操作");
            return ResponseEntity.status(403).body(response);
        }

        try {
            Milestone milestone = milestoneService.toggleMilestone(milestoneId);
            Map<String, Object> progress = milestoneService.getMilestoneProgress();

            response.put("success", true);
            response.put("data", milestone);
            response.put("progress", progress);
            response.put("message", milestone.getIsCompleted() ? "里程碑已点亮" : "里程碑已取消");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}