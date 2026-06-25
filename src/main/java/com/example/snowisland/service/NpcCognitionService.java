package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NpcCognitionService {

    private static final Logger logger = LoggerFactory.getLogger(NpcCognitionService.class);

    @Autowired
    private PlayerNpcRecognitionRepository recognitionRepository;

    @Autowired
    private LocationNpcRepository npcRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NpcFavorRepository npcFavorRepository;

    /** 好感度配置 */
    private static final int FAVOR_MAX = 100;
    private static final int FAVOR_MIN = -100;
    private static final int INITIAL_FAVOR_FAVOR = 20;
    private static final int INITIAL_FAVOR_DISLIKE = -20;
    private static final int INITIAL_FAVOR_NEUTRAL = 0;

    /**
     * 玩家前往地点时自动认识该地点的NPC
     */
    @Transactional
    public List<Map<String, Object>> recognizeNpcsAtLocation(Integer playerId, Integer locationId) {
        List<Map<String, Object>> recognized = new ArrayList<>();

        try {
            List<LocationNpc> npcsAtLocation = npcRepository.findByLocationId(locationId);
            
            for (LocationNpc npc : npcsAtLocation) {
                if (!isRecognized(playerId, npc.getId())) {
                    // 创建认识记录
                    PlayerNpcRecognition recognition = new PlayerNpcRecognition();
                    recognition.setPlayerId(playerId);
                    recognition.setNpcId(npc.getId());
                    recognition.setLocationId(locationId);
                    recognitionRepository.save(recognition);

                    // 初始化好感度
                    int initialFavor = calculateInitialFavor(playerId, npc);
                    initializeFavor(playerId, npc.getId(), initialFavor);

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("npcId", npc.getId());
                    result.put("npcName", npc.getName());
                    result.put("npcJob", npc.getJob());
                    result.put("locationId", locationId);
                    result.put("initialFavor", initialFavor);
                    result.put("recognized", true);
                    recognized.add(result);

                    logger.info("玩家{}认识了NPC{}({})，初始好感度: {}", playerId, npc.getId(), npc.getName(), initialFavor);
                }
            }
        } catch (Exception e) {
            logger.error("认识NPC失败: {}", e.getMessage());
        }

        return recognized;
    }

    /**
     * 检查玩家是否认识某个NPC
     */
    public boolean isRecognized(Integer playerId, Integer npcId) {
        return recognitionRepository.existsByPlayerIdAndNpcId(playerId, npcId);
    }

    /**
     * 获取玩家已认识的所有NPC列表
     */
    public List<Integer> getRecognizedNpcIds(Integer playerId) {
        return recognitionRepository.findByPlayerId(playerId).stream()
                .map(PlayerNpcRecognition::getNpcId)
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家已认识的NPC详细信息列表
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecognizedNpcs(Integer playerId) {
        List<Integer> recognizedIds = getRecognizedNpcIds(playerId);
        
        return npcRepository.findAllById(recognizedIds).stream()
                .map(npc -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", npc.getId());
                    map.put("name", npc.getName());
                    map.put("job", npc.getJob());
                    map.put("gender", npc.getGender().name());
                    map.put("introduction", npc.getIntroduction());
                    map.put("locationId", npc.getLocationId());
                    map.put("personality", npc.getPersonality());
                    map.put("status", npc.getStatus());
                    map.put("dialogueStyle", npc.getDialogueStyle());
                    map.put("avatarUrl", npc.getAvatarUrl());

                    npcFavorRepository.findByNpcIdAndPlayerId(npc.getId(), playerId).ifPresent(favor -> {
                        map.put("favorValue", favor.getFavorValue());
                        map.put("favorLevel", getFavorLevel(favor.getFavorValue()));
                        map.put("favorColor", getFavorColor(favor.getFavorValue()));
                    });

                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取玩家在特定地点认识的NPC
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecognizedNpcsByLocation(Integer playerId, Integer locationId) {
        List<Integer> recognizedIds = recognitionRepository.findByPlayerIdAndLocationId(playerId, locationId).stream()
                .map(PlayerNpcRecognition::getNpcId)
                .collect(Collectors.toList());

        return npcRepository.findAllById(recognizedIds).stream()
                .map(npc -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", npc.getId());
                    map.put("name", npc.getName());
                    map.put("job", npc.getJob());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据玩家身份和NPC态度计算初始好感度
     */
    private int calculateInitialFavor(Integer playerId, LocationNpc npc) {
        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return INITIAL_FAVOR_NEUTRAL;
        }

        Player player = playerOpt.get();
        Player.Faction faction = player.getFaction();

        LocationNpc.Attitude attitude;
        switch (faction) {
            case 统治者:
                attitude = npc.getAttitudeRuler();
                break;
            case 反叛者:
                attitude = npc.getAttitudeRebel();
                break;
            case 冒险者:
                attitude = npc.getAttitudeAdventurer();
                break;
            case 天灾使者:
                attitude = npc.getAttitudeScourge();
                break;
            default:
                return INITIAL_FAVOR_NEUTRAL;
        }

        switch (attitude) {
            case 喜好:
                return INITIAL_FAVOR_FAVOR;
            case 厌恶:
                return INITIAL_FAVOR_DISLIKE;
            case 忽视:
            default:
                return INITIAL_FAVOR_NEUTRAL;
        }
    }

    /**
     * 初始化玩家对NPC的好感度
     */
    private void initializeFavor(Integer playerId, Integer npcId, int favorValue) {
        if (npcFavorRepository.findByNpcIdAndPlayerId(npcId, playerId).isPresent()) {
            return;
        }

        NpcFavor favor = new NpcFavor();
        favor.setNpcId(npcId);
        favor.setPlayerId(playerId);
        favor.setFavorValue(favorValue);
        npcFavorRepository.save(favor);
    }

    /**
     * 更新好感度
     */
    @Transactional
    public int updateFavor(Integer playerId, Integer npcId, int delta) {
        int currentFavor = getCurrentFavor(playerId, npcId);
        int newFavor = Math.max(FAVOR_MIN, Math.min(FAVOR_MAX, currentFavor + delta));

        Optional<NpcFavor> existing = npcFavorRepository.findByNpcIdAndPlayerId(npcId, playerId);
        if (existing.isPresent()) {
            existing.get().setFavorValue(newFavor);
            npcFavorRepository.save(existing.get());
        } else {
            NpcFavor favor = new NpcFavor();
            favor.setNpcId(npcId);
            favor.setPlayerId(playerId);
            favor.setFavorValue(newFavor);
            npcFavorRepository.save(favor);
        }

        logger.info("好感度更新: playerId={}, npcId={}, delta={}, current={}", playerId, npcId, delta, newFavor);
        return newFavor;
    }

    /**
     * 获取当前好感度
     */
    public int getCurrentFavor(Integer playerId, Integer npcId) {
        return npcFavorRepository.findByNpcIdAndPlayerId(npcId, playerId)
                .map(NpcFavor::getFavorValue)
                .orElse(0);
    }

    /**
     * 获取好感度等级
     */
    public static String getFavorLevel(Integer favorValue) {
        if (favorValue == null) favorValue = 0;
        if (favorValue <= -60) return "敌视";
        if (favorValue <= -20) return "冷漠";
        if (favorValue <= 20) return "中立";
        if (favorValue <= 60) return "友善";
        return "亲近";
    }

    /**
     * 获取好感度颜色
     */
    public static String getFavorColor(Integer favorValue) {
        if (favorValue == null) favorValue = 0;
        if (favorValue <= -60) return "#ef4444";
        if (favorValue <= -20) return "#f59e0b";
        if (favorValue <= 20) return "#6b7280";
        if (favorValue <= 60) return "#3b82f6";
        return "#22c55e";
    }

    /**
     * 获取玩家的认知统计信息
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCognitionStats(Integer playerId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        long totalNpcs = npcRepository.count();
        long recognizedCount = recognitionRepository.findByPlayerId(playerId).size();
        
        stats.put("totalNpcs", totalNpcs);
        stats.put("recognizedCount", recognizedCount);
        stats.put("unrecognizedCount", totalNpcs - recognizedCount);
        stats.put("recognitionRate", totalNpcs > 0 ? (double) recognizedCount / totalNpcs : 0);
        
        return stats;
    }

    /**
     * 手动设置认识NPC（GM工具）
     */
    @Transactional
    public Map<String, Object> forceRecognizeNpc(Integer playerId, Integer npcId) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "NPC不存在");
            return result;
        }

        LocationNpc npc = npcOpt.get();
        
        if (isRecognized(playerId, npcId)) {
            result.put("success", false);
            result.put("message", "玩家已经认识该NPC");
            return result;
        }

        PlayerNpcRecognition recognition = new PlayerNpcRecognition();
        recognition.setPlayerId(playerId);
        recognition.setNpcId(npcId);
        recognition.setLocationId(npc.getLocationId());
        recognitionRepository.save(recognition);

        int initialFavor = calculateInitialFavor(playerId, npc);
        initializeFavor(playerId, npcId, initialFavor);

        result.put("success", true);
        result.put("message", "成功设置认识NPC");
        result.put("npcName", npc.getName());
        result.put("initialFavor", initialFavor);
        
        return result;
    }

    /**
     * 重置玩家的所有认知（GM工具）
     */
    @Transactional
    public Map<String, Object> resetRecognition(Integer playerId) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<PlayerNpcRecognition> recognitions = recognitionRepository.findByPlayerId(playerId);
            recognitionRepository.deleteAll(recognitions);
            result.put("success", true);
            result.put("message", "已重置玩家的所有认知");
            result.put("resetCount", recognitions.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
        }
        return result;
    }
}