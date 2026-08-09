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

    /** 武器图鉴（含威胁值与备注），供 DM 游戏设置页管理 */
    public Map<String, Object> listWeapons(String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以查看武器设置");
            return result;
        }
        @SuppressWarnings("unchecked")
        List<Object[]> raw = entityManager.createNativeQuery(
                "SELECT id, name, unit, threat_level, remark FROM weapon ORDER BY id"
        ).getResultList();
        List<Map<String, Object>> weapons = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> w = new LinkedHashMap<>();
            w.put("id", ((Number) row[0]).intValue());
            w.put("name", row[1]);
            w.put("unit", row[2]);
            w.put("threatLevel", row[3] != null ? ((Number) row[3]).intValue() : 0);
            w.put("remark", row[4]);
            weapons.add(w);
        }
        result.put("success", true);
        result.put("weapons", weapons);
        return result;
    }

    /** 更新武器威胁值/备注（威胁值为战斗结算的真相来源） */
    @Transactional
    public Map<String, Object> updateWeapon(Integer weaponId, Integer threatLevel, String remark, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改武器设置");
            return result;
        }
        if (weaponId == null || weaponId <= 0) {
            result.put("success", false);
            result.put("message", "无效的武器ID");
            return result;
        }
        if (threatLevel == null && remark == null) {
            result.put("success", false);
            result.put("message", "没有需要更新的字段");
            return result;
        }
        if (threatLevel != null && (threatLevel < 0 || threatLevel > 99)) {
            result.put("success", false);
            result.put("message", "威胁值需在 0-99 之间");
            return result;
        }
        Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM weapon WHERE id = ?1"
        ).setParameter(1, weaponId).getSingleResult();
        if (exists == null || exists.intValue() == 0) {
            result.put("success", false);
            result.put("message", "武器不存在");
            return result;
        }
        if (threatLevel != null) {
            entityManager.createNativeQuery(
                    "UPDATE weapon SET threat_level = ?1 WHERE id = ?2"
            ).setParameter(1, threatLevel).setParameter(2, weaponId).executeUpdate();
        }
        if (remark != null) {
            entityManager.createNativeQuery(
                    "UPDATE weapon SET remark = ?1 WHERE id = ?2"
            ).setParameter(1, remark.trim()).setParameter(2, weaponId).executeUpdate();
        }
        result.put("success", true);
        result.put("message", "已保存");
        result.put("weaponId", weaponId);
        if (threatLevel != null) {
            result.put("threatLevel", threatLevel);
        }
        return result;
    }

    /** 更新图鉴条目的描述（item/weapon/ammo/material 通用；名称为唯一键且被逻辑引用，不允许在此修改） */
    @Transactional
    public Map<String, Object> updateCatalogRemark(String itemType, Integer itemId, String remark, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改图鉴描述");
            return result;
        }
        String type = itemType == null ? "" : itemType.trim().toLowerCase();
        if (!VALID_TYPES.contains(type)) {
            result.put("success", false);
            result.put("message", "无效的物品类型");
            return result;
        }
        if (itemId == null || itemId <= 0) {
            result.put("success", false);
            result.put("message", "无效的物品ID");
            return result;
        }
        if (remark == null) {
            result.put("success", false);
            result.put("message", "缺少描述内容");
            return result;
        }
        // type 已通过白名单校验，可安全拼接为表名
        Number exists = (Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM " + type + " WHERE id = ?1"
        ).setParameter(1, itemId).getSingleResult();
        if (exists == null || exists.intValue() == 0) {
            result.put("success", false);
            result.put("message", "条目不存在");
            return result;
        }
        entityManager.createNativeQuery(
                "UPDATE " + type + " SET remark = ?1 WHERE id = ?2"
        ).setParameter(1, remark.trim()).setParameter(2, itemId).executeUpdate();
        result.put("success", true);
        result.put("message", "已保存");
        result.put("itemType", type);
        result.put("itemId", itemId);
        result.put("remark", remark.trim());
        return result;
    }

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
                "SELECT 'item' as itemType, id, name, unit, NULL as threatLevel, remark FROM item " +
                "UNION ALL SELECT 'weapon', id, name, unit, threat_level, remark FROM weapon " +
                "UNION ALL SELECT 'ammo', id, name, unit, NULL, remark FROM ammo " +
                "UNION ALL SELECT 'material', id, name, unit, NULL, remark FROM material " +
                "ORDER BY itemType, id"
        ).getResultList();

        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", row[0]);
            item.put("itemId", ((Number) row[1]).intValue());
            item.put("name", row[2]);
            item.put("unit", row[3]);
            if (row[4] != null) {
                item.put("threatLevel", ((Number) row[4]).intValue());
            }
            item.put("remark", row[5] != null ? String.valueOf(row[5]) : "");
            items.add(item);
        }
        return items;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }
}
