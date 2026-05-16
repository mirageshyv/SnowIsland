package com.example.snowisland.service;

import com.example.snowisland.entity.ShelterEnergyStock;
import com.example.snowisland.entity.ShelterFoodStock;
import com.example.snowisland.repository.ShelterEnergyStockRepository;
import com.example.snowisland.repository.ShelterFoodStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ShelterSupplyService {

    private static final String[][] FOOD_ITEMS = {
            {"salty_pork", "咸肉"}, {"dried_fish", "鱼干"}, {"flour", "面粉"}, {"jam", "果酱"},
            {"bread", "面包"}, {"potato", "土豆"}, {"hard_biscuit", "硬饼干"}, {"sauerkraut", "酸菜"},
            {"dried_onion", "干洋葱"}, {"dried_apple", "苹果干"}, {"oatmeal", "燕麦片"}, {"fish_meat", "鱼肉"},
            {"goat_milk", "羊奶"}, {"jerky", "肉干"}, {"smoked_meat", "熏肉"}, {"canned_food", "罐头"},
            {"candy", "糖果"}, {"cereal", "麦片"}, {"military_ration", "军用压缩干粮"}, {"shellfish", "贝类"},
            {"mushroom", "食用菌菇"}, {"insect_cocoon", "虫茧"}, {"wild_blueberry", "野生蓝莓"}, {"raspberry", "树莓"},
    };

    private static final String[][] ENERGY_ITEMS = {
            {"firewood", "木柴", "kg"},
            {"coal", "煤炭", "kg"},
            {"fuel_oil", "燃油", "L"},
    };

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
    private ShelterFoodStockRepository shelterFoodStockRepository;

    @Autowired
    private ShelterEnergyStockRepository shelterEnergyStockRepository;

    @Transactional
    public void ensureReady() {
        ensureDefaultShelterStocks();
    }

    @Transactional
    protected void ensureDefaultShelterStocks() {
        if (shelterFoodStockRepository.count() == 0) {
            for (String[] pair : DEFAULT_SHELTER_FOOD_STOCK) {
                ShelterFoodStock row = new ShelterFoodStock();
                row.setItemKey(pair[0]);
                row.setQuantity(Integer.parseInt(pair[1]));
                shelterFoodStockRepository.save(row);
            }
        }
        if (shelterEnergyStockRepository.count() == 0) {
            for (String[] pair : DEFAULT_SHELTER_ENERGY_STOCK) {
                ShelterEnergyStock row = new ShelterEnergyStock();
                row.setItemKey(pair[0]);
                row.setQuantity(Integer.parseInt(pair[1]));
                shelterEnergyStockRepository.save(row);
            }
        }
    }

    public Map<String, Object> buildShelterFoodSupply() {
        Map<String, Integer> qty = new HashMap<>();
        for (ShelterFoodStock s : shelterFoodStockRepository.findAllByOrderByItemKeyAsc()) {
            qty.put(s.getItemKey(), s.getQuantity());
        }

        int totalKg = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        for (String[] def : FOOD_ITEMS) {
            int q = qty.getOrDefault(def[0], 0);
            totalKg += q;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", def[0]);
            row.put("name", def[1]);
            row.put("quantity", q);
            items.add(row);
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKg", totalKg);
        block.put("items", items);
        return block;
    }

    public Map<String, Object> buildShelterEnergyReserve() {
        Map<String, Integer> qty = new HashMap<>();
        for (ShelterEnergyStock s : shelterEnergyStockRepository.findAllByOrderByItemKeyAsc()) {
            qty.put(s.getItemKey(), s.getQuantity());
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (String[] def : ENERGY_ITEMS) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", def[0]);
            row.put("name", def[1]);
            row.put("unit", def[2]);
            row.put("quantity", qty.getOrDefault(def[0], 0));
            items.add(row);
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("items", items);
        return block;
    }
}
