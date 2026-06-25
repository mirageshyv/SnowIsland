package com.example.snowisland.controller;

import com.example.snowisland.service.NpcDialogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/npc")
@CrossOrigin(origins = "*")
public class NpcDialogueController {

    @Autowired
    private NpcDialogueService npcDialogueService;

    /**
     * 获取所有NPC列表（带当前玩家好感度）
     */
    @GetMapping("/list")
    public List<Map<String, Object>> getAllNpcs(@RequestParam(required = false) Integer playerId) {
        return npcDialogueService.getAllNpcsWithFavor(playerId);
    }

    /**
     * 获取指定位置的NPC列表
     */
    @GetMapping("/location/{locationId}")
    public List<Map<String, Object>> getNpcsByLocation(
            @PathVariable Integer locationId,
            @RequestParam(required = false) Integer playerId) {
        return npcDialogueService.getNpcsByLocation(locationId, playerId);
    }

    /**
     * 获取NPC详细信息（带好感度和对话历史）
     */
    @GetMapping("/{npcId}")
    public Map<String, Object> getNpcDetail(
            @PathVariable Integer npcId,
            @RequestParam(required = false) Integer playerId) {
        return npcDialogueService.getNpcDetail(npcId, playerId);
    }

    /**
     * 发送消息给NPC（核心对话接口）
     */
    @PostMapping("/chat")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer npcId = (Integer) request.get("npcId");
        String message = String.valueOf(request.get("message"));
        return npcDialogueService.sendMessage(playerId, npcId, message);
    }

    /**
     * 获取玩家与NPC的对话历史
     */
    @GetMapping("/dialogue/history")
    public List<Map<String, Object>> getDialogueHistory(
            @RequestParam Integer playerId,
            @RequestParam(required = false) Integer npcId) {
        if (npcId != null) {
            return npcDialogueService.getDialogueHistory(playerId, npcId);
        }
        return npcDialogueService.getDialogueHistory(playerId, -1);
    }

    /**
     * 获取玩家所有NPC好感度
     */
    @GetMapping("/favors/{playerId}")
    public List<Map<String, Object>> getPlayerFavors(@PathVariable Integer playerId) {
        return npcDialogueService.getPlayerFavors(playerId);
    }

    /**
     * DM手动设置NPC好感度
     */
    @PostMapping("/favor/set")
    public Map<String, Object> setFavor(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer playerId = (Integer) request.get("playerId");
        Integer favorValue = (Integer) request.get("favorValue");
        return npcDialogueService.setFavor(npcId, playerId, favorValue);
    }

    /**
     * 获取玩家与NPC的对话限制信息
     */
    @GetMapping("/dialogue/limit")
    public Map<String, Object> getDialogueLimit(
            @RequestParam Integer playerId,
            @RequestParam Integer npcId) {
        return npcDialogueService.getDialogueLimitInfo(playerId, npcId);
    }

    /**
     * 获取玩家所有NPC的对话次数记录（DM管理接口）
     */
    @GetMapping("/dialogue/counts")
    public List<Map<String, Object>> getAllDialogueCounts(@RequestParam Integer playerId) {
        return npcDialogueService.getAllDialogueCounts(playerId);
    }

    /**
     * DM重置指定玩家的所有对话次数
     */
    @PostMapping("/dialogue/reset")
    public Map<String, Object> resetDialogueCounts(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        String userRole = request.get("userRole") != null ? request.get("userRole").toString() : "";
        if (!"dm".equalsIgnoreCase(userRole.trim())) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("message", "只有DM可以重置对话次数");
            return result;
        }
        return npcDialogueService.resetPlayerDialogueCounts(playerId);
    }
}