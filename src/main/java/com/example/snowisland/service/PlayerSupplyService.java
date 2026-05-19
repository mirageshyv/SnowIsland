package com.example.snowisland.service;

import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.util.ItemCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** Food (material #5), wood (#2), and fuel (#8) in {@code player_items}. */
@Service
public class PlayerSupplyService {

    @Autowired
    private PlayerItemRepository playerItemRepository;

    public Map<String, Object> getPersonalResourceTotals(int playerId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("foodKg", getMaterialQuantity(playerId, ItemCatalog.FOOD_MATERIAL_ID));
        out.put("fuelKg", getMaterialQuantity(playerId, ItemCatalog.FUEL_MATERIAL_ID));
        out.put("woodKg", getMaterialQuantity(playerId, ItemCatalog.WOOD_MATERIAL_ID));
        out.put("fuelLiters", 0);
        return out;
    }

    public Map<String, Object> buildPlayerFoodSupply(int playerId) {
        int q = getMaterialQuantity(playerId, ItemCatalog.FOOD_MATERIAL_ID);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKg", q);
        block.put("items", Collections.singletonList(row(ItemCatalog.FOOD_MATERIAL_ID, ItemCatalog.FOOD_NAME, ItemCatalog.FOOD_UNIT, q)));
        return block;
    }

    public Map<String, Object> buildPlayerEnergyReserve(int playerId) {
        int fuelQ = getMaterialQuantity(playerId, ItemCatalog.FUEL_MATERIAL_ID);
        int woodQ = getMaterialQuantity(playerId, ItemCatalog.WOOD_MATERIAL_ID);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("woodKg", woodQ);
        block.put("items", List.of(
                row(ItemCatalog.FUEL_MATERIAL_ID, ItemCatalog.FUEL_NAME, ItemCatalog.FUEL_UNIT, fuelQ),
                row(ItemCatalog.WOOD_MATERIAL_ID, ItemCatalog.WOOD_NAME, ItemCatalog.WOOD_UNIT, woodQ)));
        return block;
    }

    public int getMaterialQuantity(int playerId, int materialId) {
        return playerItemRepository
                .findByPlayerIdAndItemTypeAndItemId(playerId, ItemType.material, materialId)
                .map(PlayerItem::getQuantity)
                .map(q -> q != null && q > 0 ? q : 0)
                .orElse(0);
    }

    @Transactional
    public boolean setMaterialQuantity(int playerId, int materialId, int quantity) {
        if (quantity < 0) {
            return false;
        }
        Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(
                playerId, ItemType.material, materialId);
        if (quantity == 0) {
            opt.ifPresent(playerItemRepository::delete);
            return true;
        }
        PlayerItem row = opt.orElseGet(() -> newMaterialRow(playerId, materialId));
        row.setQuantity(quantity);
        playerItemRepository.save(row);
        return true;
    }

    @Transactional
    public boolean adjustMaterialQuantity(int playerId, int materialId, int delta) {
        return setMaterialQuantity(playerId, materialId, getMaterialQuantity(playerId, materialId) + delta);
    }

    private static PlayerItem newMaterialRow(int playerId, int materialId) {
        PlayerItem pi = new PlayerItem();
        pi.setPlayerId(playerId);
        pi.setItemType(ItemType.material);
        pi.setItemId(materialId);
        return pi;
    }

    private static Map<String, Object> row(int id, String name, String unit, int quantity) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("unit", unit);
        m.put("quantity", quantity);
        return m;
    }
}
