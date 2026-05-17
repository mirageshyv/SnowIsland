package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.EnergyRepository;
import com.example.snowisland.repository.FoodRepository;
import com.example.snowisland.repository.PlayerEnergyStockRepository;
import com.example.snowisland.repository.PlayerFoodStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Per-player food / fuel from {@code player_food_stock} and {@code player_energy_stock}.
 */
@Service
public class PlayerSupplyService {

    private static final int FUEL_OIL_ENERGY_ID = 3;

    @Autowired
    private PlayerFoodStockRepository playerFoodStockRepository;

    @Autowired
    private PlayerEnergyStockRepository playerEnergyStockRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private EnergyRepository energyRepository;

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
                int itemId = row.getId() != null ? row.getId().getItemId() : 0;
                int q = positiveQty(row.getQuantity());
                if (itemId == FUEL_OIL_ENERGY_ID) {
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
        Map<Integer, Integer> qty = loadFoodQuantitiesById(playerId);
        Map<Integer, Food> catalog = loadFoodCatalog();
        int totalKg = 0;
        List<Map<String, Object>> items = new ArrayList<>();

        for (Food food : catalog.values()) {
            int q = qty.getOrDefault(food.getId(), 0);
            totalKg += q;
            items.add(foodRow(food.getId(), food.getName(), food.getUnit(), q));
        }

        for (Map.Entry<Integer, Integer> e : qty.entrySet()) {
            if (catalog.containsKey(e.getKey())) {
                continue;
            }
            int q = positiveQty(e.getValue());
            totalKg += q;
            items.add(foodRow(e.getKey(), "食物#" + e.getKey(), "kg", q));
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("totalKg", totalKg);
        block.put("items", items);
        return block;
    }

    public Map<String, Object> buildPlayerEnergyReserve(int playerId) {
        Map<Integer, Integer> qty = loadEnergyQuantitiesById(playerId);
        Map<Integer, Energy> catalog = loadEnergyCatalog();
        List<Map<String, Object>> items = new ArrayList<>();

        for (Energy energy : catalog.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", energy.getId());
            row.put("name", energy.getName());
            row.put("unit", energy.getUnit());
            row.put("quantity", qty.getOrDefault(energy.getId(), 0));
            items.add(row);
        }

        for (Map.Entry<Integer, Integer> e : qty.entrySet()) {
            if (catalog.containsKey(e.getKey())) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getKey());
            row.put("name", "燃料#" + e.getKey());
            row.put("unit", "kg");
            row.put("quantity", positiveQty(e.getValue()));
            items.add(row);
        }

        Map<String, Object> block = new LinkedHashMap<>();
        block.put("items", items);
        return block;
    }

