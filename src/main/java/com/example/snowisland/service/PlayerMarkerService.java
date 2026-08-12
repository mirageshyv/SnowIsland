package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerMarker;
import com.example.snowisland.repository.PlayerMarkerRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlayerMarkerService {

    @Autowired
    private PlayerMarkerRepository playerMarkerRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public Map<String, Object> listAll(String userRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "只有DM可以查看标记");
            return out;
        }
        List<Map<String, Object>> markers = new ArrayList<>();
        for (PlayerMarker marker : playerMarkerRepository.findAll()) {
            markers.add(toEntry(marker));
        }
        markers.sort(Comparator.comparing(m -> (Integer) m.get("id")));
        out.put("success", true);
        out.put("markers", markers);
        return out;
    }

    public List<Map<String, Object>> listForPlayer(Integer playerId) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (playerId == null) {
            return out;
        }
        for (PlayerMarker marker : playerMarkerRepository.findByPlayerIdAndVisibleToPlayerTrueOrderByIdAsc(playerId)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("name", marker.getName());
            row.put("note", marker.getNote());
            out.add(row);
        }
        return out;
    }

    @Transactional
    public Map<String, Object> add(Integer playerId, String name, Boolean visibleToPlayer, String note, String userRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "只有DM可以添加标记");
            return out;
        }
        if (playerId == null || !playerRepository.findById(playerId).isPresent()) {
            out.put("success", false);
            out.put("message", "玩家不存在");
            return out;
        }
        String trimmedName = name != null ? name.trim() : "";
        if (trimmedName.isEmpty()) {
            out.put("success", false);
            out.put("message", "标记名不能为空");
            return out;
        }
        if (trimmedName.length() > 50) {
            out.put("success", false);
            out.put("message", "标记名过长");
            return out;
        }
        String trimmedNote = note != null ? note.trim() : null;
        if (trimmedNote != null && trimmedNote.isEmpty()) {
            trimmedNote = null;
        }
        if (trimmedNote != null && trimmedNote.length() > 255) {
            out.put("success", false);
            out.put("message", "备注过长");
            return out;
        }
        PlayerMarker marker = new PlayerMarker();
        marker.setPlayerId(playerId);
        marker.setName(trimmedName);
        marker.setVisibleToPlayer(Boolean.TRUE.equals(visibleToPlayer));
        marker.setNote(trimmedNote);
        playerMarkerRepository.save(marker);
        Player player = playerRepository.findById(playerId).orElse(null);
        String playerName = player != null ? player.getName() : String.valueOf(playerId);
        out.put("success", true);
        out.put("message", "已为「" + playerName + "」添加标记「" + trimmedName + "」");
        out.putAll(listAll(userRole));
        return out;
    }

    @Transactional
    public Map<String, Object> remove(Integer markerId, String userRole) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            out.put("success", false);
            out.put("message", "只有DM可以移除标记");
            return out;
        }
        if (markerId == null || !playerMarkerRepository.findById(markerId).isPresent()) {
            out.put("success", false);
            out.put("message", "标记不存在");
            return out;
        }
        playerMarkerRepository.deleteById(markerId);
        out.put("success", true);
        out.put("message", "已移除标记");
        out.putAll(listAll(userRole));
        return out;
    }

    private Map<String, Object> toEntry(PlayerMarker marker) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", marker.getId());
        row.put("playerId", marker.getPlayerId());
        row.put("name", marker.getName());
        row.put("visibleToPlayer", Boolean.TRUE.equals(marker.getVisibleToPlayer()));
        row.put("note", marker.getNote());
        row.put("createdAt", marker.getCreatedAt());
        return row;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
