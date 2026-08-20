package com.example.snowisland.service;

import com.example.snowisland.entity.PlayerNotebook;
import com.example.snowisland.entity.User;
import com.example.snowisland.repository.PlayerNotebookRepository;
import com.example.snowisland.repository.UserRepository;
import com.example.snowisland.util.SafeText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PlayerNotebookService {

    @Autowired
    private PlayerNotebookRepository notebookRepository;

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> listForUser(Integer userId) {
        Integer playerId = resolvePlayerId(userId);
        if (playerId == null) {
            return fail("请先登录玩家账号");
        }
        return listForPlayer(playerId);
    }

    public Map<String, Object> listForPlayer(Integer playerId) {
        Map<String, Object> result = ok();
        List<Map<String, Object>> pages = notebookRepository.findByPlayerIdOrderBySortOrderAscIdAsc(playerId)
                .stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
        result.put("pages", pages);
        result.put("maxPages", SafeText.NOTE_MAX_PAGES);
        return result;
    }

    public Map<String, Object> getForUser(Integer userId, Integer noteId) {
        Integer playerId = resolvePlayerId(userId);
        if (playerId == null) {
            return fail("请先登录玩家账号");
        }
        return getForPlayer(playerId, noteId);
    }

    public Map<String, Object> getForPlayer(Integer playerId, Integer noteId) {
        if (noteId == null) {
            return fail("记录不存在");
        }
        return notebookRepository.findByIdAndPlayerId(noteId, playerId)
                .map(note -> {
                    Map<String, Object> result = ok();
                    result.put("page", toDetail(note));
                    return result;
                })
                .orElseGet(() -> fail("记录不存在"));
    }

    @Transactional
    public Map<String, Object> createForUser(Integer userId) {
        Integer playerId = resolvePlayerId(userId);
        if (playerId == null) {
            return fail("请先登录玩家账号");
        }
        long count = notebookRepository.countByPlayerId(playerId);
        if (count >= SafeText.NOTE_MAX_PAGES) {
            return fail("最多 " + SafeText.NOTE_MAX_PAGES + " 页");
        }
        int nextOrder = notebookRepository.findTopByPlayerIdOrderBySortOrderDesc(playerId)
                .map(n -> n.getSortOrder() == null ? 0 : n.getSortOrder() + 1)
                .orElse(0);
        PlayerNotebook note = new PlayerNotebook();
        note.setPlayerId(playerId);
        note.setTitle("未命名");
        note.setBody("");
        note.setSortOrder(nextOrder);
        notebookRepository.save(note);
        Map<String, Object> result = ok();
        result.put("page", toDetail(note));
        return result;
    }

    @Transactional
    public Map<String, Object> patchForUser(Integer userId, Integer noteId, Map<String, Object> body) {
        Integer playerId = resolvePlayerId(userId);
        if (playerId == null) {
            return fail("请先登录玩家账号");
        }
        if (noteId == null) {
            return fail("记录不存在");
        }
        PlayerNotebook note = notebookRepository.findByIdAndPlayerId(noteId, playerId).orElse(null);
        if (note == null) {
            return fail("记录不存在");
        }
        boolean dirty = false;
        if (body != null && body.containsKey("title")) {
            String title = SafeText.cleanLimit(String.valueOf(body.get("title")), SafeText.NOTE_TITLE_MAX);
            if (title != null) {
                title = title.trim();
            }
            if (title == null || title.isEmpty()) {
                return fail("标题不能为空");
            }
            if (!Objects.equals(note.getTitle(), title)) {
                note.setTitle(title);
                dirty = true;
            }
        }
        if (body != null && body.containsKey("body")) {
            Object raw = body.get("body");
            String text = SafeText.cleanLimit(raw == null ? "" : String.valueOf(raw), SafeText.NOTE_BODY_MAX);
            if (text == null) {
                text = "";
            }
            if (!Objects.equals(note.getBody() == null ? "" : note.getBody(), text)) {
                note.setBody(text);
                dirty = true;
            }
        }
        if (dirty) {
            notebookRepository.save(note);
        }
        Map<String, Object> result = ok();
        result.put("page", toDetail(note));
        result.put("unchanged", !dirty);
        return result;
    }

    @Transactional
    public Map<String, Object> deleteForUser(Integer userId, Integer noteId) {
        Integer playerId = resolvePlayerId(userId);
        if (playerId == null) {
            return fail("请先登录玩家账号");
        }
        if (noteId == null) {
            return fail("记录不存在");
        }
        PlayerNotebook note = notebookRepository.findByIdAndPlayerId(noteId, playerId).orElse(null);
        if (note == null) {
            return fail("记录不存在");
        }
        notebookRepository.delete(note);
        return ok();
    }

    private Integer resolvePlayerId(Integer userId) {
        if (userId == null) {
            return null;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getPlayerId() == null) {
            return null;
        }
        return user.getPlayerId();
    }

    private Map<String, Object> toSummary(PlayerNotebook note) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", note.getId());
        map.put("title", note.getTitle());
        map.put("sortOrder", note.getSortOrder());
        map.put("updatedAt", note.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toDetail(PlayerNotebook note) {
        Map<String, Object> map = toSummary(note);
        map.put("body", note.getBody() == null ? "" : note.getBody());
        return map;
    }

    private Map<String, Object> ok() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }
}