    public List<Map<String, Object>> buildPlayerFoodItemsForTrade(int playerId) {
        Map<Integer, Integer> qty = loadFoodQuantitiesById(playerId);
        Map<Integer, Food> catalog = loadFoodCatalog();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : qty.entrySet()) {
            int q = positiveQty(e.getValue());
            if (q <= 0) {
                continue;
            }
            Food food = catalog.get(e.getKey());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getKey());
            row.put("type", "food");
            row.put("quantity", q);
            if (food != null) {
                row.put("name", food.getName());
                row.put("unit", food.getUnit());
            } else {
                row.put("name", "食物#" + e.getKey());
                row.put("unit", "kg");
            }
            result.add(row);
        }
        return result;
    }

    public List<Map<String, Object>> buildPlayerEnergyItemsForTrade(int playerId) {
        Map<Integer, Integer> qty = loadEnergyQuantitiesById(playerId);
        Map<Integer, Energy> catalog = loadEnergyCatalog();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : qty.entrySet()) {
            int q = positiveQty(e.getValue());
            if (q <= 0) {
                continue;
            }
            Energy energy = catalog.get(e.getKey());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", e.getKey());
            row.put("type", "energy");
            row.put("quantity", q);
            if (energy != null) {
                row.put("name", energy.getName());
                row.put("unit", energy.getUnit());
            } else {
                row.put("name", "燃料#" + e.getKey());
                row.put("unit", "kg");
            }
            result.add(row);
        }
        return result;
    }

    public int getFoodQuantity(int playerId, int foodId) {
        PlayerFoodStockId id = stockFoodId(playerId, foodId);
        return playerFoodStockRepository.findById(id)
                .map(PlayerFoodStock::getQuantity)
                .map(this::positiveQty)
                .orElse(0);
    }

    public int getEnergyQuantity(int playerId, int energyId) {
        PlayerEnergyStockId id = stockEnergyId(playerId, energyId);
        return playerEnergyStockRepository.findById(id)
                .map(PlayerEnergyStock::getQuantity)
                .map(this::positiveQty)
                .orElse(0);
    }

    @Transactional
    public boolean setFoodStock(int playerId, int foodId, int quantity) {
        if (quantity < 0) {
            return false;
        }
        PlayerFoodStockId id = stockFoodId(playerId, foodId);
        Optional<PlayerFoodStock> opt = playerFoodStockRepository.findById(id);
        if (quantity == 0) {
            opt.ifPresent(playerFoodStockRepository::delete);
            return true;
        }
        PlayerFoodStock row = opt.orElseGet(() -> {
            PlayerFoodStock s = new PlayerFoodStock();
            s.setId(id);
            return s;
        });
        row.setQuantity(quantity);
        playerFoodStockRepository.save(row);
        return true;
    }

    @Transactional
    public boolean setEnergyStock(int playerId, int energyId, int quantity) {
        if (quantity < 0) {
            return false;
        }
        PlayerEnergyStockId id = stockEnergyId(playerId, energyId);
        Optional<PlayerEnergyStock> opt = playerEnergyStockRepository.findById(id);
        if (quantity == 0) {
            opt.ifPresent(playerEnergyStockRepository::delete);
            return true;
        }
        PlayerEnergyStock row = opt.orElseGet(() -> {
            PlayerEnergyStock s = new PlayerEnergyStock();
            s.setId(id);
            return s;
        });
        row.setQuantity(quantity);
        playerEnergyStockRepository.save(row);
        return true;
    }

    @Transactional
    public boolean adjustFoodStock(int playerId, int foodId, int delta) {
        PlayerFoodStockId id = stockFoodId(playerId, foodId);
        Optional<PlayerFoodStock> opt = playerFoodStockRepository.findById(id);
        int current = opt.map(PlayerFoodStock::getQuantity).map(this::positiveQty).orElse(0);
        int newQty = current + delta;
        if (newQty < 0) {
            return false;
        }
        if (newQty == 0) {
            opt.ifPresent(playerFoodStockRepository::delete);
            return true;
        }
        PlayerFoodStock row = opt.orElseGet(() -> {
            PlayerFoodStock s = new PlayerFoodStock();
            s.setId(id);
            return s;
        });
        row.setQuantity(newQty);
        playerFoodStockRepository.save(row);
        return true;
    }

    @Transactional
    public boolean adjustEnergyStock(int playerId, int energyId, int delta) {
        PlayerEnergyStockId id = stockEnergyId(playerId, energyId);
        Optional<PlayerEnergyStock> opt = playerEnergyStockRepository.findById(id);
        int current = opt.map(PlayerEnergyStock::getQuantity).map(this::positiveQty).orElse(0);
        int newQty = current + delta;
        if (newQty < 0) {
            return false;
        }
        if (newQty == 0) {
            opt.ifPresent(playerEnergyStockRepository::delete);
            return true;
        }
        PlayerEnergyStock row = opt.orElseGet(() -> {
            PlayerEnergyStock s = new PlayerEnergyStock();
            s.setId(id);
            return s;
        });
        row.setQuantity(newQty);
        playerEnergyStockRepository.save(row);
        return true;
    }

    public String getFoodName(int foodId) {
        return foodRepository.findById(foodId).map(Food::getName).orElse("未知食物");
    }

    public String getFoodUnit(int foodId) {
        return foodRepository.findById(foodId).map(Food::getUnit).orElse("kg");
    }

    public String getEnergyName(int energyId) {
        return energyRepository.findById(energyId).map(Energy::getName).orElse("未知燃料");
    }

    public String getEnergyUnit(int energyId) {
        return energyRepository.findById(energyId).map(Energy::getUnit).orElse("kg");
    }

    private Map<Integer, Food> loadFoodCatalog() {
        Map<Integer, Food> map = new LinkedHashMap<>();
        try {
            for (Food food : foodRepository.findAllByOrderByIdAsc()) {
                map.put(food.getId(), food);
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return map;
    }

    private Map<Integer, Energy> loadEnergyCatalog() {
        Map<Integer, Energy> map = new LinkedHashMap<>();
        try {
            for (Energy energy : energyRepository.findAllByOrderByIdAsc()) {
                map.put(energy.getId(), energy);
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return map;
    }

    private Map<Integer, Integer> loadFoodQuantitiesById(int playerId) {
        Map<Integer, Integer> qty = new HashMap<>();
        try {
            for (PlayerFoodStock s : playerFoodStockRepository.findById_PlayerId(playerId)) {
                if (s.getId() != null) {
                    qty.put(s.getId().getItemId(), positiveQty(s.getQuantity()));
                }
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return qty;
    }

    private Map<Integer, Integer> loadEnergyQuantitiesById(int playerId) {
        Map<Integer, Integer> qty = new HashMap<>();
        try {
            for (PlayerEnergyStock s : playerEnergyStockRepository.findById_PlayerId(playerId)) {
                if (s.getId() != null) {
                    qty.put(s.getId().getItemId(), positiveQty(s.getQuantity()));
                }
            }
        } catch (DataAccessException ignored) {
            // empty
        }
        return qty;
    }

    private static PlayerFoodStockId stockFoodId(int playerId, int foodId) {
        PlayerFoodStockId id = new PlayerFoodStockId();
        id.setPlayerId(playerId);
        id.setItemId(foodId);
        return id;
    }

    private static PlayerEnergyStockId stockEnergyId(int playerId, int energyId) {
        PlayerEnergyStockId id = new PlayerEnergyStockId();
        id.setPlayerId(playerId);
        id.setItemId(energyId);
        return id;
    }

    private static Map<String, Object> foodRow(int id, String name, String unit, int quantity) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", id);
        row.put("name", name);
        row.put("unit", unit);
        row.put("quantity", quantity);
        return row;
    }

    private int positiveQty(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return 0;
        }
        return quantity;
    }
}
