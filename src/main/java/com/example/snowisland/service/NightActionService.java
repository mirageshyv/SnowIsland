package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NightActionService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final Map<String, Set<String>> NIGHT_ACTION_TYPES = new LinkedHashMap<>();

    static {
        NIGHT_ACTION_TYPES.put("统治者", new LinkedHashSet<>(Arrays.asList(
                "night_personal_action", "public_trial", "other")));
        NIGHT_ACTION_TYPES.put("反叛者", new LinkedHashSet<>(Arrays.asList(
                "pressure_ruler", "conspiracy", "other")));
        NIGHT_ACTION_TYPES.put("冒险者", new LinkedHashSet<>(Arrays.asList(
                "pressure_ruler", "publicity", "conspiracy", "other")));
        NIGHT_ACTION_TYPES.put("天灾使者", new LinkedHashSet<>(Arrays.asList(
                "conspiracy", "other")));
        NIGHT_ACTION_TYPES.put("平民", new LinkedHashSet<>(Arrays.asList(
                "conspiracy", "other")));
    }

    private static final Map<String, Set<String>> CONSPIRACY_SUBTYPES = new LinkedHashMap<>();

    static {
        CONSPIRACY_SUBTYPES.put("反叛者", new LinkedHashSet<>(Arrays.asList(
                "raid_location", "assassinate_ruler", "rescue_prisoner")));
        CONSPIRACY_SUBTYPES.put("冒险者", new LinkedHashSet<>(Collections.singletonList("raid_location")));
        CONSPIRACY_SUBTYPES.put("天灾使者", new LinkedHashSet<>(Arrays.asList(
                "raid_location", "spread_terror", "assassinate_target")));
        CONSPIRACY_SUBTYPES.put("平民", new LinkedHashSet<>(Arrays.asList(
                "raid_location", "assassinate_target")));
    }

    @Autowired private NightActionRepository nightActionRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private LocationNpcRepository npcRepository;
    @Autowired private PlayerActionRepository playerActionRepository;
    @Autowired private GameStateService gameStateService;

    public Map<String, Object> getContext(Integer playerId, Integer gameDay) {
        Map<String, Object> ctx = new HashMap<>();
        Optional<Player> opt = playerRepository.findById(playerId);
        if (!opt.isPresent()) {
            ctx.put("success", false);
            ctx.put("message", "玩家不存在");
            return ctx;
        }
        Player player = opt.get();
        String faction = player.getFaction() != null ? player.getFaction().name() : null;
        if (faction == null) {
            ctx.put("success", false);
            ctx.put("message", "无法确定阵营");
            ctx.put("faction", faction);
            return ctx;
        }

        boolean unlimitedActions = "统治者".equals(faction);
        List<NightAction> todayActions = nightActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(playerId, gameDay);
        Set<String> usedTypes = todayActions.stream().map(NightAction::getActionType).collect(Collectors.toSet());
        boolean hasSubmittedToday = !todayActions.isEmpty();

        boolean hasProduceToday = playerActionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay)
                .stream().anyMatch(a -> "produce".equals(a.getActionType()));

        ctx.put("success", true);
        ctx.put("playerId", playerId);
        ctx.put("playerName", player.getName());
        ctx.put("faction", faction);
        ctx.put("gameDay", gameDay);
        ctx.put("unlimitedActions", unlimitedActions);
        ctx.put("hasSubmittedToday", hasSubmittedToday);
        ctx.put("canSubmitMore", unlimitedActions || !hasSubmittedToday);
        ctx.put("usedActionTypes", usedTypes);
        ctx.put("hasProduceToday", hasProduceToday);
        ctx.put("allPlayers", getPlayerSummaries());
        ctx.put("allowedActionTypes", NIGHT_ACTION_TYPES.getOrDefault(faction, Collections.emptySet()));
        ctx.put("conspiracySubtypes", CONSPIRACY_SUBTYPES.getOrDefault(faction, Collections.emptySet()));
        ctx.put("history", todayActions.stream().map(this::toMap).collect(Collectors.toList()));
        gameStateService.enrichActionEditMeta(ctx, gameDay);
        return ctx;
    }

    @Transactional
    public Map<String, Object> submitAction(Integer playerId, String actionType, Map<String, Object> payload, Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        String editDeny = gameStateService.denyNightSubmit(gameDay);
        if (editDeny != null) {
            result.put("success", false);
            result.put("message", editDeny);
            return result;
        }
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (!optPlayer.isPresent()) {
            result.put("success", false);
            result.put("message", "玩家不存在");
            return result;
        }
        Player player = optPlayer.get();
        String faction = player.getFaction() != null ? player.getFaction().name() : null;
        if (faction == null) {
            result.put("success", false);
            result.put("message", "无法确定阵营");
            return result;
        }

        Set<String> allowed = NIGHT_ACTION_TYPES.get(faction);
        if (allowed == null || !allowed.contains(actionType)) {
            result.put("success", false);
            result.put("message", "当前阵营不可执行此夜晚行动");
            return result;
        }

        boolean unlimitedActions = "统治者".equals(faction);
        List<NightAction> todayActions = nightActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(playerId, gameDay);

        if (!unlimitedActions) {
            if (!todayActions.isEmpty()) {
                result.put("success", false);
                result.put("message", "今日已提交夜晚行动，每天仅可执行一次");
                return result;
            }
        } else if (todayActions.stream().anyMatch(a -> actionType.equals(a.getActionType()))) {
            result.put("success", false);
            result.put("message", "今日已使用过「" + getActionTypeLabel(actionType) + "」");
            return result;
        }

        String validationError = validateAction(actionType, payload, playerId, gameDay, faction);
        if (validationError != null) {
            result.put("success", false);
            result.put("message", validationError);
            return result;
        }

        NightAction action = new NightAction();
        action.setPlayerId(playerId);
        action.setPlayerName(player.getName());
        action.setFaction(faction);
        action.setActionType(actionType);
        action.setGameDay(gameDay);
        action.setStatus(NightAction.ActionStatus.pending);
        try {
            action.setPayload(JSON.writeValueAsString(payload != null ? payload : Collections.emptyMap()));
        } catch (Exception e) {
            action.setPayload("{}");
        }
        action.setResult(buildAutoResult(actionType, payload, player));
        nightActionRepository.save(action);

        result.put("success", true);
        result.put("message", "夜晚行动提交成功");
        result.put("data", toMap(action));
        return result;
    }

    private String validateAction(String actionType, Map<String, Object> payload, Integer playerId, Integer gameDay, String faction) {
        if (payload == null) payload = Collections.emptyMap();
        switch (actionType) {
            case "night_personal_action": {
                String at = str(payload.get("actionType"));
                if (at == null) return "请选择个人行动类型";
                if (("go_location".equals(at) || "investigate_player".equals(at)) && toInt(payload.get("targetId")) == null) {
                    return "请选择行动目标";
                }
                if ("produce".equals(at) && !playerActionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay)
                        .stream().anyMatch(a -> "produce".equals(a.getActionType()))) {
                    return "生产行动须白天已提交过生产类自由行动，或由主持人裁定";
                }
                if (("use_trait".equals(at) || "use_skill".equals(at))) {
                    String notes = str(payload.get("notes"));
                    if (notes == null || notes.trim().length() < 5) return "使用特性/技能时请在备注中详细描述";
                }
                if ("other".equals(at)) {
                    String notes = str(payload.get("notes"));
                    if (notes == null || notes.trim().length() < 5) return "请详细描述你想执行的具体行动内容";
                }
                return null;
            }
            case "public_trial":
                if (toInt(payload.get("targetPlayerId")) == null) return "请选择审判目标";
                return null;
            case "pressure_ruler":
                if (str(payload.get("demand")) == null) return "请选择施压诉求";
                return null;
            case "publicity": {
                String msg = str(payload.get("message"));
                if (msg == null || msg.trim().length() < 3) return "请填写宣传内容";
                return null;
            }
            case "conspiracy": {
                String sub = str(payload.get("conspiracySubtype"));
                Set<String> allowed = CONSPIRACY_SUBTYPES.get(faction);
                if (sub == null || allowed == null || !allowed.contains(sub)) return "请选择密谋类型";
                if ("spread_terror".equals(sub)) {
                    if (toInt(payload.get("targetLocationId")) == null && toInt(payload.get("targetPlayerId")) == null) {
                        return "请选择目标地点或目标玩家";
                    }
                } else if (toInt(payload.get("targetLocationId")) == null) {
                    return "请选择目标地点";
                }
                if (asList(payload.get("participantIds")).isEmpty()) return "请至少选择一名参与玩家";
                if ("raid_location".equals(sub) && str(payload.get("raidOutcome")) == null) {
                    return "请选择袭击成功后的意向（破坏/搜刮）";
                }
                return null;
            }
            case "other": {
                String note = str(payload.get("note"));
                if (note == null || note.trim().length() < 5) return "请详细描述你想执行的具体行动内容";
                return null;
            }
            default:
                return "未知行动类型";
        }
    }

    private String buildAutoResult(String actionType, Map<String, Object> payload, Player player) {
        if (payload == null) payload = Collections.emptyMap();
        StringBuilder sb = new StringBuilder();
        sb.append("✓ 已提交【").append(getActionTypeLabel(actionType)).append("】\n\n");
        sb.append("提交者：").append(player.getName()).append("\n");

        switch (actionType) {
            case "night_personal_action": {
                String at = str(payload.get("actionType"));
                sb.append("行动：").append(labelPersonalAction(at)).append("\n");
                if ("go_location".equals(at) || "investigate_player".equals(at)) {
                    sb.append("目标：").append(formatTarget(at, toInt(payload.get("targetId")), player.getId())).append("\n");
                }
                Integer npcId = toInt(payload.get("npcId"));
                if ("go_location".equals(at) && npcId != null) {
                    sb.append("交互NPC：").append(resolveNpcName(npcId)).append("\n");
                }
                break;
            }
            case "public_trial":
                sb.append("审判对象：").append(resolvePlayerName(toInt(payload.get("targetPlayerId")))).append("\n");
                break;
            case "pressure_ruler":
                sb.append("施压诉求：").append(labelDemand(str(payload.get("demand")))).append("\n");
                break;
            case "publicity":
                sb.append("宣传内容：").append(truncate(str(payload.get("message")), 120)).append("\n");
                break;
            case "conspiracy":
                sb.append("密谋类型：").append(labelConspiracySubtype(str(payload.get("conspiracySubtype")))).append("\n");
                Integer locId = toInt(payload.get("targetLocationId"));
                if (locId != null) sb.append("目标地点：").append(resolveLocationName(locId)).append("\n");
                sb.append("参与玩家：").append(String.join("、", namesFromIds(asList(payload.get("participantIds"))))).append("\n");
                if ("raid_location".equals(str(payload.get("conspiracySubtype")))) {
                    sb.append("成功后意向：").append("destroy".equals(str(payload.get("raidOutcome"))) ? "破坏地点" : "搜刮资源").append("\n");
                }
                break;
            default:
                break;
        }
        String note = str(payload.get("note"));
        if (note == null) note = str(payload.get("notes"));
        if (note != null && !note.trim().isEmpty()) sb.append("备注：").append(note.trim()).append("\n");
        sb.append("\n等待主持人在夜晚阶段结算。");
        return sb.toString();
    }

    public List<Map<String, Object>> getAllActions(Integer gameDay, String faction, String status) {
        List<NightAction> list;
        if (faction != null && !faction.isEmpty() && gameDay != null) {
            list = nightActionRepository.findByFactionAndGameDayOrderByCreatedAtAsc(faction, gameDay);
        } else if (gameDay != null) {
            list = nightActionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        } else {
            list = nightActionRepository.findAll();
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(a -> status.equals(a.getStatus().name())).collect(Collectors.toList());
        }
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> feedbackAction(Integer actionId, String feedback) {
        Map<String, Object> result = new HashMap<>();
        Optional<NightAction> opt = nightActionRepository.findById(actionId);
        if (!opt.isPresent()) {
            result.put("success", false);
            result.put("message", "行动不存在");
            return result;
        }
        NightAction action = opt.get();
        String existing = action.getResult() != null ? action.getResult() : "";
        action.setResult(existing + "\n\n【夜晚结算】\n" + feedback);
        action.setStatus(NightAction.ActionStatus.feedbacked);
        nightActionRepository.save(action);
        result.put("success", true);
        result.put("message", "结算反馈已保存");
        result.put("data", toMap(action));
        return result;
    }

    public String getActionTypeLabel(String type) {
        if (type == null) return "?";
        switch (type) {
            case "night_personal_action": return "夜晚个人行动";
            case "public_trial": return "公开审判";
            case "pressure_ruler": return "向统治者施压";
            case "publicity": return "公开宣传";
            case "conspiracy": return "进行密谋";
            case "other": return "其他";
            default: return type;
        }
    }

    private String labelPersonalAction(String key) {
        if (key == null) return "?";
        switch (key) {
            case "go_location": return "前往地点";
            case "investigate_player": return "调查玩家";
            case "produce": return "生产";
            case "use_trait": return "使用特性";
            case "use_skill": return "使用职业技能";
            case "hide": return "隐藏";
            case "other": return "其他";
            default: return key;
        }
    }

    private String labelDemand(String key) {
        if (key == null) return "?";
        switch (key) {
            case "labor_rotation": return "合理的劳工换班机制";
            case "expand_militia": return "扩大民兵";
            case "communal_resources": return "资源由全体镇民管理";
            case "ark_resources": return "增加方舟建设资源";
            default: return key;
        }
    }

    private String labelConspiracySubtype(String key) {
        if (key == null) return "?";
        switch (key) {
            case "raid_location": return "袭击地点";
            case "assassinate_ruler": return "暗杀统治者";
            case "rescue_prisoner": return "解救人员";
            case "spread_terror": return "制造恐怖";
            case "assassinate_target": return "暗杀目标";
            default: return key;
        }
    }

    private String formatTarget(String actionType, Integer targetId, Integer selfId) {
        if (targetId == null) return "未指定";
        if ("go_location".equals(actionType)) return resolveLocationName(targetId);
        if (selfId != null && targetId.equals(selfId)) return "自己";
        return resolvePlayerName(targetId);
    }

    private List<Map<String, Object>> getPlayerSummaries() {
        return playerRepository.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("faction", p.getFaction() != null ? p.getFaction().name() : null);
            return m;
        }).collect(Collectors.toList());
    }

    private String resolveLocationName(Integer id) {
        if (id == null) return "?";
        return locationRepository.findById(id).map(Location::getName).orElse("地点#" + id);
    }

    private String resolvePlayerName(Integer id) {
        if (id == null) return "?";
        return playerRepository.findById(id).map(Player::getName).orElse("玩家#" + id);
    }

    private String resolveNpcName(Integer id) {
        if (id == null) return "?";
        return npcRepository.findById(id).map(LocationNpc::getName).orElse("NPC#" + id);
    }

    private List<String> namesFromIds(List<?> ids) {
        List<String> names = new ArrayList<>();
        for (Object o : ids) {
            Integer id = toInt(o);
            if (id != null) names.add(resolvePlayerName(id));
        }
        return names;
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private Map<String, Object> toMap(NightAction action) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", action.getId());
        map.put("playerId", action.getPlayerId());
        map.put("playerName", action.getPlayerName());
        map.put("faction", action.getFaction());
        map.put("actionType", action.getActionType());
        map.put("actionTypeLabel", getActionTypeLabel(action.getActionType()));
        map.put("payload", parsePayload(action.getPayload()));
        map.put("result", action.getResult());
        map.put("status", action.getStatus().name());
        map.put("gameDay", action.getGameDay());
        map.put("createdAt", action.getCreatedAt());
        return map;
    }

    private Map<String, Object> parsePayload(String json) {
        if (json == null || json.isEmpty()) return Collections.emptyMap();
        try {
            return JSON.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return Collections.singletonMap("raw", json);
        }
    }

    private Integer toInt(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String str(Object v) {
        return v != null ? v.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private List<?> asList(Object v) {
        if (v instanceof List) return (List<?>) v;
        if (v instanceof Collection) return new ArrayList<>((Collection<?>) v);
        return Collections.emptyList();
    }
}
