package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.QuickInteraction;
import com.example.snowisland.entity.QuickInteraction.InteractionStatus;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.repository.QuickInteractionRepository;
import com.example.snowisland.util.PlayerStatusCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuickInteractionService {

    private static final int CONTENT_MAX_LENGTH = 2000;
    private static final int QUICK_ACTION_DAILY_LIMIT = 2;
    private static final String TYPE_QUICK_ACTION = "quick_action";
    private static final String TYPE_FREE_TRANSPORT = "free_transport";
    private static final Set<String> QUOTA_TYPES = new HashSet<>(Arrays.asList(
            TYPE_QUICK_ACTION, TYPE_FREE_TRANSPORT));
    private static final Set<String> VALID_TYPES = new HashSet<>(Arrays.asList(
            TYPE_QUICK_ACTION, TYPE_FREE_TRANSPORT, "supplementary_action", "rule_consult", "ask_dm"));

    private static final Map<String, String> TYPE_LABELS = new LinkedHashMap<>();
    static {
        TYPE_LABELS.put("quick_action", "快速行动");
        TYPE_LABELS.put("free_transport", "免费搬运");
        TYPE_LABELS.put("supplementary_action", "补充行动");
        TYPE_LABELS.put("rule_consult", "规则咨询");
        TYPE_LABELS.put("ask_dm", "询问DM");
    }

    @Autowired
    private QuickInteractionRepository quickInteractionRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private TradeRestrictionService tradeRestrictionService;

    @Autowired
    private ActionService actionService;

    @Autowired
    private TransportSettlementService transportSettlementService;

    public Map<String, Object> getPlayerContext(Integer playerId, Integer gameDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (playerId == null) {
            result.put("success", false);
            result.put("message", "缺少玩家ID");
            return result;
        }
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            result.put("success", false);
            result.put("message", "玩家不存在");
            return result;
        }
        if (gameDay == null) {
            gameDay = gameStateService.getCurrentDay();
        }

        result.put("success", true);
        result.put("playerId", player.getId());
        result.put("playerName", player.getName());
        result.put("faction", player.getFaction() != null ? player.getFaction().name() : "");
        result.put("gameDay", gameDay);
        putQuickActionQuota(result, playerId, gameDay);

        List<QuickInteraction> history = quickInteractionRepository
                .findByPlayerIdOrderByCreatedAtDesc(playerId);
        result.put("history", history.stream()
                .map(this::toMap)
                .collect(Collectors.toList()));

        return result;
    }

    @Transactional
    public Map<String, Object> submitInteraction(Integer playerId, String interactionType,
                                                  String content, Integer gameDay) {
        Map<String, Object> result = new LinkedHashMap<>();

        if (playerId == null) {
            return deny(result, "缺少玩家ID");
        }
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return deny(result, "玩家不存在");
        }
        if (interactionType == null || !VALID_TYPES.contains(interactionType)) {
            return deny(result, "无效的交互类型");
        }
        if (TYPE_FREE_TRANSPORT.equals(interactionType)) {
            return deny(result, "免费搬运请使用专用提交入口");
        }
        if (countsTowardQuickQuota(interactionType) && tradeRestrictionService.isBoundActive(player)) {
            return deny(result, PlayerStatusCatalog.BOUND_DENY_MESSAGE);
        }
        if (content == null || content.trim().isEmpty()) {
            return deny(result, "请填写交互内容");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            return deny(result, "内容不能超过" + CONTENT_MAX_LENGTH + "个字符");
        }
        if (gameDay == null) {
            gameDay = gameStateService.getCurrentDay();
        }
        if (countsTowardQuickQuota(interactionType)) {
            long used = countQuotaUsed(playerId, gameDay);
            if (used >= QUICK_ACTION_DAILY_LIMIT) {
                return deny(result, "快速行动一天最多两条，每条一个动作");
            }
        }

        QuickInteraction qi = new QuickInteraction();
        qi.setPlayerId(playerId);
        qi.setPlayerName(player.getName());
        qi.setFaction(player.getFaction() != null ? player.getFaction().name() : "平民");
        qi.setInteractionType(interactionType);
        qi.setContent(content.trim());
        qi.setGameDay(gameDay);
        qi.setStatus(InteractionStatus.pending);

        qi = quickInteractionRepository.save(qi);

        activityLogService.log(
                gameDay,
                playerId,
                player.getName(),
                ActivityLogService.factionOf(player),
                ActivityLogService.CAT_QUICK,
                TYPE_LABELS.getOrDefault(interactionType, interactionType),
                ActivityLogService.truncate(content.trim(), 300));

        result.put("success", true);
        result.put("message", "提交成功");
        result.put("data", toMap(qi));
        return result;
    }

    @Transactional
    public Map<String, Object> submitFreeTransport(Integer playerId, String notes, Integer gameDay) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (playerId == null) {
            return deny(result, "缺少玩家ID");
        }
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return deny(result, "玩家不存在");
        }
        if (tradeRestrictionService.isBoundActive(player)) {
            return deny(result, PlayerStatusCatalog.BOUND_DENY_MESSAGE);
        }
        if (notes == null || notes.trim().isEmpty()) {
            return deny(result, "请填写搬运内容");
        }
        if (gameDay == null) {
            gameDay = gameStateService.getCurrentDay();
        }
        long used = countQuotaUsed(playerId, gameDay);
        if (used >= QUICK_ACTION_DAILY_LIMIT) {
            return deny(result, "快速行动一天最多两条，每条一个动作");
        }

        Map<String, Object> actionResult = actionService.submitFreeTransportAction(playerId, notes.trim(), gameDay);
        if (!Boolean.TRUE.equals(actionResult.get("success"))) {
            return actionResult;
        }

        String content = transportSettlementService.formatNotesChinese(notes.trim());
        if (content == null || content.isEmpty()) {
            content = notes.trim();
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            content = content.substring(0, CONTENT_MAX_LENGTH);
        }

        QuickInteraction qi = new QuickInteraction();
        qi.setPlayerId(playerId);
        qi.setPlayerName(player.getName());
        qi.setFaction(player.getFaction() != null ? player.getFaction().name() : "平民");
        qi.setInteractionType(TYPE_FREE_TRANSPORT);
        qi.setContent(content);
        qi.setGameDay(gameDay);
        qi.setStatus(InteractionStatus.pending);
        qi = quickInteractionRepository.save(qi);

        activityLogService.log(
                gameDay,
                playerId,
                player.getName(),
                ActivityLogService.factionOf(player),
                ActivityLogService.CAT_QUICK,
                TYPE_LABELS.get(TYPE_FREE_TRANSPORT),
                ActivityLogService.truncate(content, 300));

        result.put("success", true);
        result.put("message", "提交成功");
        result.put("data", toMap(qi));
        result.put("action", actionResult.get("data"));
        return result;
    }

    public List<Map<String, Object>> getAllInteractions(Integer gameDay, String faction,
                                                         String status, String interactionType) {
        List<QuickInteraction> list;
        if (gameDay != null && faction != null && !faction.isEmpty()) {
            list = quickInteractionRepository.findByFactionAndGameDayOrderByCreatedAtAsc(faction, gameDay);
        } else if (gameDay != null) {
            list = quickInteractionRepository.findByGameDayOrderByCreatedAtAsc(gameDay);
        } else {
            list = quickInteractionRepository.findAll();
        }

        if (status != null && !status.isEmpty()) {
            InteractionStatus filterStatus = InteractionStatus.valueOf(status);
            list = list.stream()
                    .filter(qi -> qi.getStatus() == filterStatus)
                    .collect(Collectors.toList());
        }
        if (interactionType != null && !interactionType.isEmpty()) {
            list = list.stream()
                    .filter(qi -> interactionType.equals(qi.getInteractionType()))
                    .collect(Collectors.toList());
        }

        return list.stream().map(this::toMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> replyInteraction(Integer interactionId, String reply, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以回复快速交互");
        }
        if (interactionId == null) {
            return deny(result, "缺少交互记录ID");
        }
        QuickInteraction qi = quickInteractionRepository.findById(interactionId).orElse(null);
        if (qi == null) {
            return deny(result, "交互记录不存在");
        }
        if (reply == null || reply.trim().isEmpty()) {
            return deny(result, "请填写回复内容");
        }

        qi.setDmReply(reply.trim());
        qi.setRepliedAt(LocalDateTime.now());
        qi.setStatus(InteractionStatus.replied);
        qi = quickInteractionRepository.save(qi);

        result.put("success", true);
        result.put("message", "回复成功");
        result.put("data", toMap(qi));
        return result;
    }

    @Transactional
    public Map<String, Object> updateStatus(Integer interactionId, String status, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以更新状态");
        }
        if (interactionId == null) {
            return deny(result, "缺少交互记录ID");
        }
        QuickInteraction qi = quickInteractionRepository.findById(interactionId).orElse(null);
        if (qi == null) {
            return deny(result, "交互记录不存在");
        }
        try {
            InteractionStatus newStatus = InteractionStatus.valueOf(status);
            qi.setStatus(newStatus);
            qi = quickInteractionRepository.save(qi);
            result.put("success", true);
            result.put("message", "状态已更新");
            result.put("data", toMap(qi));
        } catch (IllegalArgumentException e) {
            return deny(result, "无效的状态值");
        }
        return result;
    }

    private void putQuickActionQuota(Map<String, Object> result, Integer playerId, Integer gameDay) {
        long used = countQuotaUsed(playerId, gameDay);
        int remaining = (int) Math.max(0, QUICK_ACTION_DAILY_LIMIT - used);
        result.put("quickActionDailyLimit", QUICK_ACTION_DAILY_LIMIT);
        result.put("quickActionUsed", (int) used);
        result.put("quickActionRemaining", remaining);
    }

    private long countQuotaUsed(Integer playerId, Integer gameDay) {
        long used = 0;
        for (String type : QUOTA_TYPES) {
            used += quickInteractionRepository.countByPlayerIdAndGameDayAndInteractionType(
                    playerId, gameDay, type);
        }
        return used;
    }

    private static boolean countsTowardQuickQuota(String interactionType) {
        return QUOTA_TYPES.contains(interactionType);
    }

    public static String getTypeLabel(String type) {
        return TYPE_LABELS.getOrDefault(type, type);
    }

    private Map<String, Object> toMap(QuickInteraction qi) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", qi.getId());
        m.put("playerId", qi.getPlayerId());
        m.put("playerName", qi.getPlayerName());
        m.put("faction", qi.getFaction());
        m.put("interactionType", qi.getInteractionType());
        m.put("interactionTypeLabel", getTypeLabel(qi.getInteractionType()));
        m.put("content", qi.getContent());
        m.put("gameDay", qi.getGameDay());
        m.put("status", qi.getStatus().name());
        m.put("dmReply", qi.getDmReply());
        m.put("repliedAt", qi.getRepliedAt());
        m.put("createdAt", qi.getCreatedAt());
        m.put("updatedAt", qi.getUpdatedAt());
        return m;
    }

    private static Map<String, Object> deny(Map<String, Object> result, String message) {
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
