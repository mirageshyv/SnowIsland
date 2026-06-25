package com.example.snowisland.controller;

import com.example.snowisland.service.NpcHelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/npc/help")
@CrossOrigin(origins = "*")
public class NpcHelpController {

    @Autowired
    private NpcHelpService npcHelpService;

    /**
     * 获取NPC的可求助选项列表
     */
    @GetMapping("/options")
    public List<Map<String, Object>> getHelpOptions(
            @RequestParam Integer npcId,
            @RequestParam Integer playerId) {
        return npcHelpService.getAvailableHelpOptions(npcId, playerId);
    }

    /**
     * 请求NPC帮助
     */
    @PostMapping("/request")
    public Map<String, Object> requestHelp(@RequestBody Map<String, Object> request) {
        Integer playerId = (Integer) request.get("playerId");
        Integer npcId = (Integer) request.get("npcId");
        String helpType = (String) request.get("helpType");
        return npcHelpService.requestHelp(playerId, npcId, helpType);
    }

    /**
     * 获取玩家求助历史
     */
    @GetMapping("/history")
    public List<Map<String, Object>> getHelpHistory(
            @RequestParam Integer playerId,
            @RequestParam(required = false) Integer npcId) {
        return npcHelpService.getHelpHistory(playerId, npcId);
    }

    /**
     * 获取玩家进行中的求助
     */
    @GetMapping("/pending")
    public List<Map<String, Object>> getPendingHelps(@RequestParam Integer playerId) {
        return npcHelpService.getPendingHelps(playerId);
    }

    /**
     * 完成求助（DM操作）
     */
    @PostMapping("/complete")
    public Map<String, Object> completeHelp(@RequestBody Map<String, Object> request) {
        Integer recordId = (Integer) request.get("recordId");
        Boolean success = (Boolean) request.get("success");
        String resultDescription = (String) request.get("resultDescription");
        return npcHelpService.completeHelp(recordId, success, resultDescription);
    }

    /**
     * DM保存求助配置
     */
    @PostMapping("/dm/save-config")
    public Map<String, Object> saveHelpConfig(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> helpConfigs = (List<Map<String, Object>>) request.get("helpConfigs");
        return npcHelpService.saveHelpConfig(npcId, helpConfigs);
    }

    /**
     * DM获取所有求助配置
     */
    @GetMapping("/dm/configs")
    public List<Map<String, Object>> getAllHelpConfigs() {
        return npcHelpService.getAllHelpConfigs();
    }

    /**
     * DM删除求助配置
     */
    @DeleteMapping("/dm/config/{configId}")
    public Map<String, Object> deleteHelpConfig(@PathVariable Integer configId) {
        return npcHelpService.deleteHelpConfig(configId);
    }
}