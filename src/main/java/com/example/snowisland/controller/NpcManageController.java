package com.example.snowisland.controller;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/npc/manage")
@CrossOrigin(origins = "*")
public class NpcManageController {

    private static final Logger logger = LoggerFactory.getLogger(NpcManageController.class);

    @Autowired
    private LocationNpcRepository npcRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private NpcFavorRepository npcFavorRepository;

    @Autowired
    private NpcFavorAdjustmentRepository favorAdjustmentRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerNpcRecognitionRepository recognitionRepository;

    /**
     * 获取所有NPC列表（DM管理用，包含完整信息）
     */
    @GetMapping("/all")
    public List<Map<String, Object>> getAllNpcsForDm() {
        List<LocationNpc> npcs = npcRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (LocationNpc npc : npcs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", npc.getId());
            map.put("name", npc.getName());
            map.put("job", npc.getJob());
            map.put("gender", npc.getGender().name());
            map.put("introduction", npc.getIntroduction());
            map.put("personality", npc.getPersonality());
            map.put("status", npc.getStatus());
            map.put("dialogueStyle", npc.getDialogueStyle());
            map.put("avatarUrl", npc.getAvatarUrl());
            map.put("locationId", npc.getLocationId());
            map.put("dailyTradeLimit", npc.getDailyTradeLimit());
            map.put("clueKeywords", npc.getClueKeywords());
            map.put("specialClueContent", npc.getSpecialClueContent());

            // 阵营态度
            map.put("attitudeRuler", npc.getAttitudeRuler().name());
            map.put("attitudeRebel", npc.getAttitudeRebel().name());
            map.put("attitudeAdventurer", npc.getAttitudeAdventurer().name());
            map.put("attitudeScourge", npc.getAttitudeScourge().name());

            // 位置名称
            Optional<Location> locationOpt = locationRepository.findById(npc.getLocationId());
            map.put("locationName", locationOpt.map(Location::getName).orElse("未知"));

            // 时间信息
            map.put("createdAt", npc.getCreatedAt());
            map.put("updatedAt", npc.getUpdatedAt());

            result.add(map);
        }

        return result;
    }

