package com.example.snowisland.service;

import com.example.snowisland.entity.GameDaySettings;
import com.example.snowisland.entity.LocationNpc;
import com.example.snowisland.entity.NpcDailyConsumption;
import com.example.snowisland.entity.TradeItem;
import com.example.snowisland.repository.LocationNpcRepository;
import com.example.snowisland.repository.NpcDailyConsumptionRepository;
import com.example.snowisland.util.ItemCatalog;
import com.example.snowisland.util.NpcSurvivalMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class NpcConsumptionService {

    private static final Logger logger = LoggerFactory.getLogger(NpcConsumptionService.class);

    @Autowired
    private LocationNpcRepository npcRepository;

    @Autowired
    private NpcDailyConsumptionRepository consumptionRepository;

    @Autowired
    private NpcInventoryService npcInventoryService;

    @Autowired
    private PlayerConsumptionService playerConsumptionService;

    @Autowired
    private ActivityLogService activityLogService;

    @Transactional
    public Map<String, Object> settleDay(int gameDay) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (gameDay < 1) {
            out.put("settled", 0);
            return out;
        }
        GameDaySettings settings = playerConsumptionService.getOrCreateDaySettings(gameDay);
        int requiredFood = settings.getRequiredFoodUnits() != null
                ? settings.getRequiredFoodUnits() : PlayerConsumptionService.DEFAULT_FOOD_UNITS;
        int requiredHeat = settings.getRequiredFuelKg() != null
                ? settings.getRequiredFuelKg() : PlayerConsumptionService.DEFAULT_FUEL_KG;

        int settled = 0;
        int weakened = 0;
        int died = 0;
        int recovered = 0;
        List<String> deaths = new ArrayList<>();

        for (LocationNpc npc : npcRepository.findAll()) {
            String before = npc.getStatus() != null ? npc.getStatus() : NpcSurvivalMath.STATUS_NORMAL;
            if (!NpcSurvivalMath.participatesInDailySurvival(before)) {
                continue;
            }

            int foodHave = npcInventoryService.getMaterialKg(npc.getId(), ItemCatalog.FOOD_MATERIAL_ID);
            int woodHave = npcInventoryService.getMaterialKg(npc.getId(), ItemCatalog.WOOD_MATERIAL_ID);
            int fuelHave = npcInventoryService.getMaterialKg(npc.getId(), ItemCatalog.FUEL_MATERIAL_ID);

            int eat = Math.min(foodHave, requiredFood);
            if (eat > 0) {
                npcInventoryService.deductItem(npc.getId(), TradeItem.ItemType.material,
                        ItemCatalog.FOOD_MATERIAL_ID, eat);
            }
            NpcSurvivalMath.HeatSpend heat = NpcSurvivalMath.spendHeat(woodHave, fuelHave, requiredHeat);
            if (heat.woodUsed > 0) {
                npcInventoryService.deductItem(npc.getId(), TradeItem.ItemType.material,
                        ItemCatalog.WOOD_MATERIAL_ID, heat.woodUsed);
            }
            if (heat.fuelUsed > 0) {
                npcInventoryService.deductItem(npc.getId(), TradeItem.ItemType.material,
                        ItemCatalog.FUEL_MATERIAL_ID, heat.fuelUsed);
            }

            boolean met = eat >= requiredFood && heat.heatGained >= requiredHeat;
            String after = NpcSurvivalMath.nextStatusAfterDay(before, met);
            npc.setStatus(after);
            npcRepository.save(npc);

            NpcDailyConsumption row = consumptionRepository
                    .findByNpcIdAndGameDay(npc.getId(), gameDay)
                    .orElseGet(NpcDailyConsumption::new);
            row.setNpcId(npc.getId());
            row.setGameDay(gameDay);
            row.setRequiredFoodUnits(requiredFood);
            row.setRequiredFuelKg(requiredHeat);
            row.setConsumedFoodUnits(eat);
            row.setFuelFromWoodKg(heat.woodUsed);
            row.setFuelFromFuelKg(heat.fuelUsed);
            row.setConsumedFuelKg(heat.heatGained);
            row.setRequirementsMet(met);
            row.setResultStatus(after);
            consumptionRepository.save(row);

            String summary = met
                    ? "进食" + eat + "/" + requiredFood + " 取暖" + heat.heatGained + "/" + requiredHeat
                    : "消耗不足 食" + eat + "/" + requiredFood + " 暖" + heat.heatGained + "/" + requiredHeat
                    + " → " + after;
            activityLogService.log(gameDay, null, npc.getName(), null,
                    ActivityLogService.CAT_CONSUME, "NPC消耗：" + summary, before + " → " + after);

            settled += 1;
            if (NpcSurvivalMath.STATUS_DEAD.equals(after) && !NpcSurvivalMath.STATUS_DEAD.equals(before)) {
                died += 1;
                deaths.add(npc.getName());
            } else if (NpcSurvivalMath.STATUS_WEAK.equals(after) && !NpcSurvivalMath.STATUS_WEAK.equals(before)) {
                weakened += 1;
            } else if (NpcSurvivalMath.STATUS_WEAK.equals(before) && NpcSurvivalMath.STATUS_NORMAL.equals(after)) {
                recovered += 1;
            }
            logger.info("NPC日消耗: {} day={} met={} {}→{}", npc.getName(), gameDay, met, before, after);
        }

        out.put("settled", settled);
        out.put("weakened", weakened);
        out.put("died", died);
        out.put("recovered", recovered);
        out.put("deaths", deaths);
        return out;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> historyForNpc(Integer npcId) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (NpcDailyConsumption row : consumptionRepository.findByNpcIdOrderByGameDayDesc(npcId)) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("gameDay", row.getGameDay());
            map.put("requiredFoodUnits", row.getRequiredFoodUnits());
            map.put("requiredFuelKg", row.getRequiredFuelKg());
            map.put("consumedFoodUnits", row.getConsumedFoodUnits());
            map.put("consumedFuelKg", row.getConsumedFuelKg());
            map.put("fuelFromWoodKg", row.getFuelFromWoodKg());
            map.put("fuelFromFuelKg", row.getFuelFromFuelKg());
            map.put("requirementsMet", row.getRequirementsMet());
            map.put("resultStatus", row.getResultStatus());
            out.add(map);
        }
        return out;
    }
}
