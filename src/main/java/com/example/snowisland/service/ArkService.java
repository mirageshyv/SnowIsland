package com.example.snowisland.service;

import com.example.snowisland.entity.ArkConstruction;
import com.example.snowisland.repository.ArkConstructionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class ArkService {

    @Autowired
    private ArkConstructionRepository arkConstructionRepository;

    private static final int TARGET_WOOD = 250;
    private static final int TARGET_METAL = 100;
    private static final int TARGET_SEALANT = 100;

    public ArkConstruction getOrCreate() {
        return arkConstructionRepository.findById(ArkConstruction.SINGLETON_ID)
                .orElseGet(() -> {
                    ArkConstruction construction = new ArkConstruction();
                    construction.setId(ArkConstruction.SINGLETON_ID);
                    construction.setCurrentWood(10);
                    construction.setCurrentMetal(20);
                    return arkConstructionRepository.save(construction);
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
        result.put("hasSail", ark.getHasSail());
        result.put("targetWood", TARGET_WOOD);
        result.put("targetMetal", TARGET_METAL);
        result.put("targetSealant", TARGET_SEALANT);

        return result;
    }

    @Transactional
    public Map<String, Object> investResources(Integer wood, Integer metal, Integer sealant) {
        Map<String, Object> result = new HashMap<>();
        try {
            ArkConstruction ark = getOrCreate();
            if (wood != null && wood > 0) {
                ark.setCurrentWood(ark.getCurrentWood() + wood);
            }
            if (metal != null && metal > 0) {
                ark.setCurrentMetal(ark.getCurrentMetal() + metal);
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
            ark.setCurrentWood(10);
            ark.setCurrentMetal(20);
            ark.setCurrentSealant(0);
            ark.setEngineCount(0);
            ark.setPropellerCount(0);
            ark.setGeneratorCount(0);
            ark.setCurrentCargoCapacity(0);
            ark.setCompletionPercentage(BigDecimal.ZERO);
            ark.setHasSail(false);
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

    private void calculateAndUpdateProgress(ArkConstruction ark) {
        BigDecimal woodProgress = calculateResourceProgress(ark.getCurrentWood(), TARGET_WOOD, 25.0);
        BigDecimal metalProgress = calculateResourceProgress(ark.getCurrentMetal(), TARGET_METAL, 25.0);
        BigDecimal sealantProgress = calculateResourceProgress(ark.getCurrentSealant(), TARGET_SEALANT, 25.0);
        BigDecimal engineProgress = ark.getEngineCount() >= 1 ? new BigDecimal("10.0") : BigDecimal.ZERO;
        BigDecimal propellerProgress = ark.getPropellerCount() == 2 ? new BigDecimal("10.0") : (ark.getPropellerCount() > 0 ? new BigDecimal("5.0") : BigDecimal.ZERO);
        BigDecimal generatorProgress = ark.getGeneratorCount() >= 1 ? new BigDecimal("5.0") : BigDecimal.ZERO;

        BigDecimal totalProgress = woodProgress.add(metalProgress).add(sealantProgress)
                .add(engineProgress).add(propellerProgress).add(generatorProgress);

        if (totalProgress.compareTo(new BigDecimal("100.0")) > 0) {
            totalProgress = new BigDecimal("100.0");
        }

        ark.setCompletionPercentage(totalProgress);

        int theoreticalCapacity = 50;
        int extraWood = Math.max(0, ark.getCurrentWood() - TARGET_WOOD);
        int extraMetal = Math.max(0, ark.getCurrentMetal() - TARGET_METAL);
        theoreticalCapacity += (extraWood / 10) * 2;
        theoreticalCapacity += (extraMetal / 5) * 2;

        int shortageCount = 0;
        if (ark.getCurrentWood() < TARGET_WOOD) shortageCount++;
        if (ark.getCurrentMetal() < TARGET_METAL) shortageCount++;
        if (ark.getCurrentSealant() < TARGET_SEALANT) shortageCount++;
        int actualCapacity = Math.max(0, theoreticalCapacity - shortageCount * 3);
        ark.setCurrentCargoCapacity(actualCapacity);
    }

    private BigDecimal calculateResourceProgress(int current, int target, double maxPercent) {
        if (target <= 0) return BigDecimal.ZERO;
        double ratio = Math.min(1.0, (double) current / target);
        return new BigDecimal(ratio * maxPercent).setScale(2, RoundingMode.HALF_UP);
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
        data.put("hasSail", ark.getHasSail());
        data.put("targetWood", TARGET_WOOD);
        data.put("targetMetal", TARGET_METAL);
        data.put("targetSealant", TARGET_SEALANT);
        return data;
    }
}
