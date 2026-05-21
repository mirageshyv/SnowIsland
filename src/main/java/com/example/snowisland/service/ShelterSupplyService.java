package com.example.snowisland.service;

import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.repository.ShelterStockRepository;
import com.example.snowisland.util.ItemCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Shelter public food/fuel stored in {@code shelter_stock} as generic materials 5 and 8.
 */
@Service
public class ShelterSupplyService {

    private static final int DEFAULT_SHELTER_FOOD_KG = 127;
    private static final int DEFAULT_SHELTER_FUEL_KG = 40;

    @Autowired
    private ShelterStockRepository shelterStockRepository;

    @Transactional
    public void ensureReady() {
        ensureDefaultShelterStocks();
    }

    @Transactional
    protected void ensureDefaultShelterStocks() {
        ensureMaterialStock(ItemCatalog.FOOD_MATERIAL_ID, DEFAULT_SHELTER_FOOD_KG);
        ensureMaterialStock(ItemCatalog.FUEL_MATERIAL_ID, DEFAULT_SHELTER_FUEL_KG);
    }

    private void ensureMaterialStock(int materialId, int defaultQty) {
        if (!shelterStockRepository.findByItemTypeAndItemId(ShelterStock.ItemType.material, materialId).isPresent()) {
            ShelterStock row = new ShelterStock();
            row.setItemType(ShelterStock.ItemType.material);
            row.setItemId(materialId);
            row.setQuantity(defaultQty);
            shelterStockRepository.save(row);
        }
    }

    public Map<String, Object> buildShelterFoodSupply() {
        int q = shelterQty(ItemCatalog.FOOD_MATERIAL_ID);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKg", q);
        block.put("items", Collections.singletonList(itemRow(ItemCatalog.FOOD_MATERIAL_ID, ItemCatalog.FOOD_NAME, q)));
        return block;
    }

    public Map<String, Object> buildShelterEnergyReserve() {
        int fuelQ = shelterQty(ItemCatalog.FUEL_MATERIAL_ID);
        int woodQ = shelterQty(ItemCatalog.WOOD_MATERIAL_ID);
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("woodKg", woodQ);
        block.put("items", Arrays.asList(
                fuelRow(ItemCatalog.FUEL_MATERIAL_ID, ItemCatalog.FUEL_NAME, fuelQ),
                materialRow(ItemCatalog.WOOD_MATERIAL_ID, ItemCatalog.WOOD_NAME, ItemCatalog.WOOD_UNIT, woodQ)));
        return block;
    }

    private int shelterQty(int materialId) {
        return shelterStockRepository
                .findByItemTypeAndItemId(ShelterStock.ItemType.material, materialId)
                .map(ShelterStock::getQuantity)
                .orElse(0);
    }

    private static Map<String, Object> itemRow(int id, String name, int quantity) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("quantity", quantity);
        return row;
    }

    private static Map<String, Object> fuelRow(int id, String name, int quantity) {
        return materialRow(id, name, ItemCatalog.FUEL_UNIT, quantity);
    }

    private static Map<String, Object> materialRow(int id, String name, String unit, int quantity) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("unit", unit);
        row.put("quantity", quantity);
        return row;
    }
}
