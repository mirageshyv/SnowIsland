package com.example.snowisland.service;

import com.example.snowisland.entity.LorePlayerGrant;
import com.example.snowisland.entity.Player;
import com.example.snowisland.repository.LorePlayerGrantRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LoreService {

    private static final List<Map<String, String>> CATALOG = Arrays.asList(
            entry("dead-handwriting", "某位亡者留下的手书", "某位亡者留下的手书.png"),
            entry("weather-station-notice", "来自气象观测站的紧急通告", "来自气象观测站的紧急通告.png"),
            entry("church-bell-tragedy", "圣铃哀鸣：教堂爆炸惨案纪略", "圣铃哀鸣：教堂爆炸惨案纪略.png"),
            entry("ex-sheriff-journal", "《某前警长日录残页》——经年辗转，原主姓名已佚，唯笔迹犹存",
                    "《某前警长日录残页》——经年辗转，原主姓名已佚，唯笔迹犹存.png"),
            entry("wall-whispers", "《壁语》", " 《壁语》.png"),
            entry("cat-notes", "自称【猫】的残缺笔记", "自称【猫】的残缺笔记.png"),
            entry("brewer-tattered-notes", "酿酒师残破手记", "酿酒师残破手记.png")
    );

    @Autowired
    private LorePlayerGrantRepository lorePlayerGrantRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public Map<String, Object> listForClient(String userRole, Integer playerId) {
        Map<String, Object> out = new LinkedHashMap<>();
        boolean isDm = isDm(userRole);

        Map<String, List<Map<String, Object>>> grantsBySlug = new LinkedHashMap<>();
        for (LorePlayerGrant grant : lorePlayerGrantRepository.findAllByOrderByGrantedAtAsc()) {
            grantsBySlug
                    .computeIfAbsent(grant.getLoreSlug(), k -> new ArrayList<>())
                    .add(playerGrantEntry(grant));
        }

        Map<String, LorePlayerGrant> myGrantsBySlug = new LinkedHashMap<>();
        if (playerId != null) {
            for (LorePlayerGrant g : lorePlayerGrantRepository.findByPlayerIdOrderByGrantedAtAsc(playerId)) {
                myGrantsBySlug.put(g.getLoreSlug(), g);
            }
        }

        int unreadCount = 0;
        List<Map<String, Object>> documents = new ArrayList<>();
        for (Map<String, String> def : CATALOG) {
            String slug = def.get("slug");
            List<Map<String, Object>> grantedPlayers = grantsBySlug.getOrDefault(slug, Collections.emptyList());
            LorePlayerGrant mine = myGrantsBySlug.get(slug);
            boolean visible = isDm || mine != null;
            boolean unread = !isDm && mine != null && mine.getViewedAt() == null;
            if (unread) {
                unreadCount++;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("slug", slug);
            row.put("title", def.get("title"));
            row.put("fileName", def.get("fileName"));
            row.put("path", "/lore/" + slug);
            row.put("grantedPlayers", grantedPlayers);
            row.put("visible", visible);
            row.put("unread", unread);
            documents.add(row);
        }

        out.put("success", true);
        out.put("isDm", isDm);
        out.put("playerId", playerId);
        out.put("grantedSlugs", new ArrayList<>(myGrantsBySlug.keySet()));
        out.put("unreadCount", unreadCount);
        out.put("documents", documents);
        return out;
    }

    @Transactional
    public Map<String, Object> grantToPlayer(String loreSlug, Integer targetPlayerId, String userRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "只有DM可以授予线索权限");
            return out;
        }
        if (!isKnownSlug(loreSlug)) {
            out.put("success", false);
            out.put("message", "未知文献");
            return out;
        }
        if (targetPlayerId == null || !playerRepository.findById(targetPlayerId).isPresent()) {
            out.put("success", false);
            out.put("message", "玩家不存在");
            return out;
        }
        Optional<LorePlayerGrant> existing =
                lorePlayerGrantRepository.findByPlayerIdAndLoreSlug(targetPlayerId, loreSlug);
        if (!existing.isPresent()) {
            LorePlayerGrant grant = new LorePlayerGrant();
            grant.setPlayerId(targetPlayerId);
            grant.setLoreSlug(loreSlug);
            grant.setViewedAt(null);
            lorePlayerGrantRepository.save(grant);
        }
        Player p = playerRepository.findById(targetPlayerId).orElse(null);
        String playerName = p != null ? p.getName() : String.valueOf(targetPlayerId);
        out.put("success", true);
        out.put("message", "已授予「" + playerName + "」查阅权限：「" + titleForSlug(loreSlug) + "」");
        out.putAll(listForClient(userRole, null));
        return out;
    }

    @Transactional
    public Map<String, Object> revokeFromPlayer(String loreSlug, Integer targetPlayerId, String userRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "只有DM可以撤销线索权限");
            return out;
        }
        if (!isKnownSlug(loreSlug) || targetPlayerId == null) {
            out.put("success", false);
            out.put("message", "参数无效");
            return out;
        }
        lorePlayerGrantRepository.deleteByPlayerIdAndLoreSlug(targetPlayerId, loreSlug);
        out.put("success", true);
        out.put("message", "已撤销该玩家的查阅权限");
        out.putAll(listForClient(userRole, null));
        return out;
    }

    @Transactional
    public Map<String, Object> acknowledgeView(String loreSlug, Integer playerId) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (playerId == null || !isKnownSlug(loreSlug)) {
            out.put("success", false);
            out.put("message", "参数无效");
            return out;
        }
        Optional<LorePlayerGrant> opt = lorePlayerGrantRepository.findByPlayerIdAndLoreSlug(playerId, loreSlug);
        if (!opt.isPresent()) {
            out.put("success", false);
            out.put("message", "无查阅权限");
            return out;
        }
        LorePlayerGrant grant = opt.get();
        if (grant.getViewedAt() == null) {
            grant.setViewedAt(LocalDateTime.now());
            lorePlayerGrantRepository.save(grant);
        }
        out.put("success", true);
        out.putAll(listForClient(null, playerId));
        return out;
    }

    public boolean canAccess(String loreSlug, String userRole, Integer playerId) {
        if (isDm(userRole)) {
            return isKnownSlug(loreSlug);
        }
        if (playerId == null || !isKnownSlug(loreSlug)) {
            return false;
        }
        return lorePlayerGrantRepository.existsByPlayerIdAndLoreSlug(playerId, loreSlug);
    }

    private Map<String, Object> playerGrantEntry(LorePlayerGrant grant) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("playerId", grant.getPlayerId());
        Player p = playerRepository.findById(grant.getPlayerId()).orElse(null);
        row.put("playerName", p != null ? p.getName() : ("#" + grant.getPlayerId()));
        row.put("viewed", grant.getViewedAt() != null);
        return row;
    }

    private static boolean isKnownSlug(String slug) {
        for (Map<String, String> def : CATALOG) {
            if (def.get("slug").equals(slug)) {
                return true;
            }
        }
        return false;
    }

    private static String titleForSlug(String slug) {
        for (Map<String, String> def : CATALOG) {
            if (def.get("slug").equals(slug)) {
                return def.get("title");
            }
        }
        return slug;
    }

    private static Map<String, String> entry(String slug, String title, String fileName) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("slug", slug);
        m.put("title", title);
        m.put("fileName", fileName);
        return m;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
