package com.example.snowisland.service;

import com.example.snowisland.entity.EventPack;
import com.example.snowisland.entity.IslandEvent;
import com.example.snowisland.repository.EventPackRepository;
import com.example.snowisland.repository.IslandEventRepository;
import com.example.snowisland.service.ExplorationDataInitService.ExplorationEventData;
import com.example.snowisland.util.SafeText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class EventPackService {

    @Autowired
    private EventPackRepository eventPackRepository;

    @Autowired
    private IslandEventRepository islandEventRepository;

    @Autowired
    private ExplorationDataInitService explorationDataInitService;

    public Map<String, Object> listPacks() {
        Map<String, Object> result = new HashMap<>();
        try {
            explorationDataInitService.ensureSeedPacks();
            List<EventPack> packs = eventPackRepository.findAllByOrderBySortOrderAscIdAsc();
            List<Map<String, Object>> list = new ArrayList<>();
            for (EventPack pack : packs) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", pack.getId());
                map.put("name", pack.getName());
                map.put("enabled", Boolean.TRUE.equals(pack.getEnabled()));
                map.put("sortOrder", pack.getSortOrder());
                map.put("parentId", pack.getParentId());
                map.put("eventCount", islandEventRepository.countByPackId(pack.getId()));
                list.add(map);
            }
            result.put("success", true);
            result.put("packs", list);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取卡包列表失败: " + e.getMessage());
        }
        return result;
    }

    @Transactional
    public Map<String, Object> setPackEnabled(Integer packId, Boolean enabled) {
        Map<String, Object> result = new HashMap<>();
        if (packId == null) {
            result.put("success", false);
            result.put("message", "卡包ID不能为空");
            return result;
        }
        Optional<EventPack> opt = eventPackRepository.findById(packId);
        if (!opt.isPresent()) {
            result.put("success", false);
            result.put("message", "卡包不存在");
            return result;
        }
        EventPack pack = opt.get();
        pack.setEnabled(Boolean.TRUE.equals(enabled));
        eventPackRepository.save(pack);
        result.put("success", true);
        result.put("message", pack.getEnabled() ? "已加入本局" : "已移出本局");
        result.put("pack", toPackMap(pack));
        return result;
    }

    public Map<String, Object> previewImport(String rawText) {
        Map<String, Object> result = new HashMap<>();
        String sizeError = rawImportSizeError(rawText);
        if (sizeError != null) {
            result.put("success", false);
            result.put("message", sizeError);
            result.put("events", Collections.emptyList());
            return result;
        }
        List<ExplorationEventData> parsed = explorationDataInitService.parseBraceFormat(rawText);
        if (parsed.isEmpty()) {
            result.put("success", false);
            result.put("message", "未能解析到任何事件，请检查格式（{难度}{正文}，正文含名称/地点描述/可获得物资/历史碎片，不能包含 }）");
            result.put("events", Collections.emptyList());
            return result;
        }
        if (parsed.size() > SafeText.MAX_IMPORT_EVENTS) {
            result.put("success", false);
            result.put("message", "单次最多导入 " + SafeText.MAX_IMPORT_EVENTS + " 个事件");
            result.put("events", Collections.emptyList());
            return result;
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Set<String> batchNames = new HashSet<>();
        List<Map<String, Object>> events = new ArrayList<>();
        for (ExplorationEventData data : parsed) {
            Map<String, Object> row = toPreviewMap(data);
            events.add(row);
            String eventLabel = (data.name != null && !data.name.trim().isEmpty())
                    ? data.name
                    : (data.eventNumber != null ? "未命名（编号 " + data.eventNumber + "）" : "未命名");
            if (data.unmatchedRewards != null && !data.unmatchedRewards.isEmpty()) {
                errors.add("事件「" + eventLabel + "」无法识别物资：" + String.join("、", data.unmatchedRewards));
            }
            if (data.qtyFallbackWarnings != null && !data.qtyFallbackWarnings.isEmpty()) {
                for (String w : data.qtyFallbackWarnings) {
                    warnings.add("事件「" + eventLabel + "」" + w);
                }
            }
            if (data.name == null || data.name.trim().isEmpty()) {
                errors.add(data.eventNumber != null
                        ? "存在未命名事件（编号 " + data.eventNumber + "）"
                        : "存在未命名事件");
                continue;
            }
            if (!IslandExplorationService.isValidDifficulty(data.difficulty)) {
                errors.add("事件「" + data.name + "」难度无效（必须为 0-20）");
            }
            if (!batchNames.add(data.name)) {
                errors.add("导入文本中事件名称重复：" + data.name);
            } else if (islandEventRepository.findByName(data.name).isPresent()) {
                errors.add("事件名称已存在：" + data.name);
            }
        }

        result.put("events", events);
        result.put("count", events.size());
        result.put("warnings", warnings);
        if (!errors.isEmpty()) {
            result.put("success", false);
            result.put("message", String.join("；", errors));
            return result;
        }
        result.put("success", true);
        if (!warnings.isEmpty()) {
            result.put("message", "解析成功，共 " + events.size() + " 个事件。" + String.join("；", warnings));
        } else {
            result.put("message", "解析成功，共 " + events.size() + " 个事件");
        }
        return result;
    }

    @Transactional
    public Map<String, Object> importPack(String packName, String rawText) {
        Map<String, Object> result = new HashMap<>();
        String nameError = SafeText.packNameError(packName);
        if (nameError != null) {
            result.put("success", false);
            result.put("message", nameError);
            return result;
        }
        packName = SafeText.validatePackName(packName);
        if (packName == null) {
            result.put("success", false);
            result.put("message", "卡包名称无效");
            return result;
        }
        if (eventPackRepository.findByName(packName).isPresent()) {
            result.put("success", false);
            result.put("message", "卡包名称已存在：" + packName);
            return result;
        }

        Map<String, Object> preview = previewImport(rawText);
        if (!Boolean.TRUE.equals(preview.get("success"))) {
            return preview;
        }

        rawText = SafeText.cleanLimit(rawText, SafeText.RAW_IMPORT_MAX);
        List<ExplorationEventData> parsed = explorationDataInitService.parseBraceFormat(rawText);
        int nextOrder = eventPackRepository.findAll().stream()
                .map(EventPack::getSortOrder)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0) + 1;

        EventPack pack = new EventPack();
        pack.setName(packName);
        pack.setEnabled(true);
        pack.setSortOrder(nextOrder);
        pack.setParentId(null);
        pack = eventPackRepository.save(pack);

        int imported = 0;
        for (ExplorationEventData data : parsed) {
            IslandEvent saved = explorationDataInitService.persistParsedEvent(data, pack);
            if (saved != null) {
                imported++;
            }
        }

        result.put("success", true);
        result.put("message", "已导入卡包「" + packName + "」，共 " + imported + " 个事件");
        result.put("pack", toPackMap(pack));
        result.put("importedCount", imported);
        return result;
    }

    private Map<String, Object> toPackMap(EventPack pack) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", pack.getId());
        map.put("name", pack.getName());
        map.put("enabled", Boolean.TRUE.equals(pack.getEnabled()));
        map.put("sortOrder", pack.getSortOrder());
        map.put("parentId", pack.getParentId());
        map.put("eventCount", islandEventRepository.countByPackId(pack.getId()));
        return map;
    }

    private Map<String, Object> toPreviewMap(ExplorationEventData data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sourceNumber", data.eventNumber);
        map.put("name", data.name);
        map.put("difficulty", data.difficulty);
        map.put("eventDifficulty", data.difficulty);
        String loc = data.locationDesc != null ? data.locationDesc : "";
        map.put("locationDesc", loc);
        map.put("locationDescSnippet", loc.length() > 80 ? loc.substring(0, 80) + "…" : loc);
        String lore = data.loreFragment != null ? data.loreFragment : "";
        map.put("loreFragment", lore);
        map.put("loreSnippet", lore.length() > 80 ? lore.substring(0, 80) + "…" : lore);
        map.put("rewardsText", data.rewardsText != null ? data.rewardsText : "");
        List<String> rewardLabels = new ArrayList<>();
        if (data.rewards != null) {
            for (ExplorationDataInitService.RewardData reward : data.rewards) {
                if (reward.displayName != null && !reward.displayName.isEmpty()) {
                    rewardLabels.add(reward.displayName);
                }
            }
        }
        map.put("rewardLabels", rewardLabels);
        map.put("unmatchedRewards", data.unmatchedRewards != null
                ? new ArrayList<>(data.unmatchedRewards) : Collections.emptyList());
        map.put("qtyFallbackWarnings", data.qtyFallbackWarnings != null
                ? new ArrayList<>(data.qtyFallbackWarnings) : Collections.emptyList());
        map.put("rewardCount", data.rewards != null ? data.rewards.size() : 0);
        map.put("isSpecial", data.isSpecial || data.difficulty == 20);
        return map;
    }

    private static String rawImportSizeError(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "导入文本不能为空";
        }
        if (rawText.length() > SafeText.RAW_IMPORT_MAX) {
            return "导入文本过长（上限 " + SafeText.RAW_IMPORT_MAX + " 字符）";
        }
        if (rawText.indexOf('\0') >= 0) {
            return "导入文本包含非法控制字符";
        }
        return null;
    }
}
