package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FactionActionService {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Set<String> GUARD_JOBS = new HashSet<>(Arrays.asList("民兵", "治安官"));
    private static final Map<String, Set<String>> FACTION_ACTION_TYPES = new LinkedHashMap<>();

    static {

        FACTION_ACTION_TYPES.put("统治者", new LinkedHashSet<>(Arrays.asList(
                "assign_personnel", "assign_guard")));
        FACTION_ACTION_TYPES.put("反叛者", new LinkedHashSet<>(Arrays.asList(
                "extra_labor", "secret_contact", "sabotage")));
        FACTION_ACTION_TYPES.put("冒险者", new LinkedHashSet<>(Arrays.asList(
                "extra_investigate", "extra_labor", "guard_ark", "ark_construction")));
        FACTION_ACTION_TYPES.put("天灾使者", new LinkedHashSet<>(Arrays.asList(
                "sabotage", "extra_investigate", "curse")));
    }

    @Autowired private FactionActionRepository factionActionRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private JobRepository jobRepository;
    @Autowired private LocationRepository locationRepository;
    @Autowired private LocationFacilityRepository facilityRepository;
    @Autowired private LocationNpcRepository npcRepository;
    @Autowired private PlayerActionRepository playerActionRepository;
    @Autowired private ArkService arkService;
    @Autowired private ShelterService shelterService;
    @Autowired private EntityManager entityManager;

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
        if (faction == null || "平民".equals(faction)) {
            ctx.put("success", false);
            ctx.put("message", "平民无法使用阵营行动");
            ctx.put("faction", faction);
            return ctx;
        }

        boolean unlimitedActions = "统治者".equals(faction);
        List<FactionAction> todayActions = factionActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(playerId, gameDay);
        Set<String> usedTypes = todayActions.stream().map(FactionAction::getActionType).collect(Collectors.toSet());
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
        ctx.put("assignPersonnelUsedToday", usedTypes.contains("assign_personnel"));
        ctx.put("assignGuardUsedToday", usedTypes.contains("assign_guard"));
        ctx.put("governedLocationIds", Collections.emptyList());
        ctx.put("hasProduceToday", hasProduceToday);
        ctx.put("highThreatWeapons", getHighThreatWeapons(playerId));
        ctx.put("arkStatus", arkService.getStatus());
        ctx.put("allowedActionTypes", FACTION_ACTION_TYPES.getOrDefault(faction, Collections.emptySet()));
        ctx.put("militiaPlayers", getPlayersByJobs(GUARD_JOBS));
        ctx.put("allPlayers", getPlayerSummaries());
        ctx.put("laborCandidates", getLaborCandidates(gameDay));
        ctx.put("history", todayActions.stream().map(this::toMap).collect(Collectors.toList()));

        return ctx;
    }

    @Transactional
    public Map<String, Object> submitAction(Integer playerId, String actionType, Map<String, Object> payload, Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        Optional<Player> optPlayer = playerRepository.findById(playerId);
        if (!optPlayer.isPresent()) {
            result.put("success", false);
            result.put("message", "玩家不存在");
            return result;
        }
        Player player = optPlayer.get();
        String faction = player.getFaction() != null ? player.getFaction().name() : null;
        if (faction == null || "平民".equals(faction)) {
            result.put("success", false);
            result.put("message", "平民无法提交阵营行动");
            return result;
        }

        Set<String> allowed = FACTION_ACTION_TYPES.get(faction);
        if (allowed == null || !allowed.contains(actionType)) {
            result.put("success", false);
            result.put("message", "当前阵营不可执行此行动");
            return result;
        }

        boolean unlimitedActions = "统治者".equals(faction);
        List<FactionAction> todayActions = factionActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(playerId, gameDay);

        if (!unlimitedActions) {
            if (!todayActions.isEmpty()) {
                result.put("success", false);
                result.put("message", "今日已提交阵营行动，每天仅可执行一次");
                return result;
            }
        } else if (todayActions.stream().anyMatch(a -> actionType.equals(a.getActionType()))) {
            result.put("success", false);
            result.put("message", "今日已使用过「" + getActionTypeLabel(actionType) + "」");
            return result;
        }

        String validationError = validateAction(actionType, payload, playerId, gameDay, faction, todayActions);
        if (validationError != null) {
            result.put("success", false);
            result.put("message", validationError);
            return result;
        }

        FactionAction action = new FactionAction();
        action.setPlayerId(playerId);
        action.setPlayerName(player.getName());
        action.setFaction(faction);
        action.setActionType(actionType);
        action.setGameDay(gameDay);
        action.setStatus(FactionAction.ActionStatus.pending);
        try {
            action.setPayload(JSON.writeValueAsString(payload != null ? payload : Collections.emptyMap()));
        } catch (Exception e) {
            action.setPayload("{}");
        }

        String autoResult = buildAutoResult(actionType, payload, player, gameDay);
        action.setResult(autoResult);

        factionActionRepository.save(action);
        applySideEffects(action, payload, gameDay);

        result.put("success", true);
        result.put("message", "阵营行动提交成功");
        result.put("data", toMap(action));
        return result;
    }

    private String validateAction(String actionType, Map<String, Object> payload, Integer playerId, Integer gameDay,
                                  String faction, List<FactionAction> todayActions) {
        if (payload == null) payload = Collections.emptyMap();

        switch (actionType) {
            case "assign_personnel": {
                if (toInt(payload.get("targetId")) == null) return "请选择目标";
                List<Map<String, Object>> assigned = parseAssignedActions(payload);
                if (assigned.isEmpty()) return "请至少指定一项对方须提交的自由行动";
                if (assigned.size() > 2) return "最多指定两项自由行动";
                for (Map<String, Object> item : assigned) {
                    if (item.get("action") == null) return "请完整选择指定自由行动";
                }
                return null;
            }
            case "assign_guard": {
                Integer actorId = toInt(payload.get("actorId"));
                Integer locationId = toInt(payload.get("targetLocationId"));
                if (actorId == null || locationId == null) return "请选择看守人员与地点";
                if (str(payload.get("assignedAction")) == null) return "请选择对方须提交的夜晚行动";
                return null;
            }
            case "extra_labor": {
                if (!playerActionRepository.findByPlayerIdAndGameDayOrderByActionSlotAsc(playerId, gameDay)
                        .stream().anyMatch(a -> "produce".equals(a.getActionType())))
                    return "今日须已提交生产类自由行动";
                return null;
            }
            case "secret_contact": {
                if (toInt(payload.get("targetPlayerId")) == null) return "请选择目标玩家";
                String msg = str(payload.get("message"));
                if (msg == null || msg.trim().length() < 3) return "请填写秘密信息";
                return null;
            }
            case "sabotage": {
                Integer locationId = toInt(payload.get("targetLocationId"));
                Integer facilityId = toInt(payload.get("facilityId"));
                if (locationId == null || facilityId == null) return "请选择地点与设施";
                return null;
            }
            case "extra_investigate": {
                if (payload.get("investigateType") == null) return "请选择调查类型";
                Integer targetId = toInt(payload.get("targetId"));
                if (targetId == null) targetId = toInt(payload.get("target1"));
                if (targetId == null) return "请选择调查目标";
                return null;
            }
            case "guard_ark": {
                if (toInt(payload.get("guardId")) == null) return "请选择看守人员";
                return null;
            }
            case "ark_construction": {
                Integer points = toInt(payload.get("actionPoints"));
                if (points == null || points < 1) return "请填写至少1点行动点";
                return null;
            }
            case "curse": {
                Integer weaponId = toInt(payload.get("weaponId"));
                if (weaponId == null) return "请选择武器";
                int threat = getWeaponThreat(weaponId);
                if (threat < 4) return "武器威胁值须不低于4";
                if (toInt(payload.get("target1")) == null) return "请至少选择一个目标";
                return null;
            }
            default:
                return "未知行动类型";
        }
    }

    private String buildAutoResult(String actionType, Map<String, Object> payload, Player player, Integer gameDay) {
        if (payload == null) payload = Collections.emptyMap();
        Integer selfId = player.getId();
        switch (actionType) {
            case "assign_personnel": {
                String target = resolveActorName(toInt(payload.get("targetId")), str(payload.get("targetKind")));
                List<Map<String, Object>> assignedList = parseAssignedActions(payload);
                String note = str(payload.get("note"));
                StringBuilder sb = new StringBuilder();
                sb.append("✓ 已提交【安排人员】\n\n");
                sb.append("目标：").append(target).append("\n");
                sb.append("须提交的自由行动（共").append(assignedList.size()).append("项）：\n");
                int idx = 1;
                for (Map<String, Object> item : assignedList) {
                    String actionKey = str(item.get("action"));
                    sb.append("  ").append(idx++).append(". ").append(labelAssignedAction(actionKey));
                    Integer locId = toInt(item.get("targetLocationId"));
                    if (locId != null) sb.append("（").append(resolveLocationName(locId)).append("）");
                    sb.append("\n");
                }
                if (note != null && !note.trim().isEmpty()) sb.append("附加说明：").append(note.trim()).append("\n");
                sb.append("\n对方须提交与上述一致的行动，可拒绝（可作为审判理由）。等待主持人裁定。");
                return sb.toString();
            }
            case "assign_guard": {
                Integer actorId = toInt(payload.get("actorId"));
                int weaponBonus = Boolean.TRUE.equals(payload.get("armed")) ? getBestWeaponThreat(actorId) : 0;
                int total = 3 + weaponBonus;
                String actor = resolvePlayerName(actorId);
                String loc = resolveLocationName(toInt(payload.get("targetLocationId")));
                String nightAction = labelNightAssignedAction(str(payload.get("assignedAction")));
                StringBuilder sb = new StringBuilder();
                sb.append("✓ 已提交【安排看守】\n\n");
                sb.append("看守人员：").append(actor).append("\n");
                sb.append("看守地点：").append(loc).append("\n");
                sb.append("须提交的夜晚行动：").append(nightAction).append("\n");
                sb.append("消耗对方夜晚行动点：是\n");
                sb.append("计入武器：").append(Boolean.TRUE.equals(payload.get("armed")) ? "是" : "否").append("\n");
                sb.append("基础防御：+3\n");
                if (weaponBonus > 0) sb.append("武器威胁加成：+").append(weaponBonus).append("\n");
                sb.append("总防御值：").append(total).append("\n");
                sb.append("\n对方须提交与上述一致的夜晚行动。等待主持人确认。");
                return sb.toString();
            }
            case "extra_labor":
                return "✓ 已提交【额外劳动】\n\n"
                        + "提交者：" + player.getName() + "\n"
                        + "效果：今日生产类自由行动产出 +50%。\n"
                        + "（须今日已提交生产行动）\n等待主持人确认。";
            case "secret_contact": {
                String target = resolvePlayerName(toInt(payload.get("targetPlayerId")));
                String msg = str(payload.get("message"));
                boolean anon = Boolean.TRUE.equals(payload.get("anonymous"));
                String preview = msg != null && msg.length() > 80 ? msg.substring(0, 80) + "…" : msg;
                return "✓ 已提交【暗中联络】\n\n"
                        + "收件人：" + target + "\n"
                        + "匿名发送：" + (anon ? "是" : "否") + "\n"
                        + "信息摘要：" + (preview != null ? preview : "—") + "\n\n"
                        + "仅主持人与目标可见。等待主持人确认。";
            }
            case "sabotage": {
                String fac = resolveFacilityName(toInt(payload.get("facilityId")));
                String loc = resolveLocationName(toInt(payload.get("targetLocationId")));
                return "✓ 已提交【破坏】\n\n"
                        + "目标地点：" + loc + "\n"
                        + "目标设施：" + fac + "\n\n"
                        + "等待主持人确认。";
            }
            case "extra_investigate":
                return formatExtraInvestigateResult(payload, selfId);
            case "guard_ark": {
                String guard = resolvePlayerName(toInt(payload.get("guardId")));
                int w = Boolean.TRUE.equals(payload.get("useWeaponOrSkill")) ? 2 : 0;
                return "✓ 已提交【看守方舟】\n\n"
                        + "看守人员：" + guard + "\n"
                        + "使用武器/技能计入防御：" + (Boolean.TRUE.equals(payload.get("useWeaponOrSkill")) ? "是" : "否") + "\n"
                        + "额外防御：+" + w + "\n\n"
                        + "等待主持人确认。";
            }
            case "ark_construction": {
                Map<String, Object> ark = arkService.getStatus();
                Object pct = ark.get("completionPercentage");
                int points = toInt(payload.get("actionPoints")) != null ? toInt(payload.get("actionPoints")) : 1;
                boolean special = Boolean.TRUE.equals(payload.get("useSpecialMaterials"));
                String note = str(payload.get("note"));
                StringBuilder sb = new StringBuilder();
                sb.append("✓ 已提交【方舟建设】\n\n");
                sb.append("提交者：").append(player.getName()).append("\n");
                sb.append("消耗行动点：").append(points).append("\n");
                sb.append("使用特殊材料：").append(special ? "是" : "否").append("\n");
                sb.append("当前方舟进度：").append(pct != null ? pct : "?").append("%\n");
                if (note != null && !note.trim().isEmpty()) sb.append("备注：").append(note.trim()).append("\n");
                sb.append("\n等待主持人确认。");
                return sb.toString();
            }
            case "curse": {
                String wName = resolveWeaponName(toInt(payload.get("weaponId")));
                String t1 = formatInvestigateTarget("investigate_player", toInt(payload.get("target1")), selfId);
                Integer t2Id = toInt(payload.get("target2"));
                StringBuilder sb = new StringBuilder();
                sb.append("✓ 已提交【诅咒】\n\n");
                sb.append("消耗武器：").append(wName).append("\n");
                sb.append("目标1：").append(t1).append("\n");
                if (t2Id != null) sb.append("目标2：").append(formatInvestigateTarget("investigate_player", t2Id, selfId)).append("\n");
                sb.append("\n效果：获知阵营、施加「诅咒」标记。\n等待主持人确认。");
                return sb.toString();
            }
            default:
                return "已提交，等待主持人反馈。";
        }
    }

    private String formatExtraInvestigateResult(Map<String, Object> payload, Integer selfPlayerId) {
        String investigateType = str(payload.get("investigateType"));
        Integer targetId = toInt(payload.get("targetId"));
        if (targetId == null) targetId = toInt(payload.get("target1"));

        return "✓ 已提交【额外调查】\n\n"
                + "调查类型：" + labelInvestigateType(investigateType) + "\n"
                + "调查目标：" + formatInvestigateTarget(investigateType, targetId, selfPlayerId) + "\n\n"
                + "结算后该次调查数量将翻倍。等待主持人确认。";
    }

    private String formatInvestigateTarget(String investigateType, Integer targetId, Integer selfPlayerId) {
        if (targetId == null) return "未指定";
        if ("investigate_location".equals(investigateType)) {
            return resolveLocationName(targetId);
        }
        if (selfPlayerId != null && targetId.equals(selfPlayerId)) {
            return "自己";
        }
        return resolvePlayerName(targetId);
    }

    private String labelInvestigateType(String key) {
        if ("investigate_location".equals(key)) return "调查地点";
        if ("investigate_player".equals(key)) return "调查玩家";
        return key != null ? key : "?";
    }

    private void applySideEffects(FactionAction action, Map<String, Object> payload, Integer gameDay) {
        // 无自动副作用；由主持人裁定
    }

    public List<Map<String, Object>> getPlayerHistory(Integer playerId, Integer gameDay) {
        return factionActionRepository.findByPlayerIdAndGameDayOrderByCreatedAtDesc(playerId, gameDay)
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllActions(Integer gameDay, String faction, String status) {
        List<FactionAction> list;
        if (faction != null && !faction.isEmpty() && gameDay != null) {
            list = factionActionRepository.findByFactionAndGameDayOrderByCreatedAtAsc(faction, gameDay);
        } else if (gameDay != null) {
            list = factionActionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        } else {
            list = factionActionRepository.findAll();
        }
        if (status != null && !status.isEmpty()) {
            list = list.stream().filter(a -> status.equals(a.getStatus().name())).collect(Collectors.toList());
        }
        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> feedbackAction(Integer actionId, String feedback) {
        Map<String, Object> result = new HashMap<>();
        Optional<FactionAction> opt = factionActionRepository.findById(actionId);
        if (!opt.isPresent()) {
            result.put("success", false);
            result.put("message", "行动不存在");
            return result;
        }
        FactionAction action = opt.get();
        String existing = action.getResult() != null ? action.getResult() : "";
        action.setResult(existing + "\n\n【DM反馈】\n" + feedback);
        action.setStatus(FactionAction.ActionStatus.feedbacked);
        factionActionRepository.save(action);
        result.put("success", true);
        result.put("message", "反馈成功");
        result.put("data", toMap(action));
        return result;
    }

    private List<Map<String, Object>> getHighThreatWeapons(Integer playerId) {
        List<Map<String, Object>> weapons = new ArrayList<>();
        try {
            Query q = entityManager.createNativeQuery(
                    "SELECT pi.item_id, w.name, w.threat_level, pi.quantity FROM player_items pi " +
                            "JOIN weapon w ON pi.item_id = w.id WHERE pi.player_id = ?1 AND pi.item_type = 'WEAPON'");
            q.setParameter(1, playerId);
            for (Object row : q.getResultList()) {
                Object[] r = (Object[]) row;
                int threat = ((Number) r[2]).intValue();
                if (threat >= 4) {
                    Map<String, Object> w = new LinkedHashMap<>();
                    w.put("weaponId", ((Number) r[0]).intValue());
                    w.put("name", r[1].toString());
                    w.put("threatLevel", threat);
                    w.put("quantity", ((Number) r[3]).intValue());
                    weapons.add(w);
                }
            }
        } catch (Exception e) {
            // player_items may use lowercase enum
            try {
                Query q2 = entityManager.createNativeQuery(
                        "SELECT pi.item_id, w.name, w.threat_level, pi.quantity FROM player_items pi " +
                                "JOIN weapon w ON pi.item_id = w.id WHERE pi.player_id = ?1 AND LOWER(pi.item_type) = 'weapon'");
                q2.setParameter(1, playerId);
                for (Object row : q2.getResultList()) {
                    Object[] r = (Object[]) row;
                    int threat = ((Number) r[2]).intValue();
                    if (threat >= 4) {
                        Map<String, Object> w = new LinkedHashMap<>();
                        w.put("weaponId", ((Number) r[0]).intValue());
                        w.put("name", r[1].toString());
                        w.put("threatLevel", threat);
                        w.put("quantity", ((Number) r[3]).intValue());
                        weapons.add(w);
                    }
                }
            } catch (Exception ignored) { }
        }
        return weapons;
    }

    private int getWeaponThreat(int weaponId) {
        try {
            Query q = entityManager.createNativeQuery("SELECT threat_level FROM weapon WHERE id = ?1");
            q.setParameter(1, weaponId);
            List<?> res = q.getResultList();
            if (!res.isEmpty()) return ((Number) res.get(0)).intValue();
        } catch (Exception ignored) { }
        return 0;
    }

    private int getBestWeaponThreat(Integer playerId) {
        if (playerId == null) return 0;
        int best = 0;
        for (Map<String, Object> w : getHighThreatWeapons(playerId)) {
            int t = ((Number) w.get("threatLevel")).intValue();
            if (t > best) best = t;
        }
        try {
            Query q = entityManager.createNativeQuery(
                    "SELECT MAX(w.threat_level) FROM player_items pi JOIN weapon w ON pi.item_id = w.id " +
                            "WHERE pi.player_id = ?1 AND LOWER(pi.item_type) = 'weapon'");
            q.setParameter(1, playerId);
            List<?> res = q.getResultList();
            if (!res.isEmpty() && res.get(0) != null) {
                best = Math.max(best, ((Number) res.get(0)).intValue());
            }
        } catch (Exception ignored) { }
        return best;
    }

    private boolean isMilitiaOrGuard(Integer playerId) {
        Optional<Player> p = playerRepository.findById(playerId);
        if (!p.isPresent() || p.get().getJobId() == null) return false;
        return jobRepository.findById(p.get().getJobId())
                .map(j -> GUARD_JOBS.contains(j.getName())).orElse(false);
    }

    private List<Map<String, Object>> getPlayersByJobs(Set<String> jobNames) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Player p : playerRepository.findAll()) {
            if (p.getJobId() == null) continue;
            jobRepository.findById(p.getJobId()).ifPresent(job -> {
                if (jobNames.contains(job.getName())) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("job", job.getName());
                    out.add(m);
                }
            });
        }
        return out;
    }

    private List<Map<String, Object>> getPlayerSummaries() {
        return playerRepository.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("faction", p.getFaction() != null ? p.getFaction().name() : null);
            m.put("isOverworked", Boolean.TRUE.equals(p.getIsOverworked()));
            m.put("isInjured", Boolean.TRUE.equals(p.getIsInjured()));
            return m;
        }).collect(Collectors.toList());
    }

    private List<Map<String, Object>> getLaborCandidates(int gameDay) {
        List<Integer> assigned = shelterService.getLaborPlayerIdsForDay(gameDay);
        if (!assigned.isEmpty()) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Integer id : assigned) {
                playerRepository.findById(id).ifPresent(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    rows.add(m);
                });
            }
            return rows;
        }
        return playerRepository.findAll().stream()
                .filter(p -> p.getFaction() != Player.Faction.统治者)
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
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

    private String resolveActorName(Integer id, String kind) {
        if (id == null) return "?";
        if ("npc".equals(kind)) {
            return npcRepository.findById(id).map(LocationNpc::getName).orElse("NPC#" + id);
        }
        return resolvePlayerName(id);
    }

    private String resolveFacilityName(Integer id) {
        if (id == null) return "?";
        return facilityRepository.findById(id).map(LocationFacility::getName).orElse("设施#" + id);
    }

    private String resolveWeaponName(Integer id) {
        if (id == null) return "?";
        try {
            Query q = entityManager.createNativeQuery("SELECT name FROM weapon WHERE id = ?1");
            q.setParameter(1, id);
            List<?> res = q.getResultList();
            if (!res.isEmpty()) return res.get(0).toString();
        } catch (Exception ignored) { }
        return "武器#" + id;
    }

    private String labelAssignedAction(String key) {
        if (key == null) return "?";
        switch (key) {
            case "investigate_location": return "调查地点";
            case "investigate_player": return "调查玩家";
            case "produce": return "生产";
            case "go_location": return "前往地点";
            case "guard": return "看守";
            case "trade": return "交易";
            case "other": return "其他自由行动";
            default: return key;
        }
    }

    private String labelNightAssignedAction(String key) {
        if (key == null) return "?";
        switch (key) {
            case "hide": return "隐藏";
            case "conspiracy": return "密谋";
            case "patrol": return "巡逻";
            case "assassinate": return "暗杀";
            case "other": return "其他夜晚行动";
            default: return key;
        }
    }

    private String labelPlayerActionType(String key) {
        if (key == null) return "?";
        switch (key) {
            case "go_location": return "前往地点";
            case "investigate_player": return "调查玩家";
            case "produce": return "生产";
            case "use_trait": return "使用特性";
            case "use_skill": return "使用职业技能";
            case "hide": return "隐藏";
            default: return key;
        }
    }

    private String formatExtraActionTarget(String actionType, Integer targetId, Integer selfPlayerId) {
        if (targetId == null) return "未指定";
        if ("go_location".equals(actionType)) return resolveLocationName(targetId);
        if (selfPlayerId != null && targetId.equals(selfPlayerId)) return "自己";
        return resolvePlayerName(targetId);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAssignedActions(Map<String, Object> payload) {
        if (payload == null) return Collections.emptyList();
        Object raw = payload.get("assignedActions");
        if (raw instanceof List && !((List<?>) raw).isEmpty()) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object item : (List<?>) raw) {
                if (item instanceof Map) {
                    out.add((Map<String, Object>) item);
                }
            }
            return out;
        }
        String single = str(payload.get("assignedAction"));
        if (single == null) return Collections.emptyList();
        Map<String, Object> one = new LinkedHashMap<>();
        one.put("action", single);
        one.put("targetLocationId", payload.get("targetLocationId"));
        return Collections.singletonList(one);
    }

    private List<String> namesFromIds(List<?> ids) {
        List<String> names = new ArrayList<>();
        for (Object o : ids) {
            Integer id = toInt(o);
            if (id != null) names.add(resolvePlayerName(id));
        }
        return names;
    }

    private Map<String, Object> toMap(FactionAction action) {
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
        map.put("phase", action.getPhase());
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

    public String getActionTypeLabel(String type) {
        switch (type) {
            case "assign_personnel": return "安排人员";
            case "assign_guard": return "安排看守";
            case "extra_labor": return "额外劳动";
            case "secret_contact": return "暗中联络";
            case "sabotage": return "破坏";
            case "extra_investigate": return "额外调查";
            case "guard_ark": return "看守方舟";
            case "ark_construction": return "方舟建设";
            case "curse": return "诅咒";
            default: return type;
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
