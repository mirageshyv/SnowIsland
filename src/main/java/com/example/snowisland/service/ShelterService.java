package com.example.snowisland.service;

import com.example.snowisland.entity.ShelterProgress;
import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.repository.ShelterProgressRepository;
import com.example.snowisland.repository.ShelterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 统治者避难所：建造值、避难所物资库存、避难所公共食物与能量（{@link ShelterSupplyService}）。
 * 玩家个人食物/能量见 {@link PlayerSupplyService}，不通过本接口展示。
 */
@Service
public class ShelterService {

    private static final int DEFAULT_BUILD_VALUE = 76;

    /** 与前端 gameData 默认库存一致，供空库时初始化 */
    private static final String[][] DEFAULT_STOCK = {
            {"wood", "45"}, {"stone", "32"}, {"medical_kit", "8"}, {"flashlight", "4"},
            {"handcuffs", "2"}, {"whistle", "3"}, {"body_armor", "1"}, {"composite_shield", "1"},
            {"flare_gun", "1"}, {"repair_kit", "5"}, {"contract", "2"}, {"rum", "10"},
            {"herbs", "12"}, {"fishing_net", "2"}, {"candle", "18"}, {"rubbing_alcohol", "3"},
            {"matches", "6"}, {"pencil", "4"}, {"tattered_chart", "1"}, {"service_pistol", "1"},
            {"hunting_shotgun", "1"}, {"baton", "2"}, {"bayonet", "1"}, {"harpoon_spear", "1"},
            {"hunting_bow", "1"}, {"pickaxe", "2"}, {"axe", "1"}, {"plank", "24"}, {"rope", "35"},
    };

    @Autowired
    private ShelterProgressRepository shelterProgressRepository;

    @Autowired
    private ShelterStockRepository shelterStockRepository;

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
        List<ShelterStock> rows = shelterStockRepository.findAllByOrderByItemKeyAsc();
        if (rows.isEmpty()) {
            seedDefaultStock();
            rows = shelterStockRepository.findAllByOrderByItemKeyAsc();
        }

        List<Map<String, Object>> inventory = new ArrayList<>();
        for (ShelterStock row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getItemKey());
            item.put("quantity", row.getQuantity());
            inventory.add(item);
        }

        shelterSupplyService.ensureDefaultShelterStocks();

        out.put("success", true);
        out.put("currentBuildValue", progress.getCurrentBuildValue());
        out.put("inventory", inventory);
        out.put("foodSupply", shelterSupplyService.buildShelterFoodSupply());
        out.put("energyReserve", shelterSupplyService.buildShelterEnergyReserve());
        return out;
    }

    protected void seedDefaultStock() {
        for (String[] pair : DEFAULT_STOCK) {
            ShelterStock s = new ShelterStock();
            s.setItemKey(pair[0]);
            s.setQuantity(Integer.parseInt(pair[1]));
            shelterStockRepository.save(s);
        }
    }
}
