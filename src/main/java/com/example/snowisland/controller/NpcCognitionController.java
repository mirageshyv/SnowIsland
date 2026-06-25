package com.example.snowisland.controller;

import com.example.snowisland.service.NpcCognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/npc/cognition")
@CrossOrigin(origins = "*")
public class NpcCognitionController {

    @Autowired
    private NpcCognitionService npcCognitionService;

    /**
     * 获取玩家已认识的所有NPC
     */
    @GetMapping("/recognized")
    public List<Map<String, Object>> getRecognizedNpcs(@RequestParam Integer playerId) {
        return npcCognitionService.getRecognizedNpcs(playerId);
    }

    /**
     * 获取玩家在特定地点认识的NPC
     */
    @GetMapping("/recognized/location")
    public List<Map<String, Object>> getRecognizedNpcsByLocation(
            @RequestParam Integer playerId,
            @RequestParam Integer locationId) {
        return npcCognitionService.getRecognizedNpcsByLocation(playerId, locationId);
    }

    /**
     * 检查玩家是否认识某个NPC
     */
    @GetMapping("/check")
    public Map<String, Object> checkRecognition(
            @RequestParam Integer playerId,
            @RequestParam Integer npcId) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("recognized", npcCognitionService.isRecognized(playerId, npcId));
        result.put("favorValue", npcCognitionService.getCurrentFavor(playerId, npcId));
        return result;
    }

    /**
     * 获取玩家认知统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getCognitionStats(@RequestParam Integer playerId) {
        return npcCognitionService.getCognitionStats(playerId);
    }

    /**
     * 手动设置认识NPC（GM工具）
     */
    @PostMapping("/force-recognize")
    public Map<String, Object> forceRecognizeNpc(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer npcId = (Integer) request.get("npcId");
        return npcCognitionService.forceRecognizeNpc(playerId, npcId);
    }

    /**
     * 重置玩家所有认知（GM工具）
     */
    @PostMapping("/reset")
    public Map<String, Object> resetRecognition(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        String userRole = request.get("userRole") != null ? request.get("userRole").toString() : "";
        if (!"dm".equalsIgnoreCase(userRole.trim())) {
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("success", false);
            result.put("message", "只有DM可以重置认知");
            return result;
        }
        return npcCognitionService.resetRecognition(playerId);
    }

    /**
     * 更新好感度
     */
    @PostMapping("/favor/update")
    public Map<String, Object> updateFavor(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer npcId = (Integer) request.get("npcId");
        Integer delta = (Integer) request.get("delta");
        int newFavor = npcCognitionService.updateFavor(playerId, npcId, delta);
        
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("success", true);
        result.put("newFavor", newFavor);
        result.put("favorLevel", NpcCognitionService.getFavorLevel(newFavor));
        return result;
    }

    /**
     * 获取当前好感度
     */
    @GetMapping("/favor")
    public Map<String, Object> getFavor(@RequestParam Integer playerId, @RequestParam Integer npcId) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        int favor = npcCognitionService.getCurrentFavor(playerId, npcId);
        result.put("favorValue", favor);
        result.put("favorLevel", NpcCognitionService.getFavorLevel(favor));
        result.put("favorColor", NpcCognitionService.getFavorColor(favor));
        return result;
    }
}