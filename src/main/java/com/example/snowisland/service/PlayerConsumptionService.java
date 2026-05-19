package com.example.snowisland.service;

import com.example.snowisland.entity.GameDaySettings;
import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerDailyConsumption;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.TradeItem;
import com.example.snowisland.repository.GameDaySettingsRepository;
import com.example.snowisland.repository.PlayerDailyConsumptionRepository;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.util.ItemCatalog;
import com.example.snowisland.util.PlayerStatusCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PlayerConsumptionService {

  public static final int DEFAULT_FOOD_UNITS = 2;
  public static final int DEFAULT_FUEL_KG = 15;

  @Autowired private PlayerDailyConsumptionRepository consumptionRepository;
  @Autowired private GameDaySettingsRepository gameDaySettingsRepository;
  @Autowired private PlayerRepository playerRepository;
  @Autowired private PlayerItemRepository playerItemRepository;

  public GameDaySettings getOrCreateDaySettings(int gameDay) {
    return gameDaySettingsRepository.findById(gameDay).orElseGet(() -> {
      GameDaySettings s = new GameDaySettings();
      s.setGameDay(gameDay);
      s.setRequiredFoodUnits(DEFAULT_FOOD_UNITS);
      s.setRequiredFuelKg(DEFAULT_FUEL_KG);
      return gameDaySettingsRepository.save(s);
    });
  }

  @Transactional
  public Map<String, Object> saveDaySettings(int gameDay, Integer foodUnits, Integer fuelKg) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (gameDay < 1) {
      out.put("success", false);
      out.put("message", "无效的游戏天数");
      return out;
    }
    GameDaySettings s = getOrCreateDaySettings(gameDay);
    if (foodUnits != null && foodUnits >= 0) {
      s.setRequiredFoodUnits(foodUnits);
    }
    if (fuelKg != null && fuelKg >= 0) {
      s.setRequiredFuelKg(fuelKg);
    }
    gameDaySettingsRepository.save(s);
    out.put("success", true);
    out.put("message", "每日消耗需求已保存");
    out.put("gameDay", gameDay);
    out.put("requiredFoodUnits", s.getRequiredFoodUnits());
    out.put("requiredFuelKg", s.getRequiredFuelKg());
    return out;
  }

  public Map<String, Object> getConsumptionContext(Integer playerId, Integer gameDay) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (playerId == null || gameDay == null || gameDay < 1) {
      out.put("success", false);
      out.put("message", "参数无效");
      return out;
    }
    Optional<Player> opt = playerRepository.findById(playerId);
    if (!opt.isPresent()) {
      out.put("success", false);
      out.put("message", "玩家不存在");
      return out;
    }
    GameDaySettings settings = getOrCreateDaySettings(gameDay);
    PlayerDailyConsumption row = consumptionRepository
        .findByPlayerIdAndGameDay(playerId, gameDay)
        .orElseGet(() -> {
          PlayerDailyConsumption c = new PlayerDailyConsumption();
          c.setPlayerId(playerId);
          c.setGameDay(gameDay);
          c.setRequiredFoodUnits(settings.getRequiredFoodUnits());
          c.setRequiredFuelKg(settings.getRequiredFuelKg());
          return c;
        });

    int foodAvail = availableFoodUnits(playerId);
    int woodAvail = availableMaterialKg(playerId, ItemCatalog.WOOD_MATERIAL_ID);
    int fuelAvail = availableMaterialKg(playerId, ItemCatalog.FUEL_MATERIAL_ID);

    boolean foodMet = row.getConsumedFoodUnits() >= row.getRequiredFoodUnits();
    boolean fuelMet = row.getConsumedFuelKg() >= row.getRequiredFuelKg();

    out.put("success", true);
    out.put("gameDay", gameDay);
    out.put("requiredFoodUnits", row.getRequiredFoodUnits());
    out.put("requiredFuelKg", row.getRequiredFuelKg());
    out.put("consumedFoodUnits", row.getConsumedFoodUnits());
    out.put("consumedFuelKg", row.getConsumedFuelKg());
    out.put("fuelFromWoodKg", row.getFuelFromWoodKg());
    out.put("fuelFromFuelKg", row.getFuelFromFuelKg());
    out.put("foodMet", foodMet);
    out.put("fuelMet", fuelMet);
    out.put("requirementsMet", foodMet && fuelMet);
    out.put("remainingFoodUnits", Math.max(0, row.getRequiredFoodUnits() - row.getConsumedFoodUnits()));
    out.put("remainingFuelKg", Math.max(0, row.getRequiredFuelKg() - row.getConsumedFuelKg()));
    out.put("submitted", Boolean.TRUE.equals(row.getSubmitted()));
    out.put("availableFoodUnits", foodAvail);
    out.put("availableWoodKg", woodAvail);
    out.put("availableFuelKg", fuelAvail);
    out.put("weakWarning", "若当日未满足进食与取暖需求，次日将陷入「虚弱」状态。");
    out.put("statuses", PlayerStatusCatalog.buildStatusList(opt.get()));
    return out;
  }

  @Transactional
  public Map<String, Object> submitConsumption(Integer playerId, Integer gameDay, Map<String, Object> body) {
    Map<String, Object> out = new LinkedHashMap<>();
    if (playerId == null || gameDay == null || gameDay < 1) {
      out.put("success", false);
      out.put("message", "参数无效");
      return out;
    }
    if (!playerRepository.findById(playerId).isPresent()) {
      out.put("success", false);
      out.put("message", "玩家不存在");
      return out;
    }

    GameDaySettings settings = getOrCreateDaySettings(gameDay);
    PlayerDailyConsumption row = consumptionRepository
        .findByPlayerIdAndGameDay(playerId, gameDay)
        .orElseGet(() -> {
          PlayerDailyConsumption c = new PlayerDailyConsumption();
          c.setPlayerId(playerId);
          c.setGameDay(gameDay);
          return c;
        });
    row.setRequiredFoodUnits(settings.getRequiredFoodUnits());
    row.setRequiredFuelKg(settings.getRequiredFuelKg());

    int addFood = intVal(body.get("foodUnits"), 0);
    int addWood = intVal(body.get("woodKg"), 0);
    int addFuel = intVal(body.get("fuelKg"), 0);
    if (addFood < 0 || addWood < 0 || addFuel < 0) {
      out.put("success", false);
      out.put("message", "数量不能为负数");
      return out;
    }
    if (addFood == 0 && addWood == 0 && addFuel == 0) {
      out.put("success", false);
      out.put("message", "请至少提交一项消耗");
      return out;
    }

    int newFood = row.getConsumedFoodUnits() + addFood;
    int newWoodTotal = row.getFuelFromWoodKg() + addWood;
    int newFuelTotal = row.getFuelFromFuelKg() + addFuel;
    int newHeating = newWoodTotal + newFuelTotal;

    if (newFood > row.getRequiredFoodUnits()) {
      out.put("success", false);
      out.put("message", "进食累计将超过当日需求（还需 "
              + Math.max(0, row.getRequiredFoodUnits() - row.getConsumedFoodUnits()) + " 单位）");
      return out;
    }
    if (newHeating > row.getRequiredFuelKg()) {
      out.put("success", false);
      out.put("message", "取暖累计将超过当日需求（还需 "
              + Math.max(0, row.getRequiredFuelKg() - row.getConsumedFuelKg()) + " 千克）");
      return out;
    }

    if (addFood > 0) {
      String deductErr = deductFoodUnits(playerId, addFood);
      if (deductErr != null) {
        out.put("success", false);
        out.put("message", deductErr);
        return out;
      }
    }
    if (addWood > 0) {
      String deductErr = deductMaterialKg(playerId, ItemCatalog.WOOD_MATERIAL_ID, addWood);
      if (deductErr != null) {
        out.put("success", false);
        out.put("message", deductErr);
        return out;
      }
    }
    if (addFuel > 0) {
      String deductErr = deductMaterialKg(playerId, ItemCatalog.FUEL_MATERIAL_ID, addFuel);
      if (deductErr != null) {
        out.put("success", false);
        out.put("message", deductErr);
        return out;
      }
    }

    row.setConsumedFoodUnits(newFood);
    row.setFuelFromWoodKg(newWoodTotal);
    row.setFuelFromFuelKg(newFuelTotal);
    row.setConsumedFuelKg(newHeating);
    row.setSubmitted(newFood >= row.getRequiredFoodUnits() && newHeating >= row.getRequiredFuelKg());
    consumptionRepository.save(row);

    out.put("success", true);
    out.put("message", "消耗已记录");
    return getConsumptionContext(playerId, gameDay);
  }

  /** 结算某日：未达标且未死亡的玩家获得虚弱 */
  @Transactional
  public void applyMissedConsumptionPenalties(int gameDay) {
    if (gameDay < 1) {
      return;
    }
    List<Player> players = playerRepository.findAll();
    for (Player player : players) {
      if (Boolean.TRUE.equals(player.getIsDead())) {
        continue;
      }
      Optional<PlayerDailyConsumption> opt = consumptionRepository.findByPlayerIdAndGameDay(player.getId(), gameDay);
      boolean met = opt.map(c ->
          c.getConsumedFoodUnits() >= c.getRequiredFoodUnits()
              && c.getConsumedFuelKg() >= c.getRequiredFuelKg()).orElse(false);
      if (!met) {
        player.setIsWeak(true);
        playerRepository.save(player);
      }
    }
  }

  private int availableFoodUnits(int playerId) {
    return availableMaterialKg(playerId, ItemCatalog.FOOD_MATERIAL_ID);
  }

  private int availableMaterialKg(int playerId, int materialId) {
    Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(
        playerId, TradeItem.ItemType.material, materialId);
    return opt.map(pi -> pi.getQuantity() != null ? pi.getQuantity() : 0).orElse(0);
  }

  private String deductFoodUnits(int playerId, int units) {
    return deductMaterialKg(playerId, ItemCatalog.FOOD_MATERIAL_ID, units);
  }

  private String deductMaterialKg(int playerId, int materialId, int kg) {
    if (kg <= 0) {
      return null;
    }
    Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(
        playerId, TradeItem.ItemType.material, materialId);
    int have = opt.map(pi -> pi.getQuantity() != null ? pi.getQuantity() : 0).orElse(0);
    if (have < kg) {
      return "物资不足（需要 " + kg + " kg，当前 " + have + " kg）";
    }
    int left = have - kg;
    if (opt.isPresent()) {
      PlayerItem pi = opt.get();
      if (left <= 0) {
        playerItemRepository.delete(pi);
      } else {
        pi.setQuantity(left);
        playerItemRepository.save(pi);
      }
    }
    return null;
  }

  private static int intVal(Object o, int def) {
    if (o == null || "".equals(o)) {
      return def;
    }
    if (o instanceof Number) {
      return ((Number) o).intValue();
    }
    try {
      return Integer.parseInt(String.valueOf(o));
    } catch (NumberFormatException e) {
      return def;
    }
  }
}
