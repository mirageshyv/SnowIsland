package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.entity.Player.Faction;
import com.example.snowisland.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DmPlayerManagementService {

    private static final String DEFAULT_PASSWORD = "test123";

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private JobInitialItemsRepository jobInitialItemsRepository;

    @Autowired
    private DmPlayerInventoryService dmPlayerInventoryService;

    @Autowired
    private PlayerSupplyService playerSupplyService;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    public Map<String, Object> listPlayersForDm(String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以查看玩家列表");
        }

        Map<Integer, String> jobNames = new HashMap<>();
        for (Job job : jobRepository.findAll()) {
            jobNames.put(job.getId(), job.getName());
        }
        Map<Integer, String> skillNames = new HashMap<>();
        for (Skill skill : skillRepository.findAll()) {
            skillNames.put(skill.getId(), skill.getName());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            rows.add(toDmPlayerRow(player, jobNames, skillNames));
        }
        result.put("success", true);
        result.put("players", rows);
        return result;
    }

    public Map<String, Object> previewJobStartingInventory(Integer jobId, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以预览初始背包");
        }
        if (jobId == null || !jobRepository.findById(jobId).isPresent()) {
            return deny(result, "职业不存在");
        }
        result.put("success", true);
        result.put("items", buildStartingItemRows(jobId));
        return result;
    }

    @Transactional
    public Map<String, Object> createPlayerForDm(Map<String, Object> body, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以创建玩家");
        }

        String name = stringVal(body.get("name"));
        if (name == null || name.trim().isEmpty()) {
            return deny(result, "请输入玩家姓名");
        }

        try {
            Player player = new Player();
            player.setName(name.trim());
            applyPlayerFieldsFromBody(player, body, true);

            Player saved = playerRepository.save(player);

            String loginUsername = stringVal(body.get("loginUsername"));
            if (loginUsername == null || loginUsername.trim().isEmpty()) {
                loginUsername = "player" + saved.getId();
            } else {
                loginUsername = loginUsername.trim();
            }
            if (userRepository.existsByUsername(loginUsername)) {
                throw new IllegalArgumentException("登录账号已存在: " + loginUsername);
            }

            String loginPassword = stringVal(body.get("loginPassword"));
            if (loginPassword == null || loginPassword.trim().isEmpty()) {
                loginPassword = DEFAULT_PASSWORD;
            }

            User user = new User();
            user.setUsername(loginUsername);
            user.setPassword(loginPassword);
            user.setRole(User.Role.PLAYER);
            user.setPlayerId(saved.getId());
            userRepository.save(user);

            boolean grant = body.get("grantStartingInventory") == null
                    || Boolean.TRUE.equals(body.get("grantStartingInventory"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> customItems = (List<Map<String, Object>>) body.get("startingItems");
            if (customItems != null && !customItems.isEmpty()) {
                applyInventoryItems(saved.getId(), customItems, "set", userRole);
            } else if (grant && saved.getJobId() != null) {
                applyInventoryItems(saved.getId(), buildStartingItemRows(saved.getJobId()), "set", userRole);
            }

            result.put("success", true);
            result.put("player", saved);
            result.put("username", loginUsername);
            result.put("password", loginPassword);
            result.put("playerId", saved.getId());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建玩家失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> updatePlayerForDm(Integer playerId, Map<String, Object> body, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以更新玩家");
        }

        Player existing = playerRepository.findById(playerId).orElse(null);
        if (existing == null) {
            return deny(result, "玩家不存在");
        }

        try {
            if (body.containsKey("name")) {
                String name = stringVal(body.get("name"));
                if (name != null && !name.trim().isEmpty()) {
                    existing.setName(name.trim());
                }
            }
            applyPlayerFieldsFromBody(existing, body, false);

            playerRepository.save(existing);

            if (body.containsKey("loginUsername") || body.containsKey("loginPassword")) {
                updateCredentials(playerId, body, result);
                if (Boolean.FALSE.equals(result.get("success"))) {
                    return result;
                }
            }

            result.put("success", true);
            result.put("message", "已保存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> grantJobStartingInventory(Integer playerId, String mode, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以发放初始背包");
        }
        Player player = playerRepository.findById(playerId).orElse(null);
        if (player == null) {
            return deny(result, "玩家不存在");
        }
        if (player.getJobId() == null) {
            return deny(result, "玩家未分配职业");
        }
        String applyMode = "replace".equalsIgnoreCase(mode) ? "set" : "add";
        return applyInventoryItems(playerId, buildStartingItemRows(player.getJobId()), applyMode, userRole);
    }

    @Transactional
    public Map<String, Object> applyInventoryItems(
            Integer playerId,
            List<Map<String, Object>> items,
            String mode,
            String userRole
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以修改背包");
        }
        if (!playerRepository.findById(playerId).isPresent()) {
            return deny(result, "玩家不存在");
        }
        if (items == null) {
            items = Collections.emptyList();
        }

        boolean replace = "set".equalsIgnoreCase(mode) || "replace".equalsIgnoreCase(mode);

        try {
            for (Map<String, Object> row : items) {
                String itemType = stringVal(row.get("itemType"));
                Integer itemId = intOrNull(row.get("itemId"));
                Integer quantity = intOrNull(row.get("quantity"));
                if (itemType == null || itemId == null || quantity == null) {
                    continue;
                }
                if (quantity < 0) {
                    return deny(result, "数量不能为负数");
                }
                NormalizedItem normalized = normalizeItemType(itemType, itemId);
                applyOneItem(playerId, normalized.type, normalized.itemId, quantity, replace);
            }
            result.put("success", true);
            result.put("message", replace ? "已设置背包" : "已添加物品");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> deletePlayerForDm(Integer playerId, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            return deny(result, "只有DM可以删除玩家");
        }
        if (!playerRepository.findById(playerId).isPresent()) {
            return deny(result, "玩家不存在");
        }
        try {
            userRepository.findByPlayerId(playerId).ifPresent(userRepository::delete);
            playerItemRepository.deleteByPlayerId(playerId);
            playerRepository.deleteById(playerId);
            result.put("success", true);
            result.put("message", "删除成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }

    private List<Map<String, Object>> buildStartingItemRows(Integer jobId) {
        List<JobInitialItems> initialItems = jobInitialItemsRepository.findByJobIdOrderByItemType(jobId);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JobInitialItems item : initialItems) {
            Map<String, Object> row = new LinkedHashMap<>();
            String type = item.getItemType().name().toLowerCase(Locale.ROOT);
            int id = item.getItemId();
            NormalizedItem normalized = normalizeItemType(type, id);
            row.put("itemType", normalized.type);
            row.put("itemId", normalized.itemId);
            row.put("quantity", item.getQuantity());
            row.put("unit", item.getUnit());
            rows.add(row);
        }
        return rows;
    }

    private void applyOneItem(Integer playerId, String itemType, Integer itemId, int quantity, boolean replace) {
        ItemType type = ItemType.valueOf(itemType.toLowerCase(Locale.ROOT));
        Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(playerId, type, itemId);
        int target = quantity;
        if (!replace && opt.isPresent()) {
            target = opt.get().getQuantity() + quantity;
        }
        dmPlayerInventoryService.setPlayerItemQuantity(playerId, type.name(), itemId, target, "dm");
    }

    private NormalizedItem normalizeItemType(String itemType, int itemId) {
        return new NormalizedItem(itemType.toLowerCase(Locale.ROOT), itemId);
    }

    private void applyPlayerFieldsFromBody(Player player, Map<String, Object> body, boolean isCreate) {
        if (body.containsKey("faction") || isCreate) {
            String factionStr = stringVal(body.get("faction"));
            if (factionStr != null && !factionStr.trim().isEmpty()) {
                player.setFaction(Faction.valueOf(factionStr.trim()));
            } else if (isCreate && player.getFaction() == null) {
                player.setFaction(Faction.平民);
            }
        }
        if (body.containsKey("jobId")) {
            player.setJobId(intOrNull(body.get("jobId")));
        }
        if (body.containsKey("skillId")) {
            player.setSkillId(intOrNull(body.get("skillId")));
        }
        if (body.containsKey("isWeak")) {
            player.setIsWeak(boolVal(body.get("isWeak")));
        }
        if (body.containsKey("isOverworked")) {
            player.setIsOverworked(boolVal(body.get("isOverworked")));
        }
        if (body.containsKey("isInjured")) {
            Integer val = intOrNull(body.get("isInjured"));
            player.setIsInjured(val != null ? val : 0);
        }
    }

    private void updateCredentials(Integer playerId, Map<String, Object> body, Map<String, Object> result) {
        User user = userRepository.findByPlayerId(playerId).orElse(null);
        if (user == null) {
            user = new User();
            user.setRole(User.Role.PLAYER);
            user.setPlayerId(playerId);
            user.setPassword(DEFAULT_PASSWORD);
            user.setUsername("player" + playerId);
        }

        if (body.containsKey("loginUsername")) {
            String username = stringVal(body.get("loginUsername"));
            if (username != null && !username.trim().isEmpty()) {
                username = username.trim();
                if (!username.equals(user.getUsername()) && userRepository.existsByUsername(username)) {
                    result.put("success", false);
                    result.put("message", "登录账号已存在");
                    return;
                }
                user.setUsername(username);
            }
        }
        if (body.containsKey("loginPassword")) {
            String password = stringVal(body.get("loginPassword"));
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
        }
        userRepository.save(user);
    }

    private Map<String, Object> toDmPlayerRow(Player player, Map<Integer, String> jobNames, Map<Integer, String> skillNames) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", player.getId());
        row.put("name", player.getName());
        row.put("faction", player.getFaction() != null ? player.getFaction().name() : null);
        row.put("jobId", player.getJobId());
        row.put("jobName", player.getJobId() != null ? jobNames.getOrDefault(player.getJobId(), "—") : "—");
        row.put("skillId", player.getSkillId());
        row.put("skillName", player.getSkillId() != null ? skillNames.getOrDefault(player.getSkillId(), "—") : "—");
        row.put("isWeak", Boolean.TRUE.equals(player.getIsWeak()));
        row.put("isOverworked", Boolean.TRUE.equals(player.getIsOverworked()));
        row.put("isInjured", player.getIsInjured() != null ? player.getIsInjured() : 0);

        userRepository.findByPlayerId(player.getId()).ifPresent(user -> {
            row.put("loginUsername", user.getUsername());
            row.put("loginPassword", user.getPassword());
        });
        return row;
    }

    private static Map<String, Object> deny(Map<String, Object> result, String message) {
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }

    private static String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Integer intOrNull(Object o) {
        if (o == null || "".equals(o)) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean boolVal(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return Boolean.parseBoolean(String.valueOf(o));
    }

    private static final class NormalizedItem {
        final String type;
        final int itemId;

        NormalizedItem(String type, int itemId) {
            this.type = type;
            this.itemId = itemId;
        }
    }
}
