package com.example.snowisland.service;

import com.example.snowisland.entity.PlayerEnergyStock;
import com.example.snowisland.entity.PlayerFoodStock;
import com.example.snowisland.repository.PlayerEnergyStockRepository;
import com.example.snowisland.repository.PlayerFoodStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Per-player food / fuel from {@code player_food_stock} and {@code player_energy_stock}.
 */
@Service
public class PlayerSupplyService {

    private static final String FUEL_OIL_KEY = "fuel_oil";

    /** item_key, display name, unit (kg or portion) */
    private static final String[][] FOOD_ITEMS = {
            {"salty_pork", "咸肉", "kg"}, {"dried_fish", "鱼干", "kg"}, {"flour", "面粉", "kg"}, {"jam", "果酱", "kg"},
            {"bread", "面包", "kg"}, {"potato", "土豆", "kg"}, {"hard_biscuit", "硬饼干", "kg"}, {"sauerkraut", "酸菜", "kg"},
            {"dried_onion", "干洋葱", "kg"}, {"dried_apple", "苹果干", "kg"}, {"oatmeal", "燕麦片", "kg"}, {"fish_meat", "鱼肉", "kg"},
            {"goat_milk", "羊奶", "kg"}, {"jerky", "肉干", "kg"}, {"smoked_meat", "熏肉", "kg"}, {"canned_food", "罐头", "portion"},
            {"candy", "糖果", "kg"}, {"cereal", "麦片", "kg"}, {"military_ration", "军用压缩干粮", "portion"}, {"shellfish", "贝类", "kg"},
            {"mushroom", "食用菌菇", "kg"}, {"insect_cocoon", "虫茧", "portion"}, {"wild_blueberry", "野生蓝莓", "kg"}, {"raspberry", "树莓", "kg"},
    };

    private static final String[][] ENERGY_ITEMS = {
            {"firewood", "木柴", "kg"},
            {"coal", "煤炭", "kg"},
            {"fuel_oil", "燃油", "L"},
    };

    @Autowired
    private PlayerFoodStockRepository playerFoodStockRepository;

    @Autowired
    private PlayerEnergyStockRepository playerEnergyStockRepository;

    public Map<String, Object> getPersonalResourceTotals(int playerId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("foodKg", 0);
        out.put("fuelKg", 0);
        out.put("fuelLiters", 0);
        out.put("hasFoodStockTable", false);
        out.put("hasEnergyStockTable", false);

        try {
            List<PlayerFoodStock> foodRows = playerFoodStockRepository.findById_PlayerId(playerId);
            out.put("hasFoodStockTable", true);
            int foodKg = 0;
            for (PlayerFoodStock row : foodRows) {
                foodKg += positiveQty(row.getQuantity());
            }
            out.put("foodKg", foodKg);
        } catch (DataAccessException ignored) {
            // table missing
        }

        try {
            List<PlayerEnergyStock> energyRows = playerEnergyStockRepository.findById_PlayerId(playerId);
            out.put("hasEnergyStockTable", true);
            int fuelKg = 0;
            int fuelLiters = 0;
            for (PlayerEnergyStock row : energyRows) {
                String key = row.getId() != null ? row.getId().getItemKey() : "";
                int q = positiveQty(row.getQuantity());
                if (FUEL_OIL_KEY.equals(key)) {
                    fuelLiters += q;
                } else {
                    fuelKg += q;
                }
            }
            out.put("fuelKg", fuelKg);
            out.put("fuelLiters", fuelLiters);
        } catch (DataAccessException ignored) {
            // table missing
        }

        return out;
    }

    public Map<String, Object> buildPlayerFoodSupply(int playerId) {
        Map<String, Integer> qty = loadFoodQuantities(playerId);
        int totalKg = 0;
        List<Map<String, Object>> items = new ArrayList<>();
        Set<String> known = new HashSet<>();

        for (String[] def : FOOD_ITEMS) {
            known.add(def[0]);
            int q = qty.getOrDefault(def[0], 0);
            totalKg += q;
            items.add(foodRow(def[0], def[1], def[2], q));
        }

        for (Map.Entry<String, Integer> e : qty.entrySet()) {
            if (known.contains(e.getKey())) {
                continue;
            }
            int q = positiveQty(e.getValue());
            totalKg += q;
            items.add(foodRow(e.getKey(), e.getKey(), "kg", q));
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKg", totalKg);
        block.put("items", items);
        return block;
    }

    public Map<String, Object> buildPlayerEnergyReserve(int playerId) {
        Map<String, Integer> qty = loadEnergyQuantities(playerId);
        List<Map<String, Object>> items = new ArrayList<>();
        Set<String> known = new HashSet<>();

        for (String[] def : ENERGY_ITEMS) {
            known.add(def[0]);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", def[0]);
            row.put("name", def[1]);
            row.put("unit", def[2]);
            row.put("quantity", qty.getOrDefault(def[0], 0));
            items.add(row);
        }

        for (Map.Entry<String, Integer> e : qty.entrySet()) {
            if (known.contains(e.getKey())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getKey());
            row.put("name", e.getKey());
            row.put("unit", "kg");
            row.put("quantity", positiveQty(e.getValue()));
            items.add(row);
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("items", items);
        return block;
    }

    private Map<String, Integer> loadFoodQuantities(int playerId) {
        Map<String, Integer> qty = new HashMap<>();
        try {
            for (PlayerFoodStock s : playerFoodStockRepository.findById_PlayerId(playerId)) {
                if (s.getId() != null) {
                    qty.put(s.getId().getItemKey(), positiveQty(s.getQuantity()));
                }
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return qty;
    }

    private Map<String, Integer> loadEnergyQuantities(int playerId) {
        Map<String, Integer> qty = new HashMap<>();
        try {
            for (PlayerEnergyStock s : playerEnergyStockRepository.findById_PlayerId(playerId)) {
                if (s.getId() != null) {
                    qty.put(s.getId().getItemKey(), positiveQty(s.getQuantity()));
                }
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return qty;
    }

    private static Map<String, Object> foodRow(String id, String name, String unit, int quantity) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("unit", unit);
        row.put("quantity", quantity);
        return row;
    }

    private static int positiveQty(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return 0;
        }
        return quantity;
    }
}
