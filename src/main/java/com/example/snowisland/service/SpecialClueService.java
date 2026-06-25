package com.example.snowisland.service;

import com.example.snowisland.entity.ClueTriggerLog;
import com.example.snowisland.entity.SpecialClue;
import com.example.snowisland.repository.ClueTriggerLogRepository;
import com.example.snowisland.repository.SpecialClueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpecialClueService {

    private static final Logger logger = LoggerFactory.getLogger(SpecialClueService.class);

    @Autowired
    private SpecialClueRepository clueRepository;

    @Autowired
    private ClueTriggerLogRepository logRepository;

    public List<SpecialClue> getAllClues() {
        return clueRepository.findAllByOrderByPriorityDesc();
    }

    public List<SpecialClue> getActiveClues() {
        return clueRepository.findByIsActiveTrueOrderByPriorityDesc();
    }

    public SpecialClue getClueById(Integer id) {
        return clueRepository.findById(id).orElse(null);
    }

    public SpecialClue getClueByCode(String clueCode) {
        return clueRepository.findByClueCode(clueCode).orElse(null);
    }

    public Map<String, Object> createClue(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            SpecialClue clue = new SpecialClue();
            
            String clueCode = (String) data.get("clueCode");
            if (clueCode != null && !clueCode.isEmpty()) {
                if (clueRepository.existsByClueCode(clueCode)) {
                    result.put("success", false);
                    result.put("message", "线索编码已存在");
                    return result;
                }
                clue.setClueCode(clueCode);
            }

            String keywords = (String) data.get("keywords");
            if (keywords == null || keywords.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请输入唤醒关键词");
                return result;
            }
            clue.setKeywords(keywords);

            String content = (String) data.get("content");
            if (content == null || content.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "请输入线索内容");
                return result;
            }
            clue.setContent(content);

            if (data.containsKey("probabilityWeight")) {
                clue.setProbabilityWeight(((Number) data.get("probabilityWeight")).intValue());
            }
            if (data.containsKey("cooldownMinutes")) {
                clue.setCooldownMinutes(((Number) data.get("cooldownMinutes")).intValue());
            }
            if (data.containsKey("priority")) {
                clue.setPriority(((Number) data.get("priority")).intValue());
            }
            if (data.containsKey("matchMode")) {
                clue.setMatchMode(SpecialClue.MatchMode.valueOf((String) data.get("matchMode")));
            }
            if (data.containsKey("isActive")) {
                clue.setIsActive((Boolean) data.get("isActive"));
            }
            if (data.containsKey("description")) {
                clue.setDescription((String) data.get("description"));
            }

            clue = clueRepository.save(clue);
            logger.info("创建特殊线索成功: id={}, code={}", clue.getId(), clue.getClueCode());

            result.put("success", true);
            result.put("message", "创建成功");
            result.put("clue", clueToMap(clue));
            return result;

        } catch (Exception e) {
            logger.error("创建特殊线索失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> updateClue(Integer id, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        try {
            SpecialClue clue = clueRepository.findById(id).orElse(null);
            if (clue == null) {
                result.put("success", false);
                result.put("message", "线索不存在");
                return result;
            }

            if (data.containsKey("clueCode")) {
                String clueCode = (String) data.get("clueCode");
                if (!clueCode.equals(clue.getClueCode()) && clueRepository.existsByClueCode(clueCode)) {
                    result.put("success", false);
                    result.put("message", "线索编码已存在");
                    return result;
                }
                clue.setClueCode(clueCode);
            }
            if (data.containsKey("keywords")) {
                clue.setKeywords((String) data.get("keywords"));
            }
            if (data.containsKey("content")) {
                clue.setContent((String) data.get("content"));
            }
            if (data.containsKey("probabilityWeight")) {
                clue.setProbabilityWeight(((Number) data.get("probabilityWeight")).intValue());
            }
            if (data.containsKey("cooldownMinutes")) {
                clue.setCooldownMinutes(((Number) data.get("cooldownMinutes")).intValue());
            }
            if (data.containsKey("priority")) {
                clue.setPriority(((Number) data.get("priority")).intValue());
            }
            if (data.containsKey("matchMode")) {
                clue.setMatchMode(SpecialClue.MatchMode.valueOf((String) data.get("matchMode")));
            }
            if (data.containsKey("isActive")) {
                clue.setIsActive((Boolean) data.get("isActive"));
            }
            if (data.containsKey("description")) {
                clue.setDescription((String) data.get("description"));
            }

            clue = clueRepository.save(clue);
            logger.info("更新特殊线索成功: id={}", id);

            result.put("success", true);
            result.put("message", "更新成功");
            result.put("clue", clueToMap(clue));
            return result;

        } catch (Exception e) {
            logger.error("更新特殊线索失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> deleteClue(Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            SpecialClue clue = clueRepository.findById(id).orElse(null);
            if (clue == null) {
                result.put("success", false);
                result.put("message", "线索不存在");
                return result;
            }
            clueRepository.delete(clue);
            logger.info("删除特殊线索成功: id={}, code={}", id, clue.getClueCode());
            result.put("success", true);
            result.put("message", "删除成功");
            return result;
        } catch (Exception e) {
            logger.error("删除特殊线索失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return result;
        }
    }

    public Map<String, Object> checkKeywordMatch(String playerMessage, Integer playerId, Integer npcId) {
        Map<String, Object> result = new HashMap<>();
        
        List<SpecialClue> activeClues = getActiveClues();
        if (activeClues.isEmpty()) {
            result.put("matched", false);
            return result;
        }

        List<MatchedClue> matchedClues = new ArrayList<>();
        for (SpecialClue clue : activeClues) {
            String matchedKeyword = matchKeywords(playerMessage, clue);
            if (matchedKeyword != null) {
                boolean onCooldown = isOnCooldown(playerId, clue.getId(), clue.getCooldownMinutes());
                if (!onCooldown) {
                    matchedClues.add(new MatchedClue(clue, matchedKeyword));
                }
            }
        }

        if (matchedClues.isEmpty()) {
            result.put("matched", false);
            return result;
        }

        matchedClues.sort((a, b) -> b.clue.getPriority().compareTo(a.clue.getPriority()));

        SpecialClue highestPriorityClue = matchedClues.get(0).clue;
        String matchedKeyword = matchedClues.get(0).matchedKeyword;

        boolean triggered = rollProbability(highestPriorityClue.getProbabilityWeight());

        if (triggered) {
            logger.info("特殊线索触发成功: playerId={}, npcId={}, clueId={}, keyword={}", 
                playerId, npcId, highestPriorityClue.getId(), matchedKeyword);
            
            result.put("matched", true);
            result.put("clue", clueToMap(highestPriorityClue));
            result.put("matchedKeyword", matchedKeyword);
            result.put("triggered", true);
            
            return result;
        } else {
            logger.info("特殊线索概率判定失败: playerId={}, clueId={}, probability={}", 
                playerId, highestPriorityClue.getId(), highestPriorityClue.getProbabilityWeight());
            
            result.put("matched", true);
            result.put("clue", clueToMap(highestPriorityClue));
            result.put("matchedKeyword", matchedKeyword);
            result.put("triggered", false);
            
            return result;
        }
    }

    public void logTrigger(Integer playerId, Integer npcId, SpecialClue clue, 
                          String playerMessage, String matchedKeyword, String responseText) {
        try {
            ClueTriggerLog log = new ClueTriggerLog();
            log.setClueId(clue.getId());
            log.setClueCode(clue.getClueCode());
            log.setPlayerId(playerId);
            log.setNpcId(npcId);
            log.setPlayerMessage(playerMessage);
            log.setMatchedKeyword(matchedKeyword);
            log.setTriggerTime(LocalDateTime.now());
            log.setResponseText(responseText);
            logRepository.save(log);
            logger.debug("记录线索触发日志: playerId={}, clueId={}", playerId, clue.getId());
        } catch (Exception e) {
            logger.error("记录线索触发日志失败: {}", e.getMessage());
        }
    }

    private String matchKeywords(String playerMessage, SpecialClue clue) {
        if (playerMessage == null || playerMessage.isEmpty()) {
            return null;
        }

        String keywordsStr = clue.getKeywords();
        if (keywordsStr == null || keywordsStr.isEmpty()) {
            return null;
        }

        String[] keywords = keywordsStr.split("[,，;；、\\n\\r]");
        for (String keyword : keywords) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                continue;
            }

            if (clue.getMatchMode() == SpecialClue.MatchMode.EXACT) {
                if (playerMessage.contains(keyword)) {
                    return keyword;
                }
            } else {
                String lowerMessage = playerMessage.toLowerCase();
                String lowerKeyword = keyword.toLowerCase();
                if (lowerMessage.contains(lowerKeyword)) {
                    return keyword;
                }
            }
        }
        return null;
    }

    private boolean isOnCooldown(Integer playerId, Integer clueId, Integer cooldownMinutes) {
        if (playerId == null || clueId == null || cooldownMinutes == null || cooldownMinutes <= 0) {
            return false;
        }

        LocalDateTime since = LocalDateTime.now().minusMinutes(cooldownMinutes);
        List<ClueTriggerLog> recentTriggers = logRepository.findRecentTriggersByPlayerAndClue(playerId, clueId, since);
        return !recentTriggers.isEmpty();
    }

    private boolean rollProbability(Integer probabilityWeight) {
        if (probabilityWeight == null || probabilityWeight <= 0) {
            return false;
        }
        if (probabilityWeight >= 100) {
            return true;
        }
        return new Random().nextInt(100) < probabilityWeight;
    }

    public List<Map<String, Object>> getTriggerLogs(Integer playerId, Integer clueId) {
        List<ClueTriggerLog> logs;
        if (playerId != null && clueId != null) {
            logs = logRepository.findByPlayerIdAndClueIdOrderByTriggerTimeDesc(playerId, clueId);
        } else if (clueId != null) {
            logs = logRepository.findByClueIdOrderByTriggerTimeDesc(clueId);
        } else if (playerId != null) {
            logs = logRepository.findByPlayerIdOrderByTriggerTimeDesc(playerId);
        } else {
            logs = logRepository.findAllByOrderByTriggerTimeDesc();
        }
        return logs.stream().map(this::logToMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> exportClues() {
        return getAllClues().stream().map(this::clueToMap).collect(Collectors.toList());
    }

    public Map<String, Object> importClues(List<Map<String, Object>> clues) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> clueData : clues) {
            try {
                String clueCode = (String) clueData.get("clueCode");
                SpecialClue existing = clueRepository.findByClueCode(clueCode).orElse(null);
                
                if (existing != null) {
                    clueData.remove("id");
                    updateClue(existing.getId(), clueData);
                } else {
                    createClue(clueData);
                }
                successCount++;
            } catch (Exception e) {
                failCount++;
                logger.warn("导入线索失败: {}", e.getMessage());
            }
        }
        
        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", String.format("导入完成，成功 %d 条，失败 %d 条", successCount, failCount));
        return result;
    }

    private Map<String, Object> clueToMap(SpecialClue clue) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", clue.getId());
        map.put("clueCode", clue.getClueCode());
        map.put("keywords", clue.getKeywords());
        map.put("content", clue.getContent());
        map.put("probabilityWeight", clue.getProbabilityWeight());
        map.put("cooldownMinutes", clue.getCooldownMinutes());
        map.put("priority", clue.getPriority());
        map.put("matchMode", clue.getMatchMode().name());
        map.put("isActive", clue.getIsActive());
        map.put("description", clue.getDescription());
        map.put("createdAt", clue.getCreatedAt());
        map.put("updatedAt", clue.getUpdatedAt());
        return map;
    }

    private Map<String, Object> logToMap(ClueTriggerLog log) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("clueId", log.getClueId());
        map.put("clueCode", log.getClueCode());
        map.put("playerId", log.getPlayerId());
        map.put("npcId", log.getNpcId());
        map.put("playerMessage", log.getPlayerMessage());
        map.put("matchedKeyword", log.getMatchedKeyword());
        map.put("triggerTime", log.getTriggerTime());
        map.put("responseText", log.getResponseText());
        map.put("createdAt", log.getCreatedAt());
        return map;
    }

    private static class MatchedClue {
        final SpecialClue clue;
        final String matchedKeyword;

        MatchedClue(SpecialClue clue, String matchedKeyword) {
            this.clue = clue;
            this.matchedKeyword = matchedKeyword;
        }
    }
}