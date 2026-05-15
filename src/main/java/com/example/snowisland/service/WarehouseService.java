package com.example.snowisland.service;

import com.example.snowisland.entity.WarehouseConfig;
import com.example.snowisland.repository.WarehouseConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

@Service
public class WarehouseService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseService.class);

    private static final Set<String> VALID_TABLES = new HashSet<>(Arrays.asList(
            "warehouse_general", "warehouse_fuel", "warehouse_armory",
            "warehouse_dock", "warehouse_rebel", "warehouse_ark"
    ));

    private static final Set<String> VALID_ITEM_TYPES = new HashSet<>(Arrays.asList(
            "item", "weapon", "ammo", "material"
    ));

    @Autowired
    private WarehouseConfigRepository configRepository;

    @Autowired
    private EntityManager entityManager;

    public List<Map<String, Object>> getAllWarehouses() {
        return toMapList(configRepository.findAllByOrderBySortOrderAsc());
    }

    public List<Map<String, Object>> getAccessibleWarehouses(Integer playerId) {
        List<WarehouseConfig> allConfigs = configRepository.findAllByOrderBySortOrderAsc();
        List<Map<String, Object>> result = new ArrayList<>();
        for (WarehouseConfig config : allConfigs) {
            boolean hasKey = checkPlayerHasKey(playerId, config.getKeyItemId());
            Map<String, Object> map = toMap(config);
            map.put("accessible", hasKey);
            result.add(map);
        }
        return result;
    }

    public boolean checkPlayerHasKey(Integer playerId, Integer keyItemId) {
        if (playerId == null || keyItemId == null) return false;
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT quantity FROM player_items WHERE player_id = ?1 AND item_type = 'item' AND item_id = ?2");
            query.setParameter(1, playerId);
            query.setParameter(2, keyItemId);
            Object result = query.getResultList().stream().findFirst().orElse(null);
            if (result != null) {
                int qty = ((Number) result).intValue();
                return qty > 0;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error checking player key: playerId={}, keyItemId={}", playerId, keyItemId, e);
            return false;
        }
    }

    public Map<String, Object> getWarehouseStock(String warehouseKey, Integer playerId, String userRole) {
        Map<String, Object> result = new HashMap<>();

        Optional<WarehouseConfig> optionalConfig = configRepository.findByWarehouseKey(warehouseKey);
        if (!optionalConfig.isPresent()) {
            result.put("success", false);
            result.put("message", "仓库不存在");
            return result;
        }

        WarehouseConfig config = optionalConfig.get();

        if (!"dm".equalsIgnoreCase(userRole)) {
            boolean hasKey = checkPlayerHasKey(playerId, config.getKeyItemId());
            if (!hasKey) {
                result.put("success", false);
                result.put("message", "您没有该仓库的钥匙");
                return result;
            }
        }

        String tableName = config.getTableName();
        if (!VALID_TABLES.contains(tableName)) {
            result.put("success", false);
            result.put("message", "无效的仓库表");
            return result;
        }

        List<Map<String, Object>> stockList = queryStock(tableName);
        for (Map<String, Object> item : stockList) {
            enrichItemInfo(item);
        }

        result.put("success", true);
        result.put("warehouseKey", config.getWarehouseKey());
        result.put("warehouseName", config.getWarehouseName());
        result.put("icon", config.getIcon());
        result.put("items", stockList);
        return result;
    }

    public Map<String, Object> updateWarehouseStock(String warehouseKey, String itemType, Integer itemId, Integer quantity, String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改仓库库存");
            return result;
        }

        if (!VALID_ITEM_TYPES.contains(itemType)) {
            result.put("success", false);
            result.put("message", "无效的物品类型");
            return result;
        }

        Optional<WarehouseConfig> optionalConfig = configRepository.findByWarehouseKey(warehouseKey);
        if (!optionalConfig.isPresent()) {
            result.put("success", false);
            result.put("message", "仓库不存在");
            return result;
        }

        String tableName = optionalConfig.get().getTableName();
        if (!VALID_TABLES.contains(tableName)) {
            result.put("success", false);
            result.put("message", "无效的仓库表");
            return result;
        }

        try {
            Query checkQuery = entityManager.createNativeQuery(
                    "SELECT id FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
            checkQuery.setParameter(1, itemType);
            checkQuery.setParameter(2, itemId);
            List<?> existing = checkQuery.getResultList();

            if (!existing.isEmpty()) {
                if (quantity <= 0) {
                    Query deleteQuery = entityManager.createNativeQuery(
                            "DELETE FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
                    deleteQuery.setParameter(1, itemType);
                    deleteQuery.setParameter(2, itemId);
                    deleteQuery.executeUpdate();
                } else {
                    Query updateQuery = entityManager.createNativeQuery(
                            "UPDATE " + tableName + " SET quantity = ?1 WHERE item_type = ?2 AND item_id = ?3");
                    updateQuery.setParameter(1, quantity);
                    updateQuery.setParameter(2, itemType);
                    updateQuery.setParameter(3, itemId);
                    updateQuery.executeUpdate();
                }
            } else if (quantity > 0) {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO " + tableName + " (item_type, item_id, quantity) VALUES (?1, ?2, ?3)");
                insertQuery.setParameter(1, itemType);
                insertQuery.setParameter(2, itemId);
                insertQuery.setParameter(3, quantity);
                insertQuery.executeUpdate();
            }

            result.put("success", true);
            result.put("message", "库存更新成功");
        } catch (Exception e) {
            logger.error("Error updating warehouse stock", e);
            result.put("success", false);
            result.put("message", "库存更新失败: " + e.getMessage());
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> queryStock(String tableName) {
        Query query = entityManager.createNativeQuery("SELECT id, item_type, item_id, quantity FROM " + tableName + " ORDER BY item_type, item_id");
        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("itemType", row[1]);
            map.put("itemId", row[2]);
            map.put("quantity", ((Number) row[3]).intValue());
            result.add(map);
        }
        return result;
    }

    private void enrichItemInfo(Map<String, Object> item) {
        String itemType = (String) item.get("itemType");
        Integer itemId = ((Number) item.get("itemId")).intValue();
        String tableName;
        switch (itemType) {
            case "weapon": tableName = "weapon"; break;
            case "ammo": tableName = "ammo"; break;
            case "material": tableName = "material"; break;
            default: tableName = "item"; break;
        }
        try {
            Query query = entityManager.createNativeQuery("SELECT name, unit FROM " + tableName + " WHERE id = ?1");
            query.setParameter(1, itemId);
            List<Object[]> results = query.getResultList();
            if (!results.isEmpty()) {
                item.put("name", results.get(0)[0]);
                item.put("unit", results.get(0)[1]);
            } else {
                item.put("name", "未知物品");
                item.put("unit", "");
            }
        } catch (Exception e) {
            item.put("name", "未知物品");
            item.put("unit", "");
        }
    }

    private List<Map<String, Object>> toMapList(List<WarehouseConfig> configs) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (WarehouseConfig c : configs) {
            result.add(toMap(c));
        }
        return result;
    }

    private Map<String, Object> toMap(WarehouseConfig config) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", config.getId());
        map.put("warehouseKey", config.getWarehouseKey());
        map.put("warehouseName", config.getWarehouseName());
        map.put("tableName", config.getTableName());
        map.put("keyItemId", config.getKeyItemId());
        map.put("icon", config.getIcon());
        map.put("sortOrder", config.getSortOrder());
        return map;
    }
}
