package com.example.snowisland.service;

import com.example.snowisland.entity.ArkConstruction;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.repository.ArkConstructionRepository;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.ShelterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ArkService {

    private static final int WOOD_MATERIAL_ID = 2;
    private static final int METAL_MATERIAL_ID = 1;
    private static final int SEALANT_MATERIAL_ID = 6;
    private static final int ENGINE_MATERIAL_ID = 10;
    private static final int PROPELLER_MATERIAL_ID = 11;
    private static final int GENERATOR_MATERIAL_ID = 12;

    @Autowired
    private ArkConstructionRepository arkConstructionRepository;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    @Autowired
    private ShelterStockRepository shelterStockRepository;

    @Autowired
    private WarehouseService warehouseService;

    /** 参考规则：50点载重基础船体 */
    private static final double TARGET_WOOD = 250.0;
    private static final double TARGET_METAL = 100.0;
    private static final int TARGET_SEALANT = 100;

    private static final int TARGET_ENGINE_MIN = 1;
    private static final int TARGET_ENGINE_MAX = 3;
    private static final int TARGET_PROPELLER = 2;
    private static final int TARGET_GENERATOR = 1;

    private static final double DAILY_WOOD_LIMIT = 30.0;
    private static final double DAILY_METAL_LIMIT = 20.0;
    private static final int DAILY_SEALANT_LIMIT = 20;

    private static final double WORK_EQUIVALENT_WOOD = 5.0;
    private static final double WORK_EQUIVALENT_METAL_KG = 5000.0;
    private static final int WORK_EQUIVALENT_SEALANT = 5;

    private static final double KG_PER_TON = 1000.0;

    private static final int BASE_CARGO_CAPACITY = 50;
    private static final int EXTRA_CARGO_PER_SET = 2;
    private static final double EXTRA_WOOD_PER_SET = 10.0;
    private static final double EXTRA_METAL_PER_SET = 5.0;
    private static final int EXTRA_SEALANT_PER_SET = 5;

    private static final int SHORTAGE_PENALTY_CAPACITY = 3;
    private static final double SHORTAGE_PENALTY_PROGRESS = 2.5;
    private static final double SHORTAGE_WOOD_UNIT = 10.0;
    private static final double SHORTAGE_METAL_UNIT = 5.0;
    private static final int SHORTAGE_SEALANT_UNIT = 5;

    public ArkConstruction getOrCreate() {
        return arkConstructionRepository.findById(ArkConstruction.SINGLETON_ID)
                .orElseGet(() -> {
                    ArkConstruction construction = new ArkConstruction();
                    construction.setId(ArkConstruction.SINGLETON_ID);
                    // 参考规则：初始已完成 10吨木材、20吨金属制品
                    construction.setCurrentWood(10.0);
                    construction.setCurrentMetal(20.0);
                    construction.setCurrentSealant(0);
                    construction.setEngineCount(0);
                    construction.setPropellerCount(0);
                    construction.setGeneratorCount(0);
                    construction.setHasSail(false);
                    ArkConstruction saved = arkConstructionRepository.save(construction);
                    calculateAndUpdateProgress(saved);
                    return arkConstructionRepository.save(saved);
                });
    }

    public Map<String, Object> getStatus() {
        ArkConstruction ark = getOrCreate();
        Map<String, Object> result = new HashMap<>();

        result.put("success", true);
        result.put("currentWood", ark.getCurrentWood());
        result.put("currentMetal", ark.getCurrentMetal());
        result.put("currentSealant", ark.getCurrentSealant());
        result.put("engineCount", ark.getEngineCount());
        result.put("propellerCount", ark.getPropellerCount());
        result.put("generatorCount", ark.getGeneratorCount());
        result.put("currentCargoCapacity", ark.getCurrentCargoCapacity());
        result.put("completionPercentage", ark.getCompletionPercentage());
        result.put("bonusPercentage", resolveBonusPercentage(ark));
        result.put("hasSail", ark.getHasSail());

        result.put("targetWood", TARGET_WOOD);
        result.put("targetMetal", TARGET_METAL);
        result.put("targetSealant", TARGET_SEALANT);
        result.put("targetEngineMin", TARGET_ENGINE_MIN);
        result.put("targetEngineMax", TARGET_ENGINE_MAX);
        result.put("targetPropeller", TARGET_PROPELLER);
        result.put("targetGenerator", TARGET_GENERATOR);

        result.put("dailyWoodLimit", DAILY_WOOD_LIMIT);
        result.put("dailyMetalLimit", DAILY_METAL_LIMIT);
        result.put("dailySealantLimit", DAILY_SEALANT_LIMIT);

        Map<String, Object> shortageInfo = calculateShortagePenalty(ark);
        result.put("shortagePenalty", shortageInfo);

        Map<String, Object> extraCargoInfo = calculateExtraCargo(ark);
        result.put("extraCargo", extraCargoInfo);

        return result;
    }

    @Transactional
    public Map<String, Object> investResources(Double wood, Double metal, Integer sealant) {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            if (wood != null && wood > 0) {
                ark.setCurrentWood(round2(ark.getCurrentWood() + wood));
            }
            if (metal != null && metal > 0) {
                ark.setCurrentMetal(round2(ark.getCurrentMetal() + metal));
            }
            if (sealant != null && sealant > 0) {
                ark.setCurrentSealant(ark.getCurrentSealant() + sealant);
            }

            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);

            result.put("success", true);
            result.put("message", "资源投入成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "资源投入失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> removeResources(Double wood, Double metal, Integer sealant) {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();

            List<String> errors = new ArrayList<>();

            double removeWood = wood != null ? wood : 0;
            double removeMetal = metal != null ? metal : 0;
            int removeSealant = sealant != null ? sealant : 0;

            if (removeWood <= 0 && removeMetal <= 0 && removeSealant <= 0) {
                errors.add("请至少选择一项要移除的资源");
            }

            if (removeWood > 0 && removeWood > ark.getCurrentWood()) {
                errors.add("木材移除量(" + round2Str(removeWood) + "吨)超出当前库存(" + round2Str(ark.getCurrentWood()) + "吨)");
            }
            if (removeMetal > 0 && removeMetal > ark.getCurrentMetal()) {
                errors.add("金属移除量(" + round2Str(removeMetal) + "吨)超出当前库存(" + round2Str(ark.getCurrentMetal()) + "吨)");
            }
            if (removeSealant > 0 && removeSealant > ark.getCurrentSealant()) {
                errors.add("密封材料移除量(" + removeSealant + "kg)超出当前库存(" + ark.getCurrentSealant() + "kg)");
            }

            if (!errors.isEmpty()) {
                result.put("success", false);
                result.put("message", String.join("; ", errors));
                return result;
            }

            if (removeWood > 0) {
                ark.setCurrentWood(round2(ark.getCurrentWood() - removeWood));
            }
            if (removeMetal > 0) {
                ark.setCurrentMetal(round2(ark.getCurrentMetal() - removeMetal));
            }
            if (removeSealant > 0) {
                ark.setCurrentSealant(ark.getCurrentSealant() - removeSealant);
            }

            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);

            result.put("success", true);
            result.put("message", "资源移除成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "资源移除失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> investFromFactionAction(Integer playerId, Double woodKg, Double metalKg, Integer sealantKg,
                                                        Double woodFromWarehouseKg, Double metalFromWarehouseKg, Integer sealantFromWarehouseKg,
                                                        Integer engineCount, Integer generatorCount, Integer propellerCount, Boolean buildSail,
                                                        String workType, String note, Integer gameDay) {
        Map<String, Object> result = new HashMap<>();
        try {
            double personalWoodKg = woodKg != null ? woodKg : 0;
            double personalMetalKg = metalKg != null ? metalKg : 0;
            int personalSealantKg = sealantKg != null ? sealantKg : 0;
            double whWoodKg = woodFromWarehouseKg != null ? woodFromWarehouseKg : 0;
            double whMetalKg = metalFromWarehouseKg != null ? metalFromWarehouseKg : 0;
            int whSealantKg = sealantFromWarehouseKg != null ? sealantFromWarehouseKg : 0;

            int playerWoodQty = getPlayerMaterialQuantity(playerId, WOOD_MATERIAL_ID);
            int playerMetalQty = getPlayerMaterialQuantity(playerId, METAL_MATERIAL_ID);
            int playerSealantQty = getPlayerMaterialQuantity(playerId, SEALANT_MATERIAL_ID);
            double warehouseWoodQty = getWarehouseStockQuantity(ShelterStock.ItemType.material, WOOD_MATERIAL_ID);
            double warehouseMetalQty = getWarehouseStockQuantity(ShelterStock.ItemType.material, METAL_MATERIAL_ID);
            double warehouseSealantQty = getWarehouseStockQuantity(ShelterStock.ItemType.material, SEALANT_MATERIAL_ID);

            List<String> errors = new ArrayList<>();
            if (personalWoodKg > playerWoodQty) {
                errors.add("木材个人投入(" + personalWoodKg + "kg)超出库存上限(" + playerWoodQty + "kg)");
            }
            if (personalMetalKg > playerMetalQty) {
                errors.add("金属个人投入(" + personalMetalKg + "kg)超出库存上限(" + playerMetalQty + "kg)");
            }
            if (personalSealantKg > playerSealantQty) {
                errors.add("密封材料(沥青)个人投入(" + personalSealantKg + "kg)超出库存上限(" + playerSealantQty + "kg)");
            }
            if (whWoodKg > warehouseWoodQty) {
                errors.add("木材仓库投入(" + (int) whWoodKg + "kg)超出仓库库存上限(" + (int) warehouseWoodQty + "kg)");
            }
            if (whMetalKg > warehouseMetalQty) {
                errors.add("金属仓库投入(" + (int) whMetalKg + "kg)超出仓库库存上限(" + (int) warehouseMetalQty + "kg)");
            }
            if (whSealantKg > warehouseSealantQty) {
                errors.add("密封材料(沥青)仓库投入(" + whSealantKg + "kg)超出仓库库存上限(" + (int) warehouseSealantQty + "kg)");
            }

            int playerEngineQty = getPlayerMaterialQuantity(playerId, ENGINE_MATERIAL_ID);
            int playerPropellerQty = getPlayerMaterialQuantity(playerId, PROPELLER_MATERIAL_ID);
            int playerGeneratorQty = getPlayerMaterialQuantity(playerId, GENERATOR_MATERIAL_ID);
            int whEngineQty = (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, ENGINE_MATERIAL_ID);
            int whPropellerQty = (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, PROPELLER_MATERIAL_ID);
            int whGeneratorQty = (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, GENERATOR_MATERIAL_ID);

            int reqEngine = engineCount != null ? engineCount : 0;
            int reqPropeller = propellerCount != null ? propellerCount : 0;
            int reqGenerator = generatorCount != null ? generatorCount : 0;

            if (reqEngine > playerEngineQty + whEngineQty) {
                errors.add("发动机数量不足(个人" + playerEngineQty + "个+仓库" + whEngineQty + "个，需要" + reqEngine + "个)");
            }
            if (reqPropeller > playerPropellerQty + whPropellerQty) {
                errors.add("螺旋桨数量不足(个人" + playerPropellerQty + "个+仓库" + whPropellerQty + "个，需要" + reqPropeller + "个)");
            }
            if (reqGenerator > playerGeneratorQty + whGeneratorQty) {
                errors.add("发电机数量不足(个人" + playerGeneratorQty + "个+仓库" + whGeneratorQty + "个，需要" + reqGenerator + "个)");
            }

            double totalWoodKg = personalWoodKg + whWoodKg;
            double totalMetalKg = personalMetalKg + whMetalKg;
            int totalSealantKg = personalSealantKg + whSealantKg;

            double totalWoodTon = totalWoodKg / KG_PER_TON;
            double totalMetalTon = totalMetalKg / KG_PER_TON;

            if (totalWoodTon > DAILY_WOOD_LIMIT) {
                errors.add("木材总投入" + round2Str(totalWoodTon) + "吨超出每日上限" + DAILY_WOOD_LIMIT + "吨");
            }
            if (totalMetalTon > DAILY_METAL_LIMIT) {
                errors.add("金属总投入" + round2Str(totalMetalTon) + "吨超出每日上限" + DAILY_METAL_LIMIT + "吨");
            }
            if (totalSealantKg > DAILY_SEALANT_LIMIT) {
                errors.add("密封材料(沥青)总投入" + totalSealantKg + "kg超出每日上限" + DAILY_SEALANT_LIMIT + "kg");
            }

            if (!errors.isEmpty()) {
                result.put("success", false);
                result.put("message", String.join("; ", errors));
                result.put("limitErrors", errors);
                return result;
            }

            if (personalWoodKg > 0) {
                deductPlayerMaterial(playerId, WOOD_MATERIAL_ID, (int) personalWoodKg);
            }
            if (personalMetalKg > 0) {
                deductPlayerMaterial(playerId, METAL_MATERIAL_ID, (int) personalMetalKg);
            }
            if (personalSealantKg > 0) {
                deductPlayerMaterial(playerId, SEALANT_MATERIAL_ID, personalSealantKg);
            }
            if (whWoodKg > 0) {
                deductWarehouseStock(ShelterStock.ItemType.material, WOOD_MATERIAL_ID, (int) whWoodKg);
            }
            if (whMetalKg > 0) {
                deductWarehouseStock(ShelterStock.ItemType.material, METAL_MATERIAL_ID, (int) whMetalKg);
            }
            if (whSealantKg > 0) {
                deductWarehouseStock(ShelterStock.ItemType.material, SEALANT_MATERIAL_ID, whSealantKg);
            }

            // 扣除组件物资：优先从个人库存扣除，不足部分从仓库扣除
            if (reqEngine > 0) {
                int fromPlayer = Math.min(reqEngine, playerEngineQty);
                int fromWh = reqEngine - fromPlayer;
                if (fromPlayer > 0) deductPlayerMaterial(playerId, ENGINE_MATERIAL_ID, fromPlayer);
                if (fromWh > 0) deductWarehouseStock(ShelterStock.ItemType.material, ENGINE_MATERIAL_ID, fromWh);
            }
            if (reqPropeller > 0) {
                int fromPlayer = Math.min(reqPropeller, playerPropellerQty);
                int fromWh = reqPropeller - fromPlayer;
                if (fromPlayer > 0) deductPlayerMaterial(playerId, PROPELLER_MATERIAL_ID, fromPlayer);
                if (fromWh > 0) deductWarehouseStock(ShelterStock.ItemType.material, PROPELLER_MATERIAL_ID, fromWh);
            }
            if (reqGenerator > 0) {
                int fromPlayer = Math.min(reqGenerator, playerGeneratorQty);
                int fromWh = reqGenerator - fromPlayer;
                if (fromPlayer > 0) deductPlayerMaterial(playerId, GENERATOR_MATERIAL_ID, fromPlayer);
                if (fromWh > 0) deductWarehouseStock(ShelterStock.ItemType.material, GENERATOR_MATERIAL_ID, fromWh);
            }

            ArkConstruction ark = getOrCreate();

            if (totalWoodTon > 0) {
                ark.setCurrentWood(round2(ark.getCurrentWood() + totalWoodTon));
            }
            if (totalMetalTon > 0) {
                ark.setCurrentMetal(round2(ark.getCurrentMetal() + totalMetalTon));
            }
            if (totalSealantKg > 0) {
                ark.setCurrentSealant(ark.getCurrentSealant() + totalSealantKg);
            }

            if (engineCount != null && engineCount > 0) {
                ark.setEngineCount(Math.min(3, ark.getEngineCount() + engineCount));
            }
            if (propellerCount != null && propellerCount > 0) {
                ark.setPropellerCount(ark.getPropellerCount() + propellerCount);
            }
            if (generatorCount != null && generatorCount > 0) {
                ark.setGeneratorCount(ark.getGeneratorCount() + generatorCount);
            }
            if (Boolean.TRUE.equals(buildSail) && !ark.getHasSail()) {
                ark.setHasSail(true);
            }

            if (workType != null && !workType.isEmpty()) {
                switch (workType) {
                    case "wood":
                        ark.setCurrentWood(round2(ark.getCurrentWood() + WORK_EQUIVALENT_WOOD));
                        break;
                    case "metal":
                        ark.setCurrentMetal(round2(ark.getCurrentMetal() + WORK_EQUIVALENT_METAL_KG / KG_PER_TON));
                        break;
                    case "sealant":
                        ark.setCurrentSealant(ark.getCurrentSealant() + WORK_EQUIVALENT_SEALANT);
                        break;
                }
            }

            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);

            result.put("success", true);
            result.put("message", "方舟建设资源投入成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "方舟建设失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> installComponent(String componentType, Integer count) {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            switch (componentType) {
                case "engine":
                    ark.setEngineCount(Math.max(0, Math.min(3, count)));
                    break;
                case "propeller":
                    ark.setPropellerCount(count);
                    break;
                case "generator":
                    ark.setGeneratorCount(count);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "无效的组件类型");
                    return result;
            }
            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);
            result.put("success", true);
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "安装组件失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> buildSail() {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            if (ark.getHasSail()) {
                result.put("success", false);
                result.put("message", "帆已经建造过了");
                return result;
            }
            ark.setHasSail(true);
            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);
            result.put("success", true);
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "建造帆失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> reset() {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            ark.setCurrentWood(5.0);
            ark.setCurrentMetal(1.0);
            ark.setCurrentSealant(0);
            ark.setEngineCount(0);
            ark.setPropellerCount(0);
            ark.setGeneratorCount(0);
            ark.setHasSail(false);
            ark.setBonusPercentage(BigDecimal.ZERO);
            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);
            result.put("success", true);
            result.put("message", "重置成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> setBonus(BigDecimal value) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (value == null) {
                result.put("success", false);
                result.put("message", "加成值不能为空");
                return result;
            }
            BigDecimal clamped = value;
            if (clamped.compareTo(new BigDecimal("-100")) < 0) {
                clamped = new BigDecimal("-100");
            }
            if (clamped.compareTo(new BigDecimal("100")) > 0) {
                clamped = new BigDecimal("100");
            }
            ArkConstruction ark = getOrCreate();
            ark.setBonusPercentage(clamped);
            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);
            result.put("success", true);
            result.put("message", "仪式加成设置成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "设置仪式加成失败: " + e.getMessage());
        }
        return result;
    }

    private void calculateAndUpdateProgress(ArkConstruction ark) {
        BigDecimal woodProgress = calculateResourceProgress(ark.getCurrentWood(), TARGET_WOOD, 25.0);
        BigDecimal metalProgress = calculateResourceProgress(ark.getCurrentMetal(), TARGET_METAL, 25.0);
        BigDecimal sealantProgress = calculateResourceProgress(ark.getCurrentSealant(), TARGET_SEALANT, 25.0);

        double engineProgress = 0;
        if (ark.getEngineCount() >= TARGET_ENGINE_MAX) {
            engineProgress = 10.0;
        } else if (ark.getEngineCount() >= TARGET_ENGINE_MIN) {
            engineProgress = 10.0 * ark.getEngineCount() / TARGET_ENGINE_MAX;
        }

        double propellerProgress = 0;
        if (ark.getPropellerCount() >= TARGET_PROPELLER) {
            propellerProgress = 10.0;
        } else if (ark.getPropellerCount() > 0) {
            propellerProgress = 10.0 * ark.getPropellerCount() / TARGET_PROPELLER;
        }

        BigDecimal generatorProgress = ark.getGeneratorCount() >= TARGET_GENERATOR ? new BigDecimal("5.0") : BigDecimal.ZERO;

        BigDecimal totalProgress = woodProgress.add(metalProgress).add(sealantProgress)
                .add(new BigDecimal(engineProgress))
                .add(new BigDecimal(propellerProgress))
                .add(generatorProgress);

        Map<String, Object> shortageInfo = calculateShortagePenalty(ark);
        int penaltyCount = ((Number) shortageInfo.get("penaltyCount")).intValue();
        double progressPenalty = penaltyCount * SHORTAGE_PENALTY_PROGRESS;

        BigDecimal finalProgress = totalProgress.subtract(new BigDecimal(progressPenalty));
        if (finalProgress.compareTo(BigDecimal.ZERO) < 0) {
            finalProgress = BigDecimal.ZERO;
        }
        finalProgress = finalProgress.add(resolveBonusPercentage(ark));
        if (finalProgress.compareTo(new BigDecimal("100.0")) > 0) {
            finalProgress = new BigDecimal("100.0");
        }

        ark.setCompletionPercentage(finalProgress);

        int baseCapacity = BASE_CARGO_CAPACITY;

        Map<String, Object> extraCargoInfo = calculateExtraCargo(ark);
        int extraCargoSets = ((Number) extraCargoInfo.get("extraSets")).intValue();
        baseCapacity += extraCargoSets * EXTRA_CARGO_PER_SET;

        int capacityPenalty = penaltyCount * SHORTAGE_PENALTY_CAPACITY;
        int actualCapacity = Math.max(0, baseCapacity - capacityPenalty);
        ark.setCurrentCargoCapacity(actualCapacity);
    }

    private Map<String, Object> calculateShortagePenalty(ArkConstruction ark) {
        Map<String, Object> result = new HashMap<>();

        double woodShortage = Math.max(0, TARGET_WOOD - ark.getCurrentWood());
        double metalShortage = Math.max(0, TARGET_METAL - ark.getCurrentMetal());
        int sealantShortage = Math.max(0, TARGET_SEALANT - ark.getCurrentSealant());

        int woodPenaltyCount = (int) (woodShortage / SHORTAGE_WOOD_UNIT);
        int metalPenaltyCount = (int) (metalShortage / SHORTAGE_METAL_UNIT);
        int sealantPenaltyCount = sealantShortage / SHORTAGE_SEALANT_UNIT;

        int penaltyCount = Math.min(Math.min(woodPenaltyCount, metalPenaltyCount), sealantPenaltyCount);

        result.put("woodShortage", round2(woodShortage));
        result.put("metalShortage", round2(metalShortage));
        result.put("sealantShortage", sealantShortage);
        result.put("woodPenaltyCount", woodPenaltyCount);
        result.put("metalPenaltyCount", metalPenaltyCount);
        result.put("sealantPenaltyCount", sealantPenaltyCount);
        result.put("penaltyCount", penaltyCount);
        result.put("capacityPenalty", penaltyCount * SHORTAGE_PENALTY_CAPACITY);
        result.put("progressPenalty", penaltyCount * SHORTAGE_PENALTY_PROGRESS);

        return result;
    }

    private Map<String, Object> calculateExtraCargo(ArkConstruction ark) {
        Map<String, Object> result = new HashMap<>();

        double extraWood = Math.max(0, ark.getCurrentWood() - TARGET_WOOD);
        double extraMetal = Math.max(0, ark.getCurrentMetal() - TARGET_METAL);
        int extraSealant = Math.max(0, ark.getCurrentSealant() - TARGET_SEALANT);

        int woodSets = (int) (extraWood / EXTRA_WOOD_PER_SET);
        int metalSets = (int) (extraMetal / EXTRA_METAL_PER_SET);
        int sealantSets = extraSealant / EXTRA_SEALANT_PER_SET;

        int extraSets = Math.min(Math.min(woodSets, metalSets), sealantSets);

        result.put("extraWood", round2(extraWood));
        result.put("extraMetal", round2(extraMetal));
        result.put("extraSealant", extraSealant);
        result.put("woodSets", woodSets);
        result.put("metalSets", metalSets);
        result.put("sealantSets", sealantSets);
        result.put("extraSets", extraSets);
        result.put("extraCapacity", extraSets * EXTRA_CARGO_PER_SET);

        return result;
    }

    private BigDecimal resolveBonusPercentage(ArkConstruction ark) {
        BigDecimal bonus = ark.getBonusPercentage();
        return bonus != null ? bonus : BigDecimal.ZERO;
    }

    private BigDecimal calculateResourceProgress(double current, double target, double maxPercent) {
        if (target <= 0) return BigDecimal.ZERO;
        double ratio = Math.min(1.0, current / target);
        return new BigDecimal(ratio * maxPercent).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateResourceProgress(int current, int target, double maxPercent) {
        return calculateResourceProgress((double) current, (double) target, maxPercent);
    }

    private Map<String, Object> buildStatusResponse(ArkConstruction ark) {
        Map<String, Object> data = new HashMap<>();
        data.put("currentWood", ark.getCurrentWood());
        data.put("currentMetal", ark.getCurrentMetal());
        data.put("currentSealant", ark.getCurrentSealant());
        data.put("engineCount", ark.getEngineCount());
        data.put("propellerCount", ark.getPropellerCount());
        data.put("generatorCount", ark.getGeneratorCount());
        data.put("currentCargoCapacity", ark.getCurrentCargoCapacity());
        data.put("completionPercentage", ark.getCompletionPercentage());
        data.put("bonusPercentage", resolveBonusPercentage(ark));
        data.put("hasSail", ark.getHasSail());

        data.put("targetWood", TARGET_WOOD);
        data.put("targetMetal", TARGET_METAL);
        data.put("targetSealant", TARGET_SEALANT);
        data.put("targetEngineMin", TARGET_ENGINE_MIN);
        data.put("targetEngineMax", TARGET_ENGINE_MAX);
        data.put("targetPropeller", TARGET_PROPELLER);
        data.put("targetGenerator", TARGET_GENERATOR);

        data.put("dailyWoodLimit", DAILY_WOOD_LIMIT);
        data.put("dailyMetalLimit", DAILY_METAL_LIMIT);
        data.put("dailySealantLimit", DAILY_SEALANT_LIMIT);

        data.put("shortagePenalty", calculateShortagePenalty(ark));
        data.put("extraCargo", calculateExtraCargo(ark));

        return data;
    }

    private double round2(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String round2Str(double val) {
        return String.format("%.2f", val);
    }

    private int getPlayerMaterialQuantity(Integer playerId, int materialId) {
        return playerItemRepository.findByPlayerIdAndItemTypeAndItemId(playerId, ItemType.material, materialId)
                .map(PlayerItem::getQuantity)
                .orElse(0);
    }

    private double getWarehouseStockQuantity(ShelterStock.ItemType itemType, int itemId) {
        Map<String, Object> stock = warehouseService.getWarehouseStock("ark", null, "dm");
        if (!Boolean.TRUE.equals(stock.get("success"))) {
            return 0;
        }
        List<Map<String, Object>> items = (List<Map<String, Object>>) stock.get("items");
        if (items == null || items.isEmpty()) {
            return 0;
        }
        String typeStr = itemType.name().toLowerCase();
        return items.stream()
                .filter(item -> typeStr.equals(item.get("itemType")) && itemId == ((Number) item.get("itemId")).intValue())
                .mapToDouble(item -> ((Number) item.get("quantity")).doubleValue())
                .findFirst()
                .orElse(0);
    }

    private void deductPlayerMaterial(Integer playerId, int materialId, int kg) {
        if (kg <= 0) return;
        Optional<PlayerItem> opt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(playerId, ItemType.material, materialId);
        if (!opt.isPresent()) return;
        PlayerItem item = opt.get();
        int newQty = Math.max(0, item.getQuantity() - kg);
        if (newQty == 0) {
            playerItemRepository.delete(item);
        } else {
            item.setQuantity(newQty);
            playerItemRepository.save(item);
        }
    }

    private void deductWarehouseStock(ShelterStock.ItemType itemType, int itemId, int kg) {
        if (kg <= 0) return;
        double currentQty = getWarehouseStockQuantity(itemType, itemId);
        int newQty = Math.max(0, (int) currentQty - kg);
        warehouseService.updateWarehouseStock("ark", itemType.name().toLowerCase(), itemId, newQty, "dm");
    }

    @Transactional
    public Map<String, Object> initializeProgress() {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            ark.setCurrentWood(5.0);
            ark.setCurrentMetal(1.0);
            ark.setCurrentSealant(0);
            ark.setEngineCount(0);
            ark.setPropellerCount(0);
            ark.setGeneratorCount(0);
            ark.setHasSail(false);
            ark.setBonusPercentage(BigDecimal.ZERO);
            calculateAndUpdateProgress(ark);
            arkConstructionRepository.save(ark);
            result.put("success", true);
            result.put("message", "方舟进度初始化成功");
            result.put("data", buildStatusResponse(ark));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "初始化失败: " + e.getMessage());
        }
        return result;
    }

    public Map<String, Object> getPlayerResourceLimits(Integer playerId) {
        Map<String, Object> result = new HashMap<>();
        result.put("playerWoodKg", getPlayerMaterialQuantity(playerId, WOOD_MATERIAL_ID));
        result.put("playerMetalKg", getPlayerMaterialQuantity(playerId, METAL_MATERIAL_ID));
        result.put("playerSealantKg", getPlayerMaterialQuantity(playerId, SEALANT_MATERIAL_ID));
        result.put("warehouseWoodKg", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, WOOD_MATERIAL_ID));
        result.put("warehouseMetalKg", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, METAL_MATERIAL_ID));
        result.put("warehouseSealantKg", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, SEALANT_MATERIAL_ID));
        result.put("playerEngine", getPlayerMaterialQuantity(playerId, ENGINE_MATERIAL_ID));
        result.put("playerPropeller", getPlayerMaterialQuantity(playerId, PROPELLER_MATERIAL_ID));
        result.put("playerGenerator", getPlayerMaterialQuantity(playerId, GENERATOR_MATERIAL_ID));
        result.put("warehouseEngine", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, ENGINE_MATERIAL_ID));
        result.put("warehousePropeller", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, PROPELLER_MATERIAL_ID));
        result.put("warehouseGenerator", (int) getWarehouseStockQuantity(ShelterStock.ItemType.material, GENERATOR_MATERIAL_ID));
        result.put("dailyWoodLimitKg", (int) (DAILY_WOOD_LIMIT * KG_PER_TON));
        result.put("dailyMetalLimitKg", (int) (DAILY_METAL_LIMIT * KG_PER_TON));
        result.put("dailySealantLimitKg", DAILY_SEALANT_LIMIT);
        result.put("workEquivalentWood", WORK_EQUIVALENT_WOOD);
        result.put("workEquivalentMetalKg", WORK_EQUIVALENT_METAL_KG);
        result.put("workEquivalentSealant", WORK_EQUIVALENT_SEALANT);
        return result;
    }
}
