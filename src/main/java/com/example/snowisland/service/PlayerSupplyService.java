package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 玩家级食物与能量（燃料）库存；种类定义全局，数量按 {@code player_id}。
 */
@Service
public class PlayerSupplyService {

    public static final int FOOD_PERSON_DAY_KCAL = 2500;
    public static final int ENERGY_PERSON_DAY_KCAL = 800;

    /** 新玩家首次补全个人食物库存时写入的行（与 player_food_stock 种子一致） */
    private static final String[][] DEFAULT_FOOD_STOCK = {
            {"bread", "5"}, {"jerky", "2"}, {"candy", "3"}, {"military_ration", "2"},
    };

    private static final String[][] DEFAULT_ENERGY_STOCK = {
            {"firewood", "3"}, {"coal", "1"},
    };

    @Autowired
    private FoodCatalogRepository foodCatalogRepository;

    @Autowired
    private EnergyCatalogRepository energyCatalogRepository;

    @Autowired
    private PlayerFoodStockRepository playerFoodStockRepository;

    @Autowired
    private PlayerEnergyStockRepository playerEnergyStockRepository;

    @Transactional
    public void ensureDefaultStocksForPlayer(int playerId) {
        if (foodCatalogRepository.count() > 0 && playerFoodStockRepository.countById_PlayerId(playerId) == 0) {
            seedDefaultFoodStock(playerId);
        }
        if (energyCatalogRepository.count() > 0 && playerEnergyStockRepository.countById_PlayerId(playerId) == 0) {
            seedDefaultEnergyStock(playerId);
        }
    }

    public Map<String, Object> buildFoodSupply(int playerId) {
        List<FoodCatalog> defs = foodCatalogRepository.findAllByOrderBySortOrderAsc();
        if (defs.isEmpty()) {
            return emptyBlock(FOOD_PERSON_DAY_KCAL, playerId);
        }

        List<PlayerFoodStock> stocks = playerFoodStockRepository.findById_PlayerId(playerId);
        Map<String, Integer> qty = new HashMap<>();
        for (PlayerFoodStock s : stocks) {
            qty.put(s.getId().getItemKey(), s.getQuantity());
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
        block.put("playerId", playerId);
        return block;
    }

    public Map<String, Object> buildEnergyReserve(int playerId) {
        List<EnergyCatalog> defs = energyCatalogRepository.findAllByOrderBySortOrderAsc();
        if (defs.isEmpty()) {
            return emptyBlock(ENERGY_PERSON_DAY_KCAL, playerId);
        }

        List<PlayerEnergyStock> stocks = playerEnergyStockRepository.findById_PlayerId(playerId);
        Map<String, Integer> qty = new HashMap<>();
        for (PlayerEnergyStock s : stocks) {
            qty.put(s.getId().getItemKey(), s.getQuantity());
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
        block.put("playerId", playerId);
        return block;
    }

    private static Map<String, Object> emptyBlock(int divisor, int playerId) {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("totalKcal", 0L);
        empty.put("personDayDivisor", divisor);
        empty.put("personDays", 0.0);
        empty.put("items", Collections.emptyList());
        empty.put("playerId", playerId);
        return empty;
    }

    protected void seedDefaultFoodStock(int playerId) {
        for (String[] pair : DEFAULT_FOOD_STOCK) {
            PlayerFoodStockId id = new PlayerFoodStockId();
            id.setPlayerId(playerId);
            id.setItemKey(pair[0]);
            PlayerFoodStock row = new PlayerFoodStock();
            row.setId(id);
            row.setQuantity(Integer.parseInt(pair[1]));
            playerFoodStockRepository.save(row);
        }
    }

    protected void seedDefaultEnergyStock(int playerId) {
        for (String[] pair : DEFAULT_ENERGY_STOCK) {
            PlayerEnergyStockId id = new PlayerEnergyStockId();
            id.setPlayerId(playerId);
            id.setItemKey(pair[0]);
            PlayerEnergyStock row = new PlayerEnergyStock();
            row.setId(id);
            row.setQuantity(Integer.parseInt(pair[1]));
            playerEnergyStockRepository.save(row);
        }
    }
}
