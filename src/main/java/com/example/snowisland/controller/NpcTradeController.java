package com.example.snowisland.controller;

import com.example.snowisland.service.NpcTradeService;
import com.example.snowisland.service.NpcTradeProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/npc/trade")
@CrossOrigin(origins = "*")
public class NpcTradeController {

    @Autowired
    private NpcTradeService npcTradeService;

    @Autowired
    private NpcTradeProposalService npcTradeProposalService;

    @GetMapping("/config")
    public Map<String, Object> getTradeConfig(
            @RequestParam Integer npcId,
            @RequestParam Integer playerId) {
        return npcTradeService.getTradeConfig(npcId, playerId);
    }

    @PostMapping("/proposal/accept")
    public Map<String, Object> acceptProposal(@RequestBody Map<String, Object> request) {
        Integer playerId = asInt(request.get("playerId"));
        Integer npcId = asInt(request.get("npcId"));
        return npcTradeProposalService.accept(playerId, npcId);
    }

    @PostMapping("/proposal/reject")
    public Map<String, Object> rejectProposal(@RequestBody Map<String, Object> request) {
        Integer playerId = asInt(request.get("playerId"));
        Integer npcId = asInt(request.get("npcId"));
        return npcTradeProposalService.reject(playerId, npcId);
    }

    @GetMapping("/history")
    public List<Map<String, Object>> getTradeHistory(
            @RequestParam Integer playerId,
            @RequestParam(required = false) Integer npcId) {
        if (npcId != null) {
            return npcTradeService.getTradeHistoryByNpc(playerId, npcId);
        }
        return npcTradeService.getTradeHistory(playerId);
    }

    @GetMapping("/dm/configs")
    public List<Map<String, Object>> getAllTradeConfigs() {
        return npcTradeService.getAllTradeConfigs();
    }

    @PostMapping("/dm/save-config")
    public Map<String, Object> saveTradeConfig(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> demandItems = (List<Map<String, Object>>) request.get("demandItems");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> supplyItems = (List<Map<String, Object>>) request.get("supplyItems");
        return npcTradeService.saveTradeConfig(npcId, demandItems, supplyItems);
    }

    @PostMapping("/dm/set-limit")
    public Map<String, Object> setDailyTradeLimit(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer limit = (Integer) request.get("limit");
        return npcTradeService.setDailyTradeLimit(npcId, limit);
    }

    /**
     * 挚友特权：免费领取物资（每日限1次）
     */
    @PostMapping("/claim-free-reward")
    public Map<String, Object> claimFreeReward(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer npcId = (Integer) request.get("npcId");
        return npcTradeService.claimFreeReward(playerId, npcId);
    }

    @PostMapping("/gift")
    public Map<String, Object> giftSurvival(@RequestBody Map<String, Object> request) {
        Integer playerId = asInt(request.get("playerId"));
        Integer npcId = asInt(request.get("npcId"));
        int foodUnits = asIntOrZero(request.get("foodUnits"));
        int woodKg = asIntOrZero(request.get("woodKg"));
        int fuelKg = asIntOrZero(request.get("fuelKg"));
        return npcTradeService.giftSurvival(playerId, npcId, foodUnits, woodKg, fuelKg);
    }

    private static Integer asInt(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int asIntOrZero(Object value) {
        Integer n = asInt(value);
        return n == null ? 0 : Math.max(0, n);
    }

    /**
     * 诊断交易配置中的itemType问题
     */
    @GetMapping("/dm/diagnose")
    public Map<String, Object> diagnoseTradeConfig(
            @RequestParam Integer npcId,
            @RequestParam Integer playerId) {
        return npcTradeService.diagnoseTradeConfig(npcId, playerId);
    }

    /**
     * 自动修复交易配置中错误的itemType
     */
    @PostMapping("/dm/auto-fix")
    public Map<String, Object> autoFixTradeConfig(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer playerId = (Integer) request.get("playerId");
        return npcTradeService.autoFixTradeConfig(npcId, playerId);
    }

    /**
     * 获取玩家所有物资诊断信息
     */
    @GetMapping("/dm/player-items")
    public Map<String, Object> getPlayerItemDiagnosis(@RequestParam Integer playerId) {
        return npcTradeService.getPlayerItemDiagnosis(playerId);
    }
}