    /**
     * 获取单个NPC详细信息
     */
    @GetMapping("/{npcId}")
    public Map<String, Object> getNpcDetail(@PathVariable Integer npcId) {
        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "NPC不存在");
            return result;
        }

        LocationNpc npc = npcOpt.get();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", npc.getId());
        map.put("name", npc.getName());
        map.put("job", npc.getJob());
        map.put("gender", npc.getGender().name());
        map.put("introduction", npc.getIntroduction());
        map.put("personality", npc.getPersonality());
        map.put("status", npc.getStatus());
        map.put("dialogueStyle", npc.getDialogueStyle());
        map.put("avatarUrl", npc.getAvatarUrl());
        map.put("locationId", npc.getLocationId());
        map.put("dailyTradeLimit", npc.getDailyTradeLimit());
        map.put("clueKeywords", npc.getClueKeywords());
        map.put("specialClueContent", npc.getSpecialClueContent());

        map.put("attitudeRuler", npc.getAttitudeRuler().name());
        map.put("attitudeRebel", npc.getAttitudeRebel().name());
        map.put("attitudeAdventurer", npc.getAttitudeAdventurer().name());
        map.put("attitudeScourge", npc.getAttitudeScourge().name());

        Optional<Location> locationOpt = locationRepository.findById(npc.getLocationId());
        map.put("locationName", locationOpt.map(Location::getName).orElse("未知"));

        map.put("createdAt", npc.getCreatedAt());
        map.put("updatedAt", npc.getUpdatedAt());

        map.put("success", true);
        return map;
    }

    /**
     * 更新NPC基本信息
     */
    @PostMapping("/update")
    public Map<String, Object> updateNpc(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("id");
        if (npcId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "缺少NPC ID");
            return result;
        }

        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "NPC不存在");
            return result;
        }

        LocationNpc npc = npcOpt.get();

        try {
            // 更新基本信息
            if (request.containsKey("name")) {
                npc.setName((String) request.get("name"));
            }
            if (request.containsKey("job")) {
                npc.setJob((String) request.get("job"));
            }
            if (request.containsKey("gender")) {
                npc.setGender(LocationNpc.Gender.valueOf((String) request.get("gender")));
            }
            if (request.containsKey("introduction")) {
                npc.setIntroduction((String) request.get("introduction"));
            }
            if (request.containsKey("personality")) {
                npc.setPersonality((String) request.get("personality"));
            }
            if (request.containsKey("status")) {
                npc.setStatus((String) request.get("status"));
            }
            if (request.containsKey("dialogueStyle")) {
                npc.setDialogueStyle((String) request.get("dialogueStyle"));
            }
            if (request.containsKey("avatarUrl")) {
                npc.setAvatarUrl((String) request.get("avatarUrl"));
            }
            if (request.containsKey("locationId")) {
                npc.setLocationId((Integer) request.get("locationId"));
            }
            if (request.containsKey("dailyTradeLimit")) {
                npc.setDailyTradeLimit((Integer) request.get("dailyTradeLimit"));
            }
            if (request.containsKey("clueKeywords")) {
                npc.setClueKeywords((String) request.get("clueKeywords"));
            }
            if (request.containsKey("specialClueContent")) {
                npc.setSpecialClueContent((String) request.get("specialClueContent"));
            }

            // 更新阵营态度
            if (request.containsKey("attitudeRuler")) {
                npc.setAttitudeRuler(LocationNpc.Attitude.valueOf((String) request.get("attitudeRuler")));
            }
            if (request.containsKey("attitudeRebel")) {
                npc.setAttitudeRebel(LocationNpc.Attitude.valueOf((String) request.get("attitudeRebel")));
            }
            if (request.containsKey("attitudeAdventurer")) {
                npc.setAttitudeAdventurer(LocationNpc.Attitude.valueOf((String) request.get("attitudeAdventurer")));
            }
            if (request.containsKey("attitudeScourge")) {
                npc.setAttitudeScourge(LocationNpc.Attitude.valueOf((String) request.get("attitudeScourge")));
            }

            npc.setUpdatedAt(LocalDateTime.now());
            npcRepository.save(npc);

            logger.info("NPC {} 更新成功: {}", npcId, npc.getName());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "NPC信息更新成功");
            result.put("npc", getNpcDetail(npcId));
            return result;

        } catch (Exception e) {
            logger.error("NPC {} 更新失败: {}", npcId, e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 创建新NPC
     */
    @PostMapping("/create")
    public Map<String, Object> createNpc(@RequestBody Map<String, Object> request) {
        try {
            LocationNpc npc = new LocationNpc();

            // 必填字段
            if (!request.containsKey("name") || !request.containsKey("job") || !request.containsKey("locationId")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "缺少必填字段：name, job, locationId");
                return result;
            }

            npc.setName((String) request.get("name"));
            npc.setJob((String) request.get("job"));
            npc.setLocationId((Integer) request.get("locationId"));

            // 可选字段
            if (request.containsKey("gender")) {
                npc.setGender(LocationNpc.Gender.valueOf((String) request.get("gender")));
            } else {
                npc.setGender(LocationNpc.Gender.男);
            }
            if (request.containsKey("introduction")) {
                npc.setIntroduction((String) request.get("introduction"));
            }
            if (request.containsKey("personality")) {
                npc.setPersonality((String) request.get("personality"));
            }
            if (request.containsKey("status")) {
                npc.setStatus((String) request.get("status"));
            } else {
                npc.setStatus("正常");
            }
            if (request.containsKey("dialogueStyle")) {
                npc.setDialogueStyle((String) request.get("dialogueStyle"));
            }
            if (request.containsKey("avatarUrl")) {
                npc.setAvatarUrl((String) request.get("avatarUrl"));
            }
            if (request.containsKey("dailyTradeLimit")) {
                npc.setDailyTradeLimit((Integer) request.get("dailyTradeLimit"));
            } else {
                npc.setDailyTradeLimit(1);
            }
            if (request.containsKey("clueKeywords")) {
                npc.setClueKeywords((String) request.get("clueKeywords"));
            }
            if (request.containsKey("specialClueContent")) {
                npc.setSpecialClueContent((String) request.get("specialClueContent"));
            }

            // 阵营态度
            if (request.containsKey("attitudeRuler")) {
                npc.setAttitudeRuler(LocationNpc.Attitude.valueOf((String) request.get("attitudeRuler")));
            } else {
                npc.setAttitudeRuler(LocationNpc.Attitude.忽视);
            }
            if (request.containsKey("attitudeRebel")) {
                npc.setAttitudeRebel(LocationNpc.Attitude.valueOf((String) request.get("attitudeRebel")));
            } else {
                npc.setAttitudeRebel(LocationNpc.Attitude.忽视);
            }
            if (request.containsKey("attitudeAdventurer")) {
                npc.setAttitudeAdventurer(LocationNpc.Attitude.valueOf((String) request.get("attitudeAdventurer")));
            } else {
                npc.setAttitudeAdventurer(LocationNpc.Attitude.忽视);
            }
            if (request.containsKey("attitudeScourge")) {
                npc.setAttitudeScourge(LocationNpc.Attitude.valueOf((String) request.get("attitudeScourge")));
            } else {
                npc.setAttitudeScourge(LocationNpc.Attitude.忽视);
            }

            npcRepository.save(npc);

            logger.info("NPC创建成功: {}", npc.getName());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "NPC创建成功");
            result.put("npcId", npc.getId());
            return result;

        } catch (Exception e) {
            logger.error("NPC创建失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 删除NPC
     */
    @DeleteMapping("/{npcId}")
    public Map<String, Object> deleteNpc(@PathVariable Integer npcId) {
        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "NPC不存在");
            return result;
        }

        try {
            LocationNpc npc = npcOpt.get();
            npcRepository.delete(npc);

            logger.info("NPC删除成功: {}", npc.getName());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "NPC删除成功");
            return result;

        } catch (Exception e) {
            logger.error("NPC删除失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取所有地点列表（用于NPC位置选择）
     */
    @GetMapping("/locations")
    public List<Map<String, Object>> getAllLocations() {
        List<Location> locations = locationRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Location loc : locations) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", loc.getId());
            map.put("name", loc.getName());
            map.put("area", loc.getArea());
            map.put("description", loc.getDescription());
            result.add(map);
        }

        return result;
    }

    /**
     * 批量更新NPC状态
     */
    @PostMapping("/batch-status")
    public Map<String, Object> batchUpdateStatus(@RequestBody Map<String, Object> request) {
        List<Integer> npcIds = (List<Integer>) request.get("npcIds");
        String status = (String) request.get("status");

        if (npcIds == null || npcIds.isEmpty() || status == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "缺少必要参数");
            return result;
        }

        try {
            int count = 0;
            for (Integer npcId : npcIds) {
                Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
                if (npcOpt.isPresent()) {
                    LocationNpc npc = npcOpt.get();
                    npc.setStatus(status);
                    npc.setUpdatedAt(LocalDateTime.now());
                    npcRepository.save(npc);
                    count++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "成功更新 " + count + " 个NPC状态");
            return result;

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "批量更新失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取NPC统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getNpcStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        long total = npcRepository.count();
        stats.put("totalNpcs", total);

        // 按状态统计
        Map<String, Long> statusStats = new LinkedHashMap<>();
        for (LocationNpc npc : npcRepository.findAll()) {
            String status = npc.getStatus() != null ? npc.getStatus() : "未知";
            statusStats.put(status, statusStats.getOrDefault(status, 0L) + 1);
        }
        stats.put("statusStats", statusStats);

        // 按地点统计
        Map<String, Long> locationStats = new LinkedHashMap<>();
        for (LocationNpc npc : npcRepository.findAll()) {
            Optional<Location> locOpt = locationRepository.findById(npc.getLocationId());
            String locName = locOpt.map(Location::getName).orElse("未知");
            locationStats.put(locName, locationStats.getOrDefault(locName, 0L) + 1);
        }
        stats.put("locationStats", locationStats);

        return stats;
    }

    // ==================== 好感度管理接口 ====================

    /**
     * 获取所有NPC对玩家的好感度列表
     */
    @GetMapping("/favors/all")
    public List<Map<String, Object>> getAllFavors() {
        List<NpcFavor> favors = npcFavorRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (NpcFavor favor : favors) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", favor.getId());
            map.put("npcId", favor.getNpcId());
            map.put("playerId", favor.getPlayerId());
            map.put("favorValue", favor.getFavorValue());
            map.put("createdAt", favor.getCreatedAt());
            map.put("updatedAt", favor.getUpdatedAt());

            // NPC名称
            Optional<LocationNpc> npcOpt = npcRepository.findById(favor.getNpcId());
            map.put("npcName", npcOpt.map(LocationNpc::getName).orElse("未知NPC"));
            map.put("npcJob", npcOpt.map(LocationNpc::getJob).orElse(""));

            // 玩家名称
            Optional<Player> playerOpt = playerRepository.findById(favor.getPlayerId());
            map.put("playerName", playerOpt.map(Player::getName).orElse("未知玩家"));

            // 好感度等级
            map.put("favorLevel", getFavorLevel(favor.getFavorValue()));

            result.add(map);
        }

        return result;
    }

    /**
     * 获取指定NPC的所有玩家好感度
     */
    @GetMapping("/favors/npc/{npcId}")
    public List<Map<String, Object>> getFavorsByNpc(@PathVariable Integer npcId) {
        List<Map<String, Object>> result = new ArrayList<>();

        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            return result;
        }

        LocationNpc npc = npcOpt.get();
        List<NpcFavor> favors = npcFavorRepository.findByNpcId(npcId);

        for (NpcFavor favor : favors) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", favor.getId());
            map.put("npcId", favor.getNpcId());
            map.put("playerId", favor.getPlayerId());
            map.put("favorValue", favor.getFavorValue());
            map.put("createdAt", favor.getCreatedAt());
            map.put("updatedAt", favor.getUpdatedAt());
            map.put("npcName", npc.getName());
            map.put("npcJob", npc.getJob());

            Optional<Player> playerOpt = playerRepository.findById(favor.getPlayerId());
            map.put("playerName", playerOpt.map(Player::getName).orElse("未知玩家"));
            map.put("playerFaction", playerOpt.map(p -> p.getFaction() != null ? p.getFaction().name() : "无").orElse("无"));
            map.put("favorLevel", getFavorLevel(favor.getFavorValue()));

            result.add(map);
        }

        return result;
    }

    /**
     * 获取指定玩家的所有NPC好感度
     */
    @GetMapping("/favors/player/{playerId}")
    public List<Map<String, Object>> getFavorsByPlayer(@PathVariable Integer playerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        Optional<Player> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return result;
        }

        Player player = playerOpt.get();
        List<NpcFavor> favors = npcFavorRepository.findByPlayerId(playerId);

        for (NpcFavor favor : favors) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", favor.getId());
            map.put("npcId", favor.getNpcId());
            map.put("playerId", favor.getPlayerId());
            map.put("favorValue", favor.getFavorValue());
            map.put("createdAt", favor.getCreatedAt());
            map.put("updatedAt", favor.getUpdatedAt());
            map.put("playerName", player.getName());
            map.put("playerFaction", player.getFaction() != null ? player.getFaction().name() : "无");

            Optional<LocationNpc> npcOpt = npcRepository.findById(favor.getNpcId());
            map.put("npcName", npcOpt.map(LocationNpc::getName).orElse("未知NPC"));
            map.put("npcJob", npcOpt.map(LocationNpc::getJob).orElse(""));
            map.put("favorLevel", getFavorLevel(favor.getFavorValue()));

            result.add(map);
        }

        return result;
    }

    /**
     * 调整NPC对玩家的好感度
     */
    @PostMapping("/favor/adjust")
    @Transactional
    public Map<String, Object> adjustFavor(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer playerId = (Integer) request.get("playerId");
        Integer newValue = (Integer) request.get("newValue");
        String reason = (String) request.get("reason");
        Integer operatorId = request.get("operatorId") != null ? (Integer) request.get("operatorId") : null;
        String operatorName = (String) request.get("operatorName");

        if (npcId == null || playerId == null || newValue == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "缺少必要参数：npcId, playerId, newValue");
            return result;
        }

        // 验证好感度范围
        if (newValue < -100 || newValue > 100) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "好感度值必须在 -100 到 100 之间");
            return result;
        }

        try {
            // 获取当前好感度
            int oldValue = 0;
            Optional<NpcFavor> existingFavor = npcFavorRepository.findByNpcIdAndPlayerId(npcId, playerId);
            NpcFavor favor;

            if (existingFavor.isPresent()) {
                favor = existingFavor.get();
                oldValue = favor.getFavorValue();
                favor.setFavorValue(newValue);
                favor.setUpdatedAt(LocalDateTime.now());
            } else {
                favor = new NpcFavor();
                favor.setNpcId(npcId);
                favor.setPlayerId(playerId);
                favor.setFavorValue(newValue);
            }
            npcFavorRepository.save(favor);

            // 记录调整历史
            NpcFavorAdjustment adjustment = new NpcFavorAdjustment();
            adjustment.setNpcId(npcId);
            adjustment.setPlayerId(playerId);
            adjustment.setOperatorId(operatorId);
            adjustment.setOperatorName(operatorName != null ? operatorName : "DM");
            adjustment.setOldValue(oldValue);
            adjustment.setNewValue(newValue);
            adjustment.setChangeAmount(newValue - oldValue);
            adjustment.setAdjustmentReason(reason != null ? reason : "");
            adjustment.setAdjustmentType("manual");
            favorAdjustmentRepository.save(adjustment);

            logger.info("好感度调整成功: NPC={}, 玩家={}, {}→{}, 操作员={}", npcId, playerId, oldValue, newValue, operatorName);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "好感度调整成功");
            result.put("oldValue", oldValue);
            result.put("newValue", newValue);
            result.put("changeAmount", newValue - oldValue);
            result.put("favorLevel", getFavorLevel(newValue));
            return result;

        } catch (Exception e) {
            logger.error("好感度调整失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "好感度调整失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取好感度调整记录
     */
    @GetMapping("/favor/adjustments")
    public Map<String, Object> getFavorAdjustments(
            @RequestParam(required = false) Integer npcId,
            @RequestParam(required = false) Integer playerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<NpcFavorAdjustment> adjustments;

        if (npcId != null && playerId != null) {
            adjustments = favorAdjustmentRepository.findByFilters(npcId, playerId, pageRequest);
        } else if (npcId != null) {
            adjustments = favorAdjustmentRepository.findByNpcIdOrderByCreatedAtDesc(npcId, pageRequest);
        } else if (playerId != null) {
            adjustments = favorAdjustmentRepository.findByPlayerIdOrderByCreatedAtDesc(playerId, pageRequest);
        } else {
            adjustments = favorAdjustmentRepository.findAllByOrderByCreatedAtDesc(pageRequest);
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (NpcFavorAdjustment adj : adjustments.getContent()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", adj.getId());
            map.put("npcId", adj.getNpcId());
            map.put("playerId", adj.getPlayerId());
            map.put("operatorId", adj.getOperatorId());
            map.put("operatorName", adj.getOperatorName());
            map.put("oldValue", adj.getOldValue());
            map.put("newValue", adj.getNewValue());
            map.put("changeAmount", adj.getChangeAmount());
            map.put("adjustmentReason", adj.getAdjustmentReason());
            map.put("adjustmentType", adj.getAdjustmentType());
            map.put("createdAt", adj.getCreatedAt());

            // NPC和玩家名称
            Optional<LocationNpc> npcOpt = npcRepository.findById(adj.getNpcId());
            map.put("npcName", npcOpt.map(LocationNpc::getName).orElse("未知NPC"));

            Optional<Player> playerOpt = playerRepository.findById(adj.getPlayerId());
            map.put("playerName", playerOpt.map(Player::getName).orElse("未知玩家"));

            records.add(map);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("totalElements", adjustments.getTotalElements());
        result.put("totalPages", adjustments.getTotalPages());
        result.put("currentPage", page);
        return result;
    }

    /**
     * 获取所有玩家列表（用于好感度管理）
     */
    @GetMapping("/players")
    public List<Map<String, Object>> getAllPlayers() {
        List<Player> players = playerRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Player player : players) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", player.getId());
            map.put("name", player.getName());
            map.put("faction", player.getFaction() != null ? player.getFaction().name() : "无");
            result.add(map);
        }

        return result;
    }

    /**
     * 批量重置好感度（GM工具）
     */
    @PostMapping("/favor/reset")
    @Transactional
    public Map<String, Object> resetAllFavors(@RequestBody Map<String, Object> request) {
        String userRole = request.get("userRole") != null ? request.get("userRole").toString() : "";
        if (!"dm".equalsIgnoreCase(userRole.trim())) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "只有DM可以重置好感度");
            return result;
        }

        try {
            List<NpcFavor> allFavors = npcFavorRepository.findAll();
            npcFavorRepository.deleteAll(allFavors);

            logger.info("所有好感度已重置，共删除 {} 条记录", allFavors.size());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "所有好感度已重置，共删除 " + allFavors.size() + " 条记录");
            return result;

        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取指定NPC已认识的玩家列表
     */
    @GetMapping("/recognition/npc/{npcId}")
    public List<Map<String, Object>> getRecognizedPlayersByNpc(@PathVariable Integer npcId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
        if (!npcOpt.isPresent()) {
            return result;
        }
        
        List<PlayerNpcRecognition> recognitions = recognitionRepository.findByNpcId(npcId);
        
        for (PlayerNpcRecognition rec : recognitions) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", rec.getId());
            map.put("playerId", rec.getPlayerId());
            map.put("npcId", rec.getNpcId());
            map.put("locationId", rec.getLocationId());
            map.put("recognizedAt", rec.getRecognizedAt());
            
            Optional<Player> playerOpt = playerRepository.findById(rec.getPlayerId());
            map.put("playerName", playerOpt.map(Player::getName).orElse("未知玩家"));
            map.put("playerFaction", playerOpt.map(p -> p.getFaction() != null ? p.getFaction().name() : "无").orElse("无"));
            
            result.add(map);
        }
        
        return result;
    }

    /**
     * 建立NPC与玩家的认识关系
     */
    @PostMapping("/recognition/create")
    @Transactional
    public Map<String, Object> createRecognition(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer playerId = (Integer) request.get("playerId");
        
        if (npcId == null || playerId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "缺少必要参数：npcId, playerId");
            return result;
        }
        
        try {
            Optional<LocationNpc> npcOpt = npcRepository.findById(npcId);
            if (!npcOpt.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "NPC不存在");
                return result;
            }
            
            LocationNpc npc = npcOpt.get();
            
            if (recognitionRepository.existsByPlayerIdAndNpcId(playerId, npcId)) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "该玩家已认识此NPC");
                return result;
            }
            
            PlayerNpcRecognition recognition = new PlayerNpcRecognition();
            recognition.setPlayerId(playerId);
            recognition.setNpcId(npcId);
            recognition.setLocationId(npc.getLocationId());
            recognitionRepository.save(recognition);
            
            Optional<NpcFavor> existingFavor = npcFavorRepository.findByNpcIdAndPlayerId(npcId, playerId);
            if (!existingFavor.isPresent()) {
                NpcFavor favor = new NpcFavor();
                favor.setNpcId(npcId);
                favor.setPlayerId(playerId);
                favor.setFavorValue(0);
                npcFavorRepository.save(favor);
            }
            
            logger.info("DM建立认识关系: playerId={}, npcId={}, npcName={}", playerId, npcId, npc.getName());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "认识关系建立成功");
            result.put("npcId", npcId);
            result.put("playerId", playerId);
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "建立认识关系失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 解除NPC与玩家的认识关系
     */
    @PostMapping("/recognition/delete")
    @Transactional
    public Map<String, Object> deleteRecognition(@RequestBody Map<String, Object> request) {
        Integer npcId = (Integer) request.get("npcId");
        Integer playerId = (Integer) request.get("playerId");
        
        if (npcId == null || playerId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "缺少必要参数：npcId, playerId");
            return result;
        }
        
        try {
            Optional<PlayerNpcRecognition> recognitionOpt = recognitionRepository.findByPlayerIdAndNpcId(playerId, npcId);
            if (!recognitionOpt.isPresent()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "认识关系不存在");
                return result;
            }
            
            recognitionRepository.delete(recognitionOpt.get());
            
            logger.info("DM解除认识关系: playerId={}, npcId={}", playerId, npcId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "认识关系已解除");
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "解除认识关系失败: " + e.getMessage());
            return result;
        }
    }

    private String getFavorLevel(Integer favorValue) {
        if (favorValue == null) favorValue = 0;
        if (favorValue <= -60) return "敌视";
        if (favorValue <= -20) return "冷漠";
        if (favorValue <= 20) return "中立";
        if (favorValue <= 60) return "友善";
        return "亲近";
    }
}