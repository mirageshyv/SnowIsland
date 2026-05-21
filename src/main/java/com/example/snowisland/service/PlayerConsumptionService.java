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
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.*;

@Service
public class PlayerConsumptionService {

  public static final int DEFAULT_FOOD_UNITS = 2;
  public static final int DEFAULT_FUEL_KG = 15;
  public static final int WOOD_HEAT_PER_KG = 1;
  public static final int FUEL_HEAT_PER_KG = 15;

  /** True when the player has met both food and heating requirements for the day. */
  public static boolean isDailyRequirementsMet(PlayerDailyConsumption row) {
    if (row == null) {
      return false;
    }
    int requiredFood = row.getRequiredFoodUnits() != null ? row.getRequiredFoodUnits() : 0;
    int requiredFuel = row.getRequiredFuelKg() != null ? row.getRequiredFuelKg() : 0;
    int consumedFood = row.getConsumedFoodUnits() != null ? row.getConsumedFoodUnits() : 0;
    int consumedFuel = row.getConsumedFuelKg() != null ? row.getConsumedFuelKg() : 0;
    return consumedFood >= requiredFood && consumedFuel >= requiredFuel;
  }

  @Autowired private PlayerDailyConsumptionRepository consumptionRepository;
  @Autowired private GameDaySettingsRepository gameDaySettingsRepository;
  @Autowired private PlayerRepository playerRepository;
  @Autowired private PlayerItemRepository playerItemRepository;
  @Autowired private ActivityLogService activityLogService;

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

  @Transactional(rollbackFor = Exception.class)
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

    boolean confirmOverFuel = boolVal(body.get("confirmOverFuel"));
    int addFood = nonNegativeInt(body.get("foodUnits"));
    int addWood = nonNegativeInt(body.get("woodKg"));
    int addFuel = nonNegativeInt(body.get("fuelKg"));
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
    int newHeating = newWoodTotal * WOOD_HEAT_PER_KG + newFuelTotal * FUEL_HEAT_PER_KG;

    if (newFood > row.getRequiredFoodUnits()) {
      out.put("success", false);
      out.put("message", "进食累计将超过当日需求（还需 "
              + Math.max(0, row.getRequiredFoodUnits() - row.getConsumedFoodUnits()) + " 单位）");
      return out;
    }
    if (newHeating > row.getRequiredFuelKg()) {
      if (!confirmOverFuel) {
        out.put("success", false);
        out.put("needsConfirm", true);
        out.put("message", "取暖投入超过当日仍需的热值。确认后将扣除您填写的全部木材与燃料，并记为满足当日取暖需求。");
        return out;
      }
      newHeating = row.getRequiredFuelKg();
    }

    // Validate all inventory before any deduction (avoid partial wood burn when fuel fails)
    if (addFood > 0) {
      String stockErr = checkMaterialKg(playerId, ItemCatalog.FOOD_MATERIAL_ID, addFood);
      if (stockErr != null) {
        out.put("success", false);
        out.put("message", stockErr);
        return out;
      }
    }
    if (addWood > 0) {
      String stockErr = checkMaterialKg(playerId, ItemCatalog.WOOD_MATERIAL_ID, addWood);
      if (stockErr != null) {
        out.put("success", false);
        out.put("message", stockErr);
        return out;
      }
    }
    if (addFuel > 0) {
      String stockErr = checkMaterialKg(playerId, ItemCatalog.FUEL_MATERIAL_ID, addFuel);
      if (stockErr != null) {
        out.put("success", false);
        out.put("message", stockErr);
        return out;
      }
    }

    try {
      if (addFood > 0) {
        deductMaterialKg(playerId, ItemCatalog.FOOD_MATERIAL_ID, addFood);
      }
      if (addWood > 0) {
        deductMaterialKg(playerId, ItemCatalog.WOOD_MATERIAL_ID, addWood);
      }
      if (addFuel > 0) {
        deductMaterialKg(playerId, ItemCatalog.FUEL_MATERIAL_ID, addFuel);
      }
    } catch (IllegalStateException e) {
      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
      out.put("success", false);
      out.put("message", e.getMessage());
      return out;
    }

    row.setConsumedFoodUnits(newFood);
    row.setFuelFromWoodKg(newWoodTotal);
    row.setFuelFromFuelKg(newFuelTotal);
    row.setConsumedFuelKg(newHeating);
    row.setSubmitted(newFood >= row.getRequiredFoodUnits() && newHeating >= row.getRequiredFuelKg());
    consumptionRepository.save(row);

    Optional<Player> optPlayer = playerRepository.findById(playerId);
    Player player = optPlayer.orElse(null);
    String playerName = player != null ? player.getName() : ("玩家" + playerId);
    String summary = "进食+" + addFood + " 木" + addWood + "kg 燃" + addFuel + "kg";
    String detail = "累计进食 " + newFood + "/" + row.getRequiredFoodUnits()
            + "；取暖 " + newHeating + "/" + row.getRequiredFuelKg() + " 热值";
    activityLogService.log(gameDay, playerId, playerName, ActivityLogService.factionOf(player),
            ActivityLogService.CAT_CONSUME, summary, detail);

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
      boolean met = opt.map(PlayerConsumptionService::isDailyRequirementsMet).orElse(false);
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

  /** Returns error message if player lacks enough material; does not mutate inventory. */
  private String checkMaterialKg(int playerId, int materialId, int kg) {
    if (kg <= 0) {
      return null;
    }
    int have = availableMaterialKg(playerId, materialId);
    if (have < kg) {
      return "物资不足（需要 " + kg + " kg，当前 " + have + " kg）";
    }
    return null;
  }

  private void deductMaterialKg(int playerId, int materialId, int kg) {
    if (kg <= 0) {
      return;
    }
    Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(
        playerId, TradeItem.ItemType.material, materialId);
    int have = opt.map(pi -> pi.getQuantity() != null ? pi.getQuantity() : 0).orElse(0);
    if (have < kg) {
      throw new IllegalStateException(
          "物资不足（需要 " + kg + " kg，当前 " + have + " kg）");
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
  }

  private static int nonNegativeInt(Object o) {
    return Math.max(0, intVal(o, 0));
  }

  private static int intVal(Object o, int def) {
    if (o == null || "".equals(o)) {
      return def;
    }
    if (o instanceof Number) {
      return (int) Math.floor(((Number) o).doubleValue());
    }
    try {
      return (int) Math.floor(Double.parseDouble(String.valueOf(o).trim()));
    } catch (NumberFormatException e) {
      return def;
    }
  }

  private static boolean boolVal(Object o) {
    if (o instanceof Boolean) {
      return (Boolean) o;
    }
    return Boolean.parseBoolean(String.valueOf(o));
  }
}
