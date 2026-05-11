package com.example.snowisland.service;

import com.example.snowisland.entity.EnergyCatalog;
import com.example.snowisland.entity.FoodCatalog;
import com.example.snowisland.entity.ShelterEnergyStock;
import com.example.snowisland.entity.ShelterFoodStock;
import com.example.snowisland.repository.EnergyCatalogRepository;
import com.example.snowisland.repository.FoodCatalogRepository;
import com.example.snowisland.repository.ShelterEnergyStockRepository;
import com.example.snowisland.repository.ShelterFoodStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 统治者避难所的公共食物与能量库存（{@code shelter_food_stock} / {@code shelter_energy_stock}），
 * 与 {@link PlayerSupplyService} 的玩家个人库存分离。
 */
@Service
public class ShelterSupplyService {

    public static final int FOOD_PERSON_DAY_KCAL = 2500;
    public static final int ENERGY_PERSON_DAY_KCAL = 800;

    /** 空库时写入避难所公共库存（与 sql 种子一致） */
    private static final String[][] DEFAULT_SHELTER_FOOD_STOCK = {
            {"salty_pork", "4"}, {"dried_fish", "3"}, {"flour", "8"}, {"jam", "2"}, {"bread", "20"},
            {"potato", "15"}, {"hard_biscuit", "2"}, {"sauerkraut", "5"}, {"dried_onion", "3"}, {"dried_apple", "2"},
            {"oatmeal", "2"}, {"fish_meat", "6"}, {"goat_milk", "5"}, {"jerky", "12"}, {"smoked_meat", "1"},
            {"canned_food", "8"}, {"candy", "1"}, {"cereal", "2"}, {"military_ration", "10"}, {"shellfish", "4"},
            {"mushroom", "5"}, {"insect_cocoon", "2"}, {"wild_blueberry", "3"}, {"raspberry", "2"},
    };

    private static final String[][] DEFAULT_SHELTER_ENERGY_STOCK = {
            {"firewood", "25"}, {"coal", "10"}, {"fuel_oil", "5"},
    };

    @Autowired
    private FoodCatalogRepository foodCatalogRepository;

    @Autowired
    private EnergyCatalogRepository energyCatalogRepository;

    @Autowired
    private ShelterFoodStockRepository shelterFoodStockRepository;

    @Autowired
    private ShelterEnergyStockRepository shelterEnergyStockRepository;

    @Transactional
    public void ensureDefaultShelterStocks() {
        if (foodCatalogRepository.count() > 0 && shelterFoodStockRepository.count() == 0) {
            for (String[] pair : DEFAULT_SHELTER_FOOD_STOCK) {
                ShelterFoodStock row = new ShelterFoodStock();
                row.setItemKey(pair[0]);
                row.setQuantity(Integer.parseInt(pair[1]));
                shelterFoodStockRepository.save(row);
            }
        }
        if (energyCatalogRepository.count() > 0 && shelterEnergyStockRepository.count() == 0) {
            for (String[] pair : DEFAULT_SHELTER_ENERGY_STOCK) {
                ShelterEnergyStock row = new ShelterEnergyStock();
                row.setItemKey(pair[0]);
                row.setQuantity(Integer.parseInt(pair[1]));
                shelterEnergyStockRepository.save(row);
            }
        }
    }

    public Map<String, Object> buildShelterFoodSupply() {
        List<FoodCatalog> defs = foodCatalogRepository.findAllByOrderBySortOrderAsc();
        if (defs.isEmpty()) {
            return emptyFoodBlock();
        }

        List<ShelterFoodStock> stocks = shelterFoodStockRepository.findAllByOrderByItemKeyAsc();
        Map<String, Integer> qty = new HashMap<>();
        for (ShelterFoodStock s : stocks) {
            qty.put(s.getItemKey(), s.getQuantity());
        }

        long totalKcal = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (FoodCatalog def : defs) {
            int q = qty.getOrDefault(def.getItemKey(), 0);
            long line = (long) q * def.getKcalPerUnit();
            totalKcal += line;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", def.getItemKey());
            row.put("name", def.getName());
            row.put("unit", def.getUnit());
            row.put("quantity", q);
            row.put("kcalPerUnit", def.getKcalPerUnit());
            row.put("lineKcal", line);
            items.add(row);
        }

        double personDays = totalKcal / (double) FOOD_PERSON_DAY_KCAL;
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKcal", totalKcal);
        block.put("personDayDivisor", FOOD_PERSON_DAY_KCAL);
        block.put("personDays", Math.round(personDays * 10.0) / 10.0);
        block.put("items", items);
        block.put("source", "shelter");
        return block;
    }

    public Map<String, Object> buildShelterEnergyReserve() {
        List<EnergyCatalog> defs = energyCatalogRepository.findAllByOrderBySortOrderAsc();
        if (defs.isEmpty()) {
            return emptyEnergyBlock();
        }

        List<ShelterEnergyStock> stocks = shelterEnergyStockRepository.findAllByOrderByItemKeyAsc();
        Map<String, Integer> qty = new HashMap<>();
        for (ShelterEnergyStock s : stocks) {
            qty.put(s.getItemKey(), s.getQuantity());
        }

        long totalKcal = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (EnergyCatalog def : defs) {
            int q = qty.getOrDefault(def.getItemKey(), 0);
            long line = (long) q * def.getKcalPerUnit();
            totalKcal += line;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", def.getItemKey());
            row.put("name", def.getName());
            row.put("unit", def.getUnit());
            row.put("quantity", q);
            row.put("kcalPerUnit", def.getKcalPerUnit());
            row.put("lineKcal", line);
            items.add(row);
        }

        double personDays = totalKcal / (double) ENERGY_PERSON_DAY_KCAL;
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKcal", totalKcal);
        block.put("personDayDivisor", ENERGY_PERSON_DAY_KCAL);
        block.put("personDays", Math.round(personDays * 10.0) / 10.0);
        block.put("items", items);
        block.put("source", "shelter");
        return block;
    }

    private Map<String, Object> emptyFoodBlock() {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("totalKcal", 0L);
        empty.put("personDayDivisor", FOOD_PERSON_DAY_KCAL);
        empty.put("personDays", 0.0);
        empty.put("items", Collections.emptyList());
        empty.put("source", "shelter");
        return empty;
    }

    private Map<String, Object> emptyEnergyBlock() {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("totalKcal", 0L);
        empty.put("personDayDivisor", ENERGY_PERSON_DAY_KCAL);
        empty.put("personDays", 0.0);
        empty.put("items", Collections.emptyList());
        empty.put("source", "shelter");
        return empty;
    }
}
