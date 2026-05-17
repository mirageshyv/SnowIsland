package com.example.snowisland.service;

import com.example.snowisland.entity.ShelterProgress;
import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.repository.ShelterProgressRepository;
import com.example.snowisland.repository.ShelterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

@Service
public class ShelterService {

    private static final int DEFAULT_BUILD_VALUE = 76;

    private static final Object[][] DEFAULT_STOCK = {
            {ShelterStock.ItemType.material, 2, 45},
            {ShelterStock.ItemType.material, 7, 32},
            {ShelterStock.ItemType.item, 1, 8},
            {ShelterStock.ItemType.item, 2, 4},
            {ShelterStock.ItemType.item, 3, 2},
            {ShelterStock.ItemType.item, 4, 3},
            {ShelterStock.ItemType.item, 5, 1},
            {ShelterStock.ItemType.item, 6, 1},
            {ShelterStock.ItemType.item, 7, 1},
            {ShelterStock.ItemType.item, 8, 5},
            {ShelterStock.ItemType.item, 9, 2},
            {ShelterStock.ItemType.item, 10, 10},
            {ShelterStock.ItemType.item, 11, 12},
            {ShelterStock.ItemType.item, 12, 2},
            {ShelterStock.ItemType.item, 13, 18},
            {ShelterStock.ItemType.item, 14, 3},
            {ShelterStock.ItemType.item, 15, 6},
            {ShelterStock.ItemType.item, 16, 4},
            {ShelterStock.ItemType.item, 17, 1},
            {ShelterStock.ItemType.weapon, 1, 1},
            {ShelterStock.ItemType.weapon, 2, 1},
            {ShelterStock.ItemType.weapon, 3, 2},
            {ShelterStock.ItemType.weapon, 4, 1},
            {ShelterStock.ItemType.weapon, 6, 1},
            {ShelterStock.ItemType.weapon, 7, 1},
            {ShelterStock.ItemType.weapon, 8, 2},
            {ShelterStock.ItemType.weapon, 9, 1},
            {ShelterStock.ItemType.material, 4, 24},
            {ShelterStock.ItemType.material, 3, 35},
    };

    @Autowired
    private ShelterProgressRepository shelterProgressRepository;

    @Autowired
    private ShelterStockRepository shelterStockRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShelterSupplyService shelterSupplyService;

    @Transactional
    public Map<String, Object> getSummary() {
        Map<String, Object> out = new LinkedHashMap<>();
        ShelterProgress progress = shelterProgressRepository.findById(ShelterProgress.SINGLETON_ID)
                .orElseGet(() -> {
                    ShelterProgress p = new ShelterProgress();
                    p.setId(ShelterProgress.SINGLETON_ID);
                    p.setCurrentBuildValue(DEFAULT_BUILD_VALUE);
                    return shelterProgressRepository.save(p);
                });
        List<ShelterStock> rows = shelterStockRepository.findAllByOrderByItemTypeAscItemIdAsc();
        if (rows.isEmpty()) {
            seedDefaultStock();
            rows = shelterStockRepository.findAllByOrderByItemTypeAscItemIdAsc();
        }

        List<Map<String, Object>> inventory = new ArrayList<>();
        for (ShelterStock row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", row.getItemType().name());
            item.put("itemId", row.getItemId());
            item.put("quantity", row.getQuantity());
            enrichItemInfo(item);
            inventory.add(item);
        }

        shelterSupplyService.ensureReady();

        out.put("success", true);
        out.put("currentBuildValue", progress.getCurrentBuildValue());
        out.put("inventory", inventory);
        out.put("foodSupply", shelterSupplyService.buildShelterFoodSupply());
        out.put("energyReserve", shelterSupplyService.buildShelterEnergyReserve());
        return out;
    }

    private void enrichItemInfo(Map<String, Object> item) {
        String itemType = (String) item.get("itemType");
        Integer itemId = (Integer) item.get("itemId");
        String tableName;
        switch (itemType) {
            case "weapon": tableName = "weapon"; break;
            case "ammo": tableName = "ammo"; break;
            case "material": tableName = "material"; break;
            default: tableName = "item"; break;
        }
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT name, unit, remark FROM " + tableName + " WHERE id = ?1");
            query.setParameter(1, itemId);
            List<Object[]> results = query.getResultList();
            if (!results.isEmpty()) {
                item.put("name", results.get(0)[0]);
                item.put("unit", results.get(0)[1]);
                item.put("description", results.get(0)[2]);
            } else {
                item.put("name", "未知物品");
                item.put("unit", "");
                item.put("description", "");
            }
        } catch (Exception e) {
            item.put("name", "未知物品");
            item.put("unit", "");
            item.put("description", "");
        }
    }

    protected void seedDefaultStock() {
        for (Object[] row : DEFAULT_STOCK) {
            ShelterStock s = new ShelterStock();
            s.setItemType((ShelterStock.ItemType) row[0]);
            s.setItemId((Integer) row[1]);
            s.setQuantity((Integer) row[2]);
            shelterStockRepository.save(s);
        }
    }
}
