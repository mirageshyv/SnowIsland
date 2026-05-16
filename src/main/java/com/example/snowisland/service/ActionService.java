package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ActionService {

    @Autowired private PlayerActionRepository actionRepository;
    @Autowired private PlayerStealthRepository stealthRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private LocationNpcRepository npcRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private LocationFacilityRepository facilityRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private EntityManager entityManager;

    private static final Map<String, String> PRODUCTION_JOB_MAP = new LinkedHashMap<>();
    private static final Map<String, Map<String, Object>> PRODUCTION_OUTPUT_MAP = new LinkedHashMap<>();
    static {
        PRODUCTION_JOB_MAP.put("渔民", "fishing");
        PRODUCTION_JOB_MAP.put("农户", "farming");
        PRODUCTION_JOB_MAP.put("伐木工", "logging");
        PRODUCTION_JOB_MAP.put("矿工", "mining");
        PRODUCTION_JOB_MAP.put("猎户", "hunting");

        Map<String, Object> fishing = new LinkedHashMap<>();
        fishing.put("itemType", "material"); fishing.put("itemId", 5); fishing.put("quantity", 10);
        fishing.put("unit", "kg"); fishing.put("itemName", "食物");
        fishing.put("description", "在码头使用渔船设施，获得食物10kg");
        fishing.put("requiredLocation", "码头"); fishing.put("requiredFacility", "渔船");
        PRODUCTION_OUTPUT_MAP.put("fishing", fishing);

        Map<String, Object> farming = new LinkedHashMap<>();
        farming.put("itemType", "material"); farming.put("itemId", 5); farming.put("quantity", 15);
        farming.put("unit", "kg"); farming.put("itemName", "食物");
        farming.put("description", "使用牲畜设施，获得食物15kg");
        PRODUCTION_OUTPUT_MAP.put("farming", farming);

        Map<String, Object> logging = new LinkedHashMap<>();
        logging.put("itemType", "material"); logging.put("itemId", 2); logging.put("quantity", 5);
        logging.put("unit", "吨"); logging.put("itemName", "木材");
        logging.put("description", "使用电锯20吨木材，否则5吨");
        PRODUCTION_OUTPUT_MAP.put("logging", logging);

        Map<String, Object> mining = new LinkedHashMap<>();
        mining.put("itemType", "material"); mining.put("itemId", 7); mining.put("quantity", 5);
        mining.put("unit", "吨"); farming.put("itemName", "石料");
        mining.put("description", "使用镐子20吨石料，否则5吨");
        PRODUCTION_OUTPUT_MAP.put("mining", mining);

        Map<String, Object> hunting = new LinkedHashMap<>();
        hunting.put("itemType", "material"); hunting.put("itemId", 5); hunting.put("quantity", 5);
        hunting.put("unit", "kg"); hunting.put("itemName", "食物");
        hunting.put("description", "需要远程武器（无需子弹或弓箭）5kg食物");
        PRODUCTION_OUTPUT_MAP.put("hunting", hunting);
    }

    @Transactional
    public Map<String, Object> submitAction(Integer playerId, Integer actionSlot, String actionType,
                                              Integer targetId, Integer npcId, String notes, Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (!optPlayer.isPresent()) {
            result.put("success", false); result.put("message", "玩家不存在");
            return result;
        }
        Player player = optPlayer.get();

        if (actionRepository.existsByPlayerIdAndGameDayAndActionSlot(playerId, gameDay, actionSlot)) {
            result.put("success", false); result.put("message", "该行动槽位已提交");
            return result;
        }

        PlayerAction action = new PlayerAction();
        action.setPlayerId(playerId);
        action.setPlayerName(player.getName());
        action.setPlayerFaction(player.getFaction() != null ? player.getFaction().name() : "平民");
        action.setActionSlot(actionSlot);
        action.setActionType(actionType);
        action.setTargetId(targetId);
        action.setNotes(notes);
        action.setGameDay(gameDay);
        action.setStatus(PlayerAction.ActionStatus.pending);

        String autoResult = "";

        if ("go_location".equals(actionType)) {
            autoResult = resolveGoLocation(targetId, npcId, player);
        } else if ("investigate_player".equals(actionType)) {
            autoResult = "等待DM反馈调查结果";
            if (targetId != null) {
                Optional<Player> optTarget = playerRepository.findById(targetId);
                optTarget.ifPresent(t -> action.setTargetName(t.getName()));
            }
        } else if ("produce".equals(actionType)) {
            autoResult = resolveProduce(player);
        } else if ("hide".equals(actionType)) {
            autoResult = resolveHide(playerId, player.getName(), gameDay, action);
        } else if ("use_trait".equals(actionType)) {
            autoResult = "等待DM反馈";
        } else if ("use_skill".equals(actionType)) {
            autoResult = "等待DM反馈";
        } else if ("transport".equals(actionType)) {
            autoResult = "等待DM反馈";
        }

        if (targetId != null && "go_location".equals(actionType)) {
            Optional<Location> optLoc = locationRepository.findById(targetId);
            optLoc.ifPresent(l -> action.setTargetName(l.getName()));
        }
        if (npcId != null) {
            Optional<LocationNpc> optNpc = npcRepository.findById(npcId);
            optNpc.ifPresent(n -> action.setNpcName(n.getName()));
        }

        action.setResult(autoResult);
        actionRepository.save(action);

        result.put("success", true);
        result.put("message", "行动提交成功");
        result.put("data", toMap(action));
        return result;
    }

    private String resolveGoLocation(Integer locationId, Integer npcId, Player player) {
        if (locationId == null) return "未选择目标地点";
        Optional<Location> optLoc = locationRepository.findById(locationId);
        if (!optLoc.isPresent()) return "地点不存在";

        Location loc = optLoc.get();
        StringBuilder sb = new StringBuilder();
        sb.append("【地点信息】").append(loc.getName()).append("\n");
        sb.append("区域：").append(loc.getArea()).append("\n");
        sb.append("描述：").append(loc.getDescription()).append("\n");
        sb.append("防御值：").append(loc.getDefenseValue());
        if (loc.getManagement() != null && !loc.getManagement().isEmpty()) {
            sb.append("\n管理方：").append(loc.getManagement());
        }

        List<LocationFacility> facilities = facilityRepository.findByLocationId(locationId);
        if (!facilities.isEmpty()) {
            sb.append("\n\n【设施】");
            for (LocationFacility f : facilities) {
                sb.append("\n• ").append(f.getName());
                if (f.getDescription() != null && !f.getDescription().isEmpty()) {
                    sb.append("：").append(f.getDescription());
                }
            }
        }

        List<LocationNpc> npcs = npcRepository.findByLocationId(locationId);
        if (!npcs.isEmpty()) {
            sb.append("\n\n【NPC】");
            for (LocationNpc n : npcs) {
                sb.append("\n• ").append(n.getName()).append("（").append(n.getJob()).append("）");
            }
        }

        if (npcId != null) {
            Optional<LocationNpc> optNpc = npcRepository.findById(npcId);
            if (optNpc.isPresent()) {
                LocationNpc npc = optNpc.get();
                sb.append("\n\n【NPC互动】").append(npc.getName()).append("（").append(npc.getJob()).append("）");
                String faction = player.getFaction() != null ? player.getFaction().name() : "平民";
                String attitude = getNpcAttitude(npc, faction);
                sb.append("\n态度：").append(attitude);
                sb.append("\n介绍：").append(npc.getIntroduction() != null ? npc.getIntroduction() : "无");
            }
        }

        return sb.toString();
    }

    private String getNpcAttitude(LocationNpc npc, String faction) {
        if ("平民".equals(faction)) return "忽视";
        switch (faction) {
            case "统治者": return npc.getAttitudeRuler() != null ? npc.getAttitudeRuler().name() : "忽视";
            case "反叛者": return npc.getAttitudeRebel() != null ? npc.getAttitudeRebel().name() : "忽视";
            case "冒险者": return npc.getAttitudeAdventurer() != null ? npc.getAttitudeAdventurer().name() : "忽视";
            case "天灾使者": return npc.getAttitudeScourge() != null ? npc.getAttitudeScourge().name() : "忽视";
            default: return "忽视";
        }
    }

    private String resolveProduce(Player player) {
        Integer jobId = player.getJobId();
        if (jobId == null) return "您没有职业，无法生产";

        Optional<Job> optJob = jobRepository.findById(jobId);
        if (!optJob.isPresent()) return "职业信息不存在";

        Job job = optJob.get();
        String productionKey = PRODUCTION_JOB_MAP.get(job.getName());
        if (productionKey == null) return "您的职业（" + job.getName() + "）没有生产技能";

        Map<String, Object> output = PRODUCTION_OUTPUT_MAP.get(productionKey);
        return "【生产】" + output.get("description") + "\n等待DM结算后物资将发放到您的背包中";
    }

    private String resolveHide(Integer playerId, String playerName, Integer gameDay, PlayerAction action) {
        int nextDay = gameDay + 1;
        if (!stealthRepository.existsByPlayerIdAndGameDay(playerId, nextDay)) {
            PlayerStealth stealth = new PlayerStealth();
            stealth.setPlayerId(playerId);
            stealth.setPlayerName(playerName);
            stealth.setGameDay(nextDay);
            stealth.setSourceActionId(action.getId());
            stealthRepository.save(stealth);
        }
        return "【隐藏】您已进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标";
    }

    public List<Map<String, Object>> getPlayerActions(Integer playerId, Integer gameDay) {
        List<PlayerAction> actions = actionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay);
        return actions.stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllActions(Integer gameDay, String actionType, String status, Integer playerId) {
        List<PlayerAction> actions;
        if (playerId != null && gameDay != null) {
            actions = actionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay);
        } else if (gameDay != null) {
            actions = actionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        } else {
            actions = actionRepository.findAll();
        }
        if (actionType != null && !actionType.isEmpty()) {
            actions = actions.stream().filter(a -> actionType.equals(a.getActionType())).collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            actions = actions.stream().filter(a -> status.equals(a.getStatus().name())).collect(Collectors.toList());
        }
        return actions.stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> feedbackAction(Integer actionId, String feedback) {
        Map<String, Object> result = new HashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false); result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        String existingResult = action.getResult() != null ? action.getResult() : "";
        action.setResult(existingResult + "\n\n【DM反馈】\n" + feedback);
        action.setStatus(PlayerAction.ActionStatus.feedbacked);
        actionRepository.save(action);
        result.put("success", true); result.put("message", "反馈成功");
        result.put("data", toMap(action));
        return result;
    }

    @Transactional
    public Map<String, Object> batchResolveInvestigate(Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        List<PlayerAction> investigateActions = actionRepository.findByActionTypeAndGameDayOrderByCreatedAtAsc("investigate_player", gameDay);
        int resolved = 0;
        for (PlayerAction action : investigateActions) {
            if (action.getStatus() != PlayerAction.ActionStatus.pending) continue;
            Integer targetPlayerId = action.getTargetId();
            if (targetPlayerId == null) continue;

            boolean isStealthed = stealthRepository.existsByPlayerIdAndGameDay(targetPlayerId, gameDay);
            if (isStealthed) {
                action.setResult("未找到该玩家");
            } else {
                List<PlayerAction> targetActions = actionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(targetPlayerId, gameDay);
                StringBuilder sb = new StringBuilder("【调查结果】");
                sb.append(action.getTargetName()).append("的自由行动：\n");
                for (PlayerAction ta : targetActions) {
                    sb.append("行动").append(ta.getActionSlot()).append("：")
                      .append(getActionTypeLabel(ta.getActionType()));
                    if (ta.getTargetName() != null) sb.append(" → ").append(ta.getTargetName());
                    sb.append("\n");
                }
                action.setResult(sb.toString());
            }
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            actionRepository.save(action);
            resolved++;
        }
        result.put("success", true);
        result.put("message", "已结算" + resolved + "条调查行动");
        result.put("resolved", resolved);
        return result;
    }

    @Transactional
    public Map<String, Object> batchResolveProduce(Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        List<PlayerAction> produceActions = actionRepository.findByActionTypeAndGameDayOrderByCreatedAtAsc("produce", gameDay);
        int resolved = 0;
        for (PlayerAction action : produceActions) {
            if (action.getStatus() != PlayerAction.ActionStatus.pending) continue;
            Integer playerId = action.getPlayerId();
            Optional<Player> optPlayer = playerRepository.findById(playerId);
            if (!optPlayer.isPresent()) continue;

            Player player = optPlayer.get();
            Integer jobId = player.getJobId();
            if (jobId == null) continue;

            Optional<Job> optJob = jobRepository.findById(jobId);
            if (!optJob.isPresent()) continue;

            String productionKey = PRODUCTION_JOB_MAP.get(optJob.get().getName());
            if (productionKey == null) continue;

            Map<String, Object> output = PRODUCTION_OUTPUT_MAP.get(productionKey);
            String itemType = (String) output.get("itemType");
            Integer itemId = (Integer) output.get("itemId");
            Integer quantity = (Integer) output.get("quantity");

            addItemToPlayer(playerId, itemType, itemId, quantity);

            String existingResult = action.getResult() != null ? action.getResult() : "";
            action.setResult(existingResult + "\n\n【生产结算】已获得" + output.get("itemName") + " " + quantity + output.get("unit"));
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            actionRepository.save(action);
            resolved++;
        }
        result.put("success", true);
        result.put("message", "已结算" + resolved + "条生产行动");
        result.put("resolved", resolved);
        return result;
    }

    @Transactional
    public Map<String, Object> resolveTransport(Integer actionId) {
        Map<String, Object> result = new HashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false); result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        if (!"transport".equals(action.getActionType())) {
            result.put("success", false); result.put("message", "不是搬运行动");
            return result;
        }
        if (action.getStatus() != PlayerAction.ActionStatus.pending) {
            result.put("success", false); result.put("message", "该行动已处理");
            return result;
        }

        String notes = action.getNotes() != null ? action.getNotes() : "";
        try {
            String[] lines = notes.split("\n");
            String mode = null;
            String sourceWarehouse = null;
            String destWarehouse = null;
            List<Map<String, Object>> items = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("[mode:")) {
                    mode = line.substring("[mode:".length(), line.indexOf("]"));
                } else if (line.startsWith("[source:")) {
                    sourceWarehouse = line.substring("[source:".length(), line.indexOf("]"));
                } else if (line.startsWith("[dest:")) {
                    destWarehouse = line.substring("[dest:".length(), line.indexOf("]"));
                } else if (line.startsWith("[item:")) {
                    String itemStr = line.substring("[item:".length(), line.indexOf("]"));
                    String[] parts = itemStr.split("\\|");
                    if (parts.length >= 4) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("itemType", parts[0]);
                        item.put("itemId", Integer.parseInt(parts[1]));
                        item.put("quantity", Integer.parseInt(parts[2]));
                        item.put("weightPerUnit", Double.parseDouble(parts[3]));
                        items.add(item);
                    }
                }
            }

            if (mode == null || sourceWarehouse == null || items.isEmpty()) {
                action.setResult((action.getResult() != null ? action.getResult() : "") + "\n\n【搬运结算失败】搬运数据格式错误");
                action.setStatus(PlayerAction.ActionStatus.feedbacked);
                actionRepository.save(action);
                result.put("success", false); result.put("message", "搬运数据格式错误");
                return result;
            }

            int maxWeight = "warehouse_to_warehouse".equals(mode) ? 500 : 300;
            StringBuilder resultLog = new StringBuilder("【搬运结算】\n");
            int totalMoved = 0;

            String sourceTable = "warehouse_" + sourceWarehouse;
            if ("warehouse_to_warehouse".equals(mode) && destWarehouse != null) {
                String destTable = "warehouse_" + destWarehouse;
                for (Map<String, Object> item : items) {
                    String itemType = (String) item.get("itemType");
                    int itemId = (int) item.get("itemId");
                    int requestedQty = (int) item.get("quantity");
                    double weightPerUnit = (double) item.get("weightPerUnit");

                    int sourceQty = getWarehouseStock(sourceTable, itemType, itemId);
                    int maxQtyByWeight = (int) Math.floor((maxWeight - totalMoved) / Math.max(weightPerUnit, 0.1));
                    int actualQty = Math.min(Math.min(requestedQty, sourceQty), maxQtyByWeight);
                    if (actualQty <= 0) continue;

                    updateWarehouseStock(sourceTable, itemType, itemId, -actualQty);
                    updateWarehouseStock(destTable, itemType, itemId, actualQty);
                    totalMoved += (int)(actualQty * weightPerUnit);
                    resultLog.append(resolveItemName(itemType, itemId))
                            .append(": 搬运").append(actualQty).append("单位（")
                            .append((int)(actualQty * weightPerUnit)).append("千克）\n");
                }
            } else if ("warehouse_to_player".equals(mode)) {
                Integer playerId = action.getPlayerId();
                for (Map<String, Object> item : items) {
                    String itemType = (String) item.get("itemType");
                    int itemId = (int) item.get("itemId");
                    int requestedQty = (int) item.get("quantity");
                    double weightPerUnit = (double) item.get("weightPerUnit");

                    int sourceQty = getWarehouseStock(sourceTable, itemType, itemId);
                    int maxQtyByWeight = (int) Math.floor((maxWeight - totalMoved) / Math.max(weightPerUnit, 0.1));
                    int actualQty = Math.min(Math.min(requestedQty, sourceQty), maxQtyByWeight);
                    if (actualQty <= 0) continue;

                    updateWarehouseStock(sourceTable, itemType, itemId, -actualQty);
                    addItemToPlayer(playerId, itemType, itemId, actualQty);
                    totalMoved += (int)(actualQty * weightPerUnit);
                    resultLog.append(resolveItemName(itemType, itemId))
                            .append(": 搬运").append(actualQty).append("单位到个人背包（")
                            .append((int)(actualQty * weightPerUnit)).append("千克）\n");
                }
            }

            resultLog.append("总计搬运: ").append(totalMoved).append("千克");
            String existingResult = action.getResult() != null ? action.getResult() : "";
            action.setResult(existingResult + "\n\n" + resultLog.toString());
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            actionRepository.save(action);

            result.put("success", true);
            result.put("message", "搬运结算完成");
            result.put("data", toMap(action));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "搬运结算失败: " + e.getMessage());
        }
        return result;
    }

    private String resolveItemName(String itemType, int itemId) {
        String tableName;
        switch (itemType) {
            case "weapon": tableName = "weapon"; break;
            case "ammo": tableName = "ammo"; break;
            case "material": tableName = "material"; break;
            default: tableName = "item"; break;
        }
        try {
            Query query = entityManager.createNativeQuery("SELECT name FROM " + tableName + " WHERE id = ?1");
            query.setParameter(1, itemId);
            List<?> results = query.getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return results.get(0).toString();
            }
        } catch (Exception e) {
            // fall through
        }
        return "未知物品";
    }

    private int getWarehouseStock(String tableName, String itemType, int itemId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT quantity FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
            query.setParameter(1, itemType);
            query.setParameter(2, itemId);
            List<?> results = query.getResultList();
            if (!results.isEmpty()) return ((Number) results.get(0)).intValue();
        } catch (Exception e) { /* table may not exist */ }
        return 0;
    }

    private void updateWarehouseStock(String tableName, String itemType, int itemId, int delta) {
        try {
            Query checkQuery = entityManager.createNativeQuery(
                    "SELECT id, quantity FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
            checkQuery.setParameter(1, itemType);
            checkQuery.setParameter(2, itemId);
            List<?> results = checkQuery.getResultList();
            if (!results.isEmpty()) {
                Object[] row = (Object[]) results.get(0);
                int currentQty = ((Number) row[1]).intValue();
                int newQty = currentQty + delta;
                if (newQty <= 0) {
                    Query deleteQuery = entityManager.createNativeQuery(
                            "DELETE FROM " + tableName + " WHERE id = ?1");
                    deleteQuery.setParameter(1, ((Number) row[0]).intValue());
                    deleteQuery.executeUpdate();
                } else {
                    Query updateQuery = entityManager.createNativeQuery(
                            "UPDATE " + tableName + " SET quantity = ?1 WHERE id = ?2");
                    updateQuery.setParameter(1, newQty);
                    updateQuery.setParameter(2, ((Number) row[0]).intValue());
                    updateQuery.executeUpdate();
                }
            } else if (delta > 0) {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO " + tableName + " (item_type, item_id, quantity) VALUES (?1, ?2, ?3)");
                insertQuery.setParameter(1, itemType);
                insertQuery.setParameter(2, itemId);
                insertQuery.setParameter(3, delta);
                insertQuery.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("更新仓库库存失败: " + e.getMessage());
        }
    }

    private void addItemToPlayer(Integer playerId, String itemType, Integer itemId, Integer quantity) {
        try {
            Query checkQuery = entityManager.createNativeQuery(
                    "SELECT quantity FROM player_items WHERE player_id = ?1 AND item_type = ?2 AND item_id = ?3");
            checkQuery.setParameter(1, playerId);
            checkQuery.setParameter(2, itemType);
            checkQuery.setParameter(3, itemId);
            List<?> existing = checkQuery.getResultList();

            if (!existing.isEmpty()) {
                int currentQty = ((Number) existing.get(0)).intValue();
                Query updateQuery = entityManager.createNativeQuery(
                        "UPDATE player_items SET quantity = ?1 WHERE player_id = ?2 AND item_type = ?3 AND item_id = ?4");
                updateQuery.setParameter(1, currentQty + quantity);
                updateQuery.setParameter(2, playerId);
                updateQuery.setParameter(3, itemType);
                updateQuery.setParameter(4, itemId);
                updateQuery.executeUpdate();
            } else {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO player_items (player_id, item_type, item_id, quantity) VALUES (?1, ?2, ?3, ?4)");
                insertQuery.setParameter(1, playerId);
                insertQuery.setParameter(2, itemType);
                insertQuery.setParameter(3, itemId);
                insertQuery.setParameter(4, quantity);
                insertQuery.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("添加物品失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getProductionInfo(Integer playerId) {
        Map<String, Object> result = new HashMap<>();
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (!optPlayer.isPresent()) {
            result.put("canProduce", false);
            return result;
        }
        Player player = optPlayer.get();
        Integer jobId = player.getJobId();
        if (jobId == null) {
            result.put("canProduce", false);
            return result;
        }
        Optional<Job> optJob = jobRepository.findById(jobId);
        if (!optJob.isPresent()) {
            result.put("canProduce", false);
            return result;
        }
        String productionKey = PRODUCTION_JOB_MAP.get(optJob.get().getName());
        if (productionKey == null) {
            result.put("canProduce", false);
            return result;
        }
        result.put("canProduce", true);
        result.put("jobName", optJob.get().getName());
        result.put("productionKey", productionKey);
        result.put("productionInfo", PRODUCTION_OUTPUT_MAP.get(productionKey));
        return result;
    }

    public boolean isPlayerStealthed(Integer playerId, Integer gameDay) {
        return stealthRepository.existsByPlayerIdAndGameDay(playerId, gameDay);
    }

    private String getActionTypeLabel(String type) {
        switch (type) {
            case "go_location": return "前往地点";
            case "investigate_player": return "调查玩家";
            case "produce": return "生产";
            case "use_trait": return "使用特性";
            case "use_skill": return "使用职业技能";
            case "transport": return "搬运";
            case "hide": return "隐藏";
            default: return type;
        }
    }

    private Map<String, Object> toMap(PlayerAction action) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", action.getId());
        map.put("playerId", action.getPlayerId());
        map.put("playerName", action.getPlayerName());
        map.put("playerFaction", action.getPlayerFaction());
        map.put("actionSlot", action.getActionSlot());
        map.put("actionType", action.getActionType());
        map.put("actionTypeLabel", getActionTypeLabel(action.getActionType()));
        map.put("targetId", action.getTargetId());
        map.put("targetName", action.getTargetName());
        map.put("npcId", action.getNpcId());
        map.put("npcName", action.getNpcName());
        map.put("notes", action.getNotes());
        map.put("result", action.getResult());
        map.put("status", action.getStatus().name());
        map.put("gameDay", action.getGameDay());
        map.put("createdAt", action.getCreatedAt());
        return map;
    }
}
