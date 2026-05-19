package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service
public class DmPlayerInventoryService {

    private static final Set<String> VALID_TYPES = new HashSet<>(
            Arrays.asList("item", "weapon", "ammo", "material"));

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerSupplyService playerSupplyService;

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Object> getItemCatalog(String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以查看物品目录");
            return result;
        }
        List<Map<String, Object>> items = loadCatalogRows();
        result.put("success", true);
        result.put("items", items);
        return result;
    }

    public Map<String, Object> getPlayerInventory(Integer playerId, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以查看玩家背包");
            return result;
        }
        if (!playerRepository.findById(playerId).isPresent()) {
            result.put("success", false);
            result.put("message", "玩家不存在");
            return result;
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> row : playerService.getPlayerItems(playerId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            String type = String.valueOf(row.get("type"));
            item.put("itemType", type);
            item.put("itemId", row.get("id"));
            item.put("name", row.get("name"));
            item.put("unit", row.get("unit"));
            item.put("quantity", row.get("quantity"));
            item.put("description", row.get("remark"));
            if ("weapon".equals(type) && row.get("threatLevel") != null) {
                item.put("threatLevel", row.get("threatLevel"));
            }
            rows.add(item);
        }

        Player player = playerRepository.findById(playerId).orElse(null);
        result.put("success", true);
        result.put("playerId", playerId);
        result.put("playerName", player != null ? player.getName() : "玩家" + playerId);
        result.put("items", rows);
        Map<String, Object> resources = playerSupplyService.getPersonalResourceTotals(playerId);
        result.put("foodKg", resources.get("foodKg"));
        result.put("fuelKg", resources.get("fuelKg"));
        result.put("woodKg", resources.get("woodKg"));
        result.put("fuelLiters", resources.get("fuelLiters"));
        return result;
    }

    @Transactional
    public Map<String, Object> setPlayerItemQuantity(
            Integer playerId,
            String itemType,
            Integer itemId,
            Integer quantity,
            String userRole
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改玩家背包");
            return result;
        }
        if (!playerRepository.findById(playerId).isPresent()) {
            result.put("success", false);
            result.put("message", "玩家不存在");
            return result;
        }
        if (itemType == null || itemId == null || quantity == null) {
            result.put("success", false);
            result.put("message", "参数不完整");
            return result;
        }
        String type = itemType.toLowerCase(Locale.ROOT);
        if (!VALID_TYPES.contains(type)) {
            result.put("success", false);
            result.put("message", "无效的物品类型");
            return result;
        }
        if (quantity < 0) {
            result.put("success", false);
            result.put("message", "数量不能为负数");
            return result;
        }
        boolean ok = setPlayerItemRow(playerId, ItemType.valueOf(type), itemId, quantity);

        if (!ok) {
            result.put("success", false);
            result.put("message", "更新失败");
            return result;
        }
        result.put("success", true);
        result.put("message", quantity == 0 ? "已移除物品" : "已更新数量");
        return result;
    }

    private boolean setPlayerItemRow(Integer playerId, ItemType itemType, Integer itemId, int quantity) {
        Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(playerId, itemType, itemId);
        if (quantity == 0) {
            opt.ifPresent(playerItemRepository::delete);
            return true;
        }
        PlayerItem row = opt.orElseGet(() -> {
            PlayerItem pi = new PlayerItem();
            pi.setPlayerId(playerId);
            pi.setItemType(itemType);
            pi.setItemId(itemId);
            return pi;
        });
        row.setQuantity(quantity);
        playerItemRepository.save(row);
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadCatalogRows() {
        List<Object[]> raw = entityManager.createNativeQuery(
                "SELECT 'item' as type, id, name, unit FROM item " +
                "UNION ALL SELECT 'weapon', id, name, unit FROM weapon " +
                "UNION ALL SELECT 'ammo', id, name, unit FROM ammo " +
                "UNION ALL SELECT 'material', id, name, unit FROM material " +
                "ORDER BY type, id"
        ).getResultList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", row[0]);
            item.put("itemId", ((Number) row[1]).intValue());
            item.put("name", row[2]);
            item.put("unit", row[3]);
            items.add(item);
        }
        return items;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
