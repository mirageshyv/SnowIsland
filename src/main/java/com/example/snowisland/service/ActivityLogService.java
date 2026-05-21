package com.example.snowisland.service;

import com.example.snowisland.entity.GameActivityLog;
import com.example.snowisland.entity.Player;
import com.example.snowisland.repository.GameActivityLogRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityLogService {

    public static final String CAT_ACTION = "action";
    public static final String CAT_FACTION = "faction";
    public static final String CAT_NIGHT = "night";
    public static final String CAT_CONSUME = "consume";
    public static final String CAT_TRADE = "trade";
    public static final String CAT_QUICK = "quick";

    @Autowired
    private GameActivityLogRepository gameActivityLogRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public void log(Integer gameDay, Integer playerId, String playerName, String playerFaction,
                    String category, String summary, String detail) {
        try {
            if ((playerFaction == null || playerFaction.isEmpty()) && playerId != null) {
                playerFaction = playerRepository.findById(playerId)
                        .map(p -> p.getFaction() != null ? p.getFaction().name() : null)
                        .orElse(null);
            }
            GameActivityLog row = new GameActivityLog();
            row.setGameDay(gameDay != null && gameDay >= 1 ? gameDay : 1);
            row.setPlayerId(playerId);
            row.setPlayerName(trim(playerName, 50));
            row.setPlayerFaction(trim(playerFaction, 20));
            row.setCategory(category != null ? category : "other");
            row.setSummary(trim(summary, 400));
            row.setDetail(detail);
            gameActivityLogRepository.save(row);
        } catch (Exception ignored) {
            // logging must not break player submissions
        }
    }

    public Map<String, Object> listForDm(Integer gameDay, Integer limit, String userRole,
                                         Integer filterPlayerId, String filterFaction) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "仅DM可查看系统日志");
            return out;
        }
        int cap = limit != null && limit > 0 ? Math.min(limit, 1000) : 500;
        List<GameActivityLog> rows;
        if (gameDay != null && gameDay >= 1) {
            rows = gameActivityLogRepository.findTop500ByGameDayOrderByCreatedAtDesc(gameDay);
        } else {
            rows = gameActivityLogRepository.findTop500ByOrderByCreatedAtDesc();
        }
        String factionFilter = filterFaction != null ? filterFaction.trim() : null;
        if (factionFilter != null && factionFilter.isEmpty()) {
            factionFilter = null;
        }

        List<Map<String, Object>> entries = new ArrayList<>();
        for (GameActivityLog row : rows) {
            if (filterPlayerId != null && !filterPlayerId.equals(row.getPlayerId())) {
                continue;
            }
            if (factionFilter != null) {
                String rowFaction = row.getPlayerFaction();
                if (rowFaction == null || !factionFilter.equals(rowFaction)) {
                    continue;
                }
            }
            if (entries.size() >= cap) {
                break;
            }
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", row.getId());
            e.put("createdAt", row.getCreatedAt());
            e.put("gameDay", row.getGameDay());
            e.put("playerId", row.getPlayerId());
            e.put("playerName", row.getPlayerName());
            e.put("playerFaction", row.getPlayerFaction());
            e.put("category", row.getCategory());
            e.put("summary", row.getSummary());
            e.put("detail", row.getDetail());
            entries.add(e);
        }
        out.put("success", true);
        out.put("entries", entries);
        return out;
    }

    public static String factionOf(Player player) {
        if (player == null || player.getFaction() == null) {
            return null;
        }
        return player.getFaction().name();
    }

    public static String truncate(String s, int max) {
        if (s == null) return null;
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, max - 1) + "…";
    }

    private static String trim(String s, int max) {
        if (s == null) return null;
        return truncate(s, max);
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
