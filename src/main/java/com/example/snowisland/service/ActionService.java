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
    @Autowired private ShelterService shelterService;
    @Autowired private WarehouseConfigRepository warehouseConfigRepository;
    @Autowired private TransportSettlementService transportSettlementService;
    @Autowired private GameStateService gameStateService;
    @Autowired private FactionActionRepository factionActionRepository;
    @Autowired private FactionActionService factionActionService;
    @Autowired private ActivityLogService activityLogService;

    private static final Random INVESTIGATE_ROLL = new Random();

    /** Shown to players while status is pending (DM sees full computed result). */
    public static final String PENDING_PLAYER_MESSAGE = "已提交，等待主持人确认。";

    private static final String DM_FEEDBACK_MARKER = "\n\n【DM反馈】\n";
    private static final String DM_FEEDBACK_MARKER_INLINE = "【DM反馈】";
    public static final String ACTION_FAILED_MARKER = "【行动失败】";

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

        String editDeny = gameStateService.denyDaytimeSubmit(gameDay);
        if (editDeny != null) {
            result.put("success", false);
            result.put("message", editDeny);
            return result;
        }

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
            autoResult = buildHideResultMessage();
        } else if ("use_trait".equals(actionType)) {
            autoResult = "等待DM反馈";
        } else if ("use_skill".equals(actionType)) {
            autoResult = "等待DM反馈";
        } else if ("transport".equals(actionType)) {
            autoResult = "等待DM反馈";
            TransportSettlementService.TransportPlan transportPlan =
                    transportSettlementService.parseNotes(notes, targetId);
            List<String> transportErrors = validateTransportSubmission(player.getId(), notes, targetId);
            if (!transportErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", String.join("；", transportErrors));
                return result;
            }
            if ("player_to_warehouse".equals(transportPlan.mode)) {
                String deductErr = transportSettlementService.deductPlayerItemsForSubmit(
                        transportPlan, player.getId());
                if (deductErr != null) {
                    result.put("success", false);
                    result.put("message", deductErr);
                    return result;
                }
                notes = transportSettlementService.appendPlayerDeductedMarker(notes);
                action.setNotes(notes);
                autoResult = "【搬运】个人背包已扣除，等待主持人确认入仓";
            }
        } else if ("other".equals(actionType)) {
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

        logActionSubmit(gameDay, player, actionSlot, actionType, action, notes);

        result.put("success", true);
        result.put("message", "个人行动提交成功");
        result.put("data", toMapForPlayer(action));
        return result;
    }

    public Map<String, Object> getSubmitContext(Integer playerId, Integer gameDay) {
        Map<String, Object> ctx = new LinkedHashMap<>();
        int day = gameDay != null && gameDay >= 1 ? gameDay : 1;
        boolean laborer = playerId != null && shelterService.isPlayerLaborerForDay(playerId, day);
        ctx.put("gameDay", day);
        ctx.put("isShelterLaborer", laborer);
        if (laborer) {
            ctx.put("laborerMessage", "你今日被指定为避难所劳工，按规定当天不应提交个人行动；但不管怎么样，想要试试也是可以的。");
        }
        gameStateService.enrichActionEditMeta(ctx, day);
        return ctx;
    }

    @Transactional
    public Map<String, Object> approveAction(Integer actionId) {
        Map<String, Object> result = new HashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false);
            result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        if (action.getStatus() != PlayerAction.ActionStatus.pending) {
            result.put("success", false);
            result.put("message", "该行动已处理");
            return result;
        }
        if ("hide".equals(action.getActionType())) {
            applyHideEffects(action);
        }
        action.setStatus(PlayerAction.ActionStatus.feedbacked);
        action.setFeedbackPublished(false);
        actionRepository.save(action);
        result.put("success", true);
        result.put("message", "行动已确认");
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

    private String buildHideResultMessage() {
        return "【隐藏】您将进入隐藏状态，明天将无法被调查、私聊或成为统治者与密谋的行动目标";
    }

    private void applyHideEffects(PlayerAction action) {
        int nextDay = action.getGameDay() + 1;
        Integer playerId = action.getPlayerId();
        if (playerId == null) {
            return;
        }
        if (!stealthRepository.existsByPlayerIdAndGameDay(playerId, nextDay)) {
            PlayerStealth stealth = new PlayerStealth();
            stealth.setPlayerId(playerId);
            stealth.setPlayerName(action.getPlayerName());
            stealth.setGameDay(nextDay);
            stealth.setSourceActionId(action.getId());
            stealthRepository.save(stealth);
        }
    }

    public List<Map<String, Object>> getPlayerActions(Integer playerId, Integer gameDay) {
        List<PlayerAction> actions = actionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay);
        return actions.stream().map(this::toMapForPlayer).collect(Collectors.toList());
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
    public Map<String, Object> feedbackAction(Integer actionId, String feedback, Boolean failed) {
        Map<String, Object> result = new HashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false); result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        String feedbackText = feedback != null ? feedback.trim() : "";
        boolean markFailed = Boolean.TRUE.equals(failed);
        boolean wasFailed = isActionFailed(action.getResult());

        if (markFailed) {
            refundPlayerTransportIfDeducted(action);
            String base = stripActionFailed(stripDmFeedback(action.getResult() != null ? action.getResult() : ""));
            base = TransportSettlementService.stripPendingBlock(base);
            action.setResult(attachDmFeedbackWithFlags(base, feedbackText, true));
        } else {
            if (wasFailed) {
                // 从失败改为成功时不再自动退还（主持人需自行处理库存）
            }
            String base = stripActionFailed(stripDmFeedback(action.getResult() != null ? action.getResult() : ""));
            action.setResult(attachDmFeedbackWithFlags(base, feedbackText, false));
        }
        action.setStatus(PlayerAction.ActionStatus.feedbacked);
        action.setFeedbackPublished(false);
        actionRepository.save(action);
        result.put("success", true);
        result.put("message", markFailed ? "失败反馈已保存" : "反馈已保存");
        result.put("data", toMap(action));
        return result;
    }

    @Transactional
    public Map<String, Object> publishFeedback(Integer gameDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (gameDay == null || gameDay < 1) {
            result.put("success", false);
            result.put("message", "无效的游戏日");
            return result;
        }
        List<PlayerAction> actions = actionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        int published = 0;
        int pending = 0;
        List<String> errors = new ArrayList<>();
        for (PlayerAction action : actions) {
            if (action.getStatus() == PlayerAction.ActionStatus.feedbacked) {
                if (!Boolean.TRUE.equals(action.getFeedbackPublished())) {
                    try {
                        boolean failed = isActionFailed(action.getResult());
                        if (!failed && "transport".equals(action.getActionType())) {
                            String execError = applyPendingTransportOnPublish(action);
                            if (execError != null) {
                                errors.add(action.getPlayerName() + "（行动" + action.getActionSlot() + "）：" + execError);
                                continue;
                            }
                        }
                        if (!failed && "hide".equals(action.getActionType())) {
                            applyHideEffects(action);
                        }
                        action.setFeedbackPublished(true);
                        actionRepository.save(action);
                        published++;
                    } catch (Exception e) {
                        errors.add(action.getPlayerName() + "（行动" + action.getActionSlot() + "）处理异常：" + e.getMessage());
                    }
                }
            } else {
                pending++;
            }
        }
        result.put("success", errors.isEmpty());
        result.put("publishedCount", published);
        result.put("pendingCount", pending);
        if (!errors.isEmpty()) {
            result.put("errors", errors);
            result.put("message", "发布完成 " + published + " 条，以下搬运未能执行：" + String.join("；", errors));
        } else {
            result.put("message", published > 0
                    ? "已发布 " + published + " 条行动反馈" + (pending > 0 ? "（另有 " + pending + " 条仍待处理）" : "")
                    : (pending > 0 ? "暂无新反馈可发布，仍有 " + pending + " 条待处理" : "当日暂无行动记录"));
        }
        return result;
    }

    private String applyPendingTransportOnPublish(PlayerAction action) {
        String payload = TransportSettlementService.extractPendingPayload(action.getResult());
        TransportSettlementService.TransportPlan plan;
        if (payload != null && !payload.isEmpty()) {
            plan = transportSettlementService.deserializePlan(payload);
            if (!plan.playerDeducted) {
                TransportSettlementService.TransportPlan fromNotes =
                        transportSettlementService.parseNotes(action.getNotes(), action.getTargetId());
                plan.playerDeducted = fromNotes.playerDeducted;
            }
        } else {
            plan = transportSettlementService.parseNotes(action.getNotes(), action.getTargetId());
            if (plan.mode == null || plan.mode.isEmpty()) {
                return null;
            }
            List<String> prepErrors = transportSettlementService.computeTransfer(plan, action.getPlayerId());
            if (!prepErrors.isEmpty()) {
                return String.join("；", prepErrors);
            }
        }
        if (plan.items.stream().noneMatch(i -> i.actualQty > 0)) {
            return "没有可执行的搬运物资";
        }
        String err = transportSettlementService.executePlan(plan, action.getPlayerId());
        if (err != null) {
            return err;
        }
        String base = TransportSettlementService.stripPendingBlock(action.getResult());
        if (base.contains("（库存变更将在发布反馈后生效）")) {
            base = base.replace("（库存变更将在发布反馈后生效）", "（库存已变更）");
        }
        if (base.contains("（个人背包已扣除，入仓将在发布反馈后生效）")) {
            base = base.replace("（个人背包已扣除，入仓将在发布反馈后生效）", "（个人背包已扣除，已入仓）");
        }
        action.setResult(base);
        return null;
    }

    @Transactional
    public Map<String, Object> batchResolveAll(Integer gameDay) {
        Map<String, Object> inv = batchResolveInvestigate(gameDay);
        Map<String, Object> prod = batchResolveProduce(gameDay);
        int simpleResolved = batchResolveGoLocationAndHide(gameDay);
        Map<String, Object> transport = batchResolveTransport(gameDay);
        Map<String, Object> result = new LinkedHashMap<>();
        int invResolved = inv.get("resolved") != null ? ((Number) inv.get("resolved")).intValue() : 0;
        int prodResolved = prod.get("resolved") != null ? ((Number) prod.get("resolved")).intValue() : 0;
        int transportResolved = transport.get("resolved") != null ? ((Number) transport.get("resolved")).intValue() : 0;
        result.put("success", true);
        result.put("investigateResolved", invResolved);
        result.put("produceResolved", prodResolved);
        result.put("simpleResolved", simpleResolved);
        result.put("transportResolved", transportResolved);
        StringBuilder msg = new StringBuilder("已结算调查 ").append(invResolved)
                .append(" 条、生产 ").append(prodResolved)
                .append(" 条、前往/隐藏 ").append(simpleResolved)
                .append(" 条、搬运 ").append(transportResolved).append(" 条");
        @SuppressWarnings("unchecked")
        List<String> transportErrors = (List<String>) transport.get("errors");
        if (transportErrors != null && !transportErrors.isEmpty()) {
            msg.append("；搬运跳过/失败：").append(String.join("；", transportErrors));
        }
        result.put("message", msg.toString());
        result.put("transportErrors", transport.get("errors"));
        return result;
    }

    private int batchResolveGoLocationAndHide(Integer gameDay) {
        List<PlayerAction> actions = actionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        int resolved = 0;
        for (PlayerAction action : actions) {
            if (action.getStatus() != PlayerAction.ActionStatus.pending) {
                continue;
            }
            String type = action.getActionType();
            if (!"go_location".equals(type) && !"hide".equals(type)) {
                continue;
            }
            if ("hide".equals(type)) {
                applyHideEffects(action);
            }
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            action.setFeedbackPublished(false);
            actionRepository.save(action);
            resolved++;
        }
        return resolved;
    }

    @Transactional
    public Map<String, Object> updateActionByDm(Integer actionId, Map<String, Object> body) {
        Map<String, Object> result = new LinkedHashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false);
            result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        if (body.containsKey("notes")) {
            action.setNotes(body.get("notes") != null ? body.get("notes").toString() : null);
        }
        if (body.containsKey("actionType")) {
            action.setActionType(body.get("actionType").toString());
        }
        if (body.containsKey("targetId")) {
            action.setTargetId(toInt(body.get("targetId")));
        }
        if (body.containsKey("targetName")) {
            action.setTargetName(body.get("targetName") != null ? body.get("targetName").toString() : null);
        }
        if (body.containsKey("npcId")) {
            action.setNpcId(toInt(body.get("npcId")));
        }
        if (body.containsKey("npcName")) {
            action.setNpcName(body.get("npcName") != null ? body.get("npcName").toString() : null);
        }
        if ("transport".equals(action.getActionType())) {
            List<String> transportErrors = validateTransportSubmission(
                    action.getPlayerId(), action.getNotes(), action.getTargetId());
            if (!transportErrors.isEmpty()) {
                result.put("success", false);
                result.put("message", String.join("；", transportErrors));
                return result;
            }
        }
        actionRepository.save(action);
        result.put("success", true);
        result.put("message", "行动已更新");
        result.put("data", toMap(action));
        return result;
    }

    private List<String> validateTransportSubmission(Integer playerId, String notes, Integer targetId) {
        TransportSettlementService.TransportPlan plan =
                transportSettlementService.parseNotes(notes, targetId);
        List<String> errors = new ArrayList<>();
        errors.addAll(transportSettlementService.validatePlanStructure(plan));
        errors.addAll(transportSettlementService.validateKeys(playerId, plan));
        return errors;
    }

    private Integer toInt(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String extractDmFeedback(String result) {
        if (result == null || result.isEmpty()) {
            return "";
        }
        int marker = result.indexOf("【DM反馈】");
        if (marker < 0) {
            return "";
        }
        int contentStart = result.indexOf('\n', marker);
        if (contentStart < 0) {
            return "";
        }
        return result.substring(contentStart + 1).trim();
    }

    private static String stripDmFeedback(String result) {
        if (result == null || result.isEmpty()) {
            return "";
        }
        int marker = result.indexOf(DM_FEEDBACK_MARKER);
        if (marker >= 0) {
            return result.substring(0, marker).trim();
        }
        marker = result.indexOf("【DM反馈】");
        if (marker >= 0) {
            return result.substring(0, marker).trim();
        }
        return result.trim();
    }

    public static boolean isActionFailed(String result) {
        return result != null && result.contains(ACTION_FAILED_MARKER);
    }

    private static String stripActionFailed(String result) {
        if (result == null || result.isEmpty()) {
            return "";
        }
        String text = result;
        int idx = text.indexOf(ACTION_FAILED_MARKER);
        if (idx >= 0) {
            text = text.substring(0, idx).trim();
            if (text.endsWith("\n\n")) {
                text = text.substring(0, text.length() - 2).trim();
            }
        }
        return text.trim();
    }

    private static String attachDmFeedbackWithFlags(String base, String feedback, boolean failed) {
        String cleaned = stripActionFailed(stripDmFeedback(base != null ? base : ""));
        cleaned = TransportSettlementService.stripPendingBlock(cleaned);
        StringBuilder sb = new StringBuilder();
        if (!cleaned.isEmpty()) {
            sb.append(cleaned);
        }
        if (failed) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(ACTION_FAILED_MARKER);
        }
        if (feedback != null && !feedback.isEmpty()) {
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append(DM_FEEDBACK_MARKER_INLINE).append("\n").append(feedback);
        }
        return sb.toString();
    }

    private void refundPlayerTransportIfDeducted(PlayerAction action) {
        if (!"transport".equals(action.getActionType())) {
            return;
        }
        String notes = action.getNotes() != null ? action.getNotes() : "";
        if (!notes.contains("[player_deducted:1]") || notes.contains("[player_refunded:1]")) {
            return;
        }
        TransportSettlementService.TransportPlan plan =
                transportSettlementService.parseNotes(notes, action.getTargetId());
        if (!"player_to_warehouse".equals(plan.mode)) {
            return;
        }
        for (TransportSettlementService.TransportItem item : plan.items) {
            if (item.requestedQty <= 0) {
                continue;
            }
            addItemToPlayer(action.getPlayerId(), item.itemType, item.itemId, item.requestedQty);
        }
        String updatedNotes = notes.replace("[player_deducted:1]", "[player_refunded:1]");
        if (!updatedNotes.contains("[player_refunded:1]")) {
            updatedNotes = updatedNotes + (updatedNotes.isEmpty() ? "" : "\n") + "[player_refunded:1]";
        }
        action.setNotes(updatedNotes);
    }

    private Map<String, Object> toMapForPlayer(PlayerAction action) {
        Map<String, Object> map = toMap(action);
        boolean published = Boolean.TRUE.equals(action.getFeedbackPublished());
        boolean showToPlayer = action.getStatus() == PlayerAction.ActionStatus.feedbacked && published;
        if (!showToPlayer) {
            map.put("result", PENDING_PLAYER_MESSAGE);
            map.put("resultPending", true);
        } else {
            map.put("resultPending", false);
            String visible = extractDmFeedback(action.getResult());
            if (visible.isEmpty()) {
                visible = stripDmFeedback(action.getResult());
            }
            map.put("result", visible);
        }
        return map;
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
                if (targetActions.isEmpty()) {
                    sb.append("（当日未提交个人行动）\n");
                } else {
                    for (PlayerAction ta : targetActions) {
                        sb.append("行动").append(ta.getActionSlot()).append("：")
                          .append(getActionTypeLabel(ta.getActionType()));
                        if (ta.getTargetName() != null) {
                            sb.append(" → ").append(ta.getTargetName());
                        }
                        sb.append("\n");
                    }
                }
                appendFactionInvestigateIntel(sb, action.getTargetName(), targetPlayerId, gameDay);
                action.setResult(sb.toString());
            }
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            action.setFeedbackPublished(false);
            actionRepository.save(action);
            resolved++;
        }
        result.put("success", true);
        result.put("message", "已结算" + resolved + "条调查行动");
        result.put("resolved", resolved);
        return result;
    }

    /** 50% chance per investigate to reveal target's submitted faction actions for that day. */
    private void appendFactionInvestigateIntel(StringBuilder sb, String targetName, int targetPlayerId, int gameDay) {
        sb.append("\n");
        if (!INVESTIGATE_ROLL.nextBoolean()) {
            sb.append("未能探知").append(targetName).append("的阵营行动（概率未中）。\n");
            return;
        }
        List<FactionAction> factionActions =
                factionActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(targetPlayerId, gameDay);
        sb.append(targetName).append("的阵营行动：\n");
        if (factionActions == null || factionActions.isEmpty()) {
            sb.append("（当日未提交阵营行动）\n");
            return;
        }
        List<FactionAction> ordered = new ArrayList<>(factionActions);
        Collections.reverse(ordered);
        int idx = 1;
        for (FactionAction fa : ordered) {
            sb.append("阵营行动").append(idx++).append("：")
              .append(factionActionService.summarizeForInvestigate(fa))
              .append("\n");
        }
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
            action.setFeedbackPublished(false);
            actionRepository.save(action);
            resolved++;
        }
        result.put("success", true);
        result.put("message", "已结算" + resolved + "条生产行动");
        result.put("resolved", resolved);
        return result;
    }

    /**
     * Preview transport settlement on action (mutates result). Returns error message or null on success.
     */
    private String applyTransportSettlement(PlayerAction action) {
        if (!"transport".equals(action.getActionType())) {
            return "不是搬运行动";
        }
        String notes = action.getNotes() != null ? action.getNotes() : "";
        TransportSettlementService.TransportPlan plan =
                transportSettlementService.parseNotes(notes, action.getTargetId());

        List<String> errors = new ArrayList<>();
        errors.addAll(transportSettlementService.validatePlanStructure(plan));
        errors.addAll(transportSettlementService.validateKeys(action.getPlayerId(), plan));
        errors.addAll(transportSettlementService.computeTransfer(plan, action.getPlayerId()));

        if (!errors.isEmpty()) {
            return String.join("；", errors);
        }

        String log = transportSettlementService.buildResultLog(plan);
        String payload = transportSettlementService.serializePlan(plan);
        action.setResult(transportSettlementService.attachPendingPayload(log, payload));
        return null;
    }

    private boolean transportAlreadySettled(PlayerAction action) {
        String existing = action.getResult() != null ? action.getResult() : "";
        return existing.contains(TransportSettlementService.SETTLEMENT_HEADER)
                || TransportSettlementService.extractPendingPayload(existing) != null;
    }

    @Transactional
    public Map<String, Object> batchResolveTransport(Integer gameDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<PlayerAction> actions =
                actionRepository.findByActionTypeAndGameDayOrderByCreatedAtAsc("transport", gameDay);
        int resolved = 0;
        List<String> errors = new ArrayList<>();
        for (PlayerAction action : actions) {
            if (action.getStatus() != PlayerAction.ActionStatus.pending) {
                continue;
            }
            if (isActionFailed(action.getResult())) {
                continue;
            }
            if (transportAlreadySettled(action)) {
                continue;
            }
            String err = applyTransportSettlement(action);
            if (err != null) {
                String label = (action.getPlayerName() != null ? action.getPlayerName() : "玩家")
                        + " 行动" + action.getActionSlot();
                errors.add(label + ": " + err);
                continue;
            }
            action.setStatus(PlayerAction.ActionStatus.feedbacked);
            action.setFeedbackPublished(false);
            actionRepository.save(action);
            resolved++;
        }
        result.put("success", true);
        result.put("resolved", resolved);
        result.put("errors", errors);
        result.put("message", "已结算 " + resolved + " 条搬运行动");
        return result;
    }

    @Transactional
    public Map<String, Object> resolveTransport(Integer actionId) {
        Map<String, Object> result = new HashMap<>();
        Optional<PlayerAction> optAction = actionRepository.findById(actionId);
        if (!optAction.isPresent()) {
            result.put("success", false);
            result.put("message", "行动不存在");
            return result;
        }
        PlayerAction action = optAction.get();
        if (transportAlreadySettled(action)) {
            result.put("success", false);
            result.put("message", "搬运已结算");
            return result;
        }

        String err = applyTransportSettlement(action);
        if (err != null) {
            result.put("success", false);
            result.put("message", err);
            return result;
        }

        action.setStatus(PlayerAction.ActionStatus.feedbacked);
        action.setFeedbackPublished(false);
        actionRepository.save(action);

        result.put("success", true);
        result.put("message", "搬运已结算（发布反馈后生效）");
        result.put("data", toMap(action));
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

    private String resolveWarehouseName(String warehouseKey) {
        if (warehouseKey == null || warehouseKey.isEmpty()) return "未知仓库";
        Optional<WarehouseConfig> opt = warehouseConfigRepository.findByWarehouseKey(warehouseKey);
        if (opt.isPresent() && opt.get().getWarehouseName() != null && !opt.get().getWarehouseName().isEmpty()) {
            return opt.get().getWarehouseName();
        }
        switch (warehouseKey) {
            case "general": return "通用仓库";
            case "fuel": return "燃料仓库";
            case "armory": return "镇武库";
            case "dock": return "码头集换站";
            case "rebel": return "反叛者基地";
            case "ark": return "方舟仓库";
            default: return warehouseKey;
        }
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
            case "other": return "其他";
            default: return type;
        }
    }

    private void logActionSubmit(Integer gameDay, Player player, Integer actionSlot, String actionType,
                                 PlayerAction action, String notes) {
        StringBuilder detail = new StringBuilder();
        if (action.getTargetName() != null) {
            detail.append("目标:").append(action.getTargetName()).append(" ");
        }
        if (action.getNpcName() != null) {
            detail.append("NPC:").append(action.getNpcName()).append(" ");
        }
        if (notes != null && !notes.trim().isEmpty()) {
            if ("transport".equals(actionType)) {
                String zh = transportSettlementService.formatNotesChinese(notes);
                if (zh != null) {
                    detail.append(zh);
                }
            } else {
                detail.append(ActivityLogService.truncate(notes.trim(), 240));
            }
        }
        String summary = "自由#" + actionSlot + "·" + getActionTypeLabel(actionType);
        activityLogService.log(
                gameDay,
                player.getId(),
                player.getName(),
                ActivityLogService.factionOf(player),
                ActivityLogService.CAT_ACTION,
                summary,
                detail.length() > 0 ? detail.toString().trim() : null);
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
        map.put("feedbackPublished", Boolean.TRUE.equals(action.getFeedbackPublished()));
        map.put("dmFeedback", extractDmFeedback(action.getResult()));
        map.put("actionFailed", isActionFailed(action.getResult()));
        map.put("gameDay", action.getGameDay());
        map.put("createdAt", action.getCreatedAt());
        boolean laborer = action.getPlayerId() != null && action.getGameDay() != null
                && shelterService.isPlayerLaborerForDay(action.getPlayerId(), action.getGameDay());
        map.put("playerIsShelterLaborer", laborer);
        if (laborer) {
            map.put("laborerDmWarning",
                    "该玩家已列入第 " + action.getGameDay() + " 天避难所劳工名单，按规定不应提交个人行动。");
        }
        return map;
    }
}
