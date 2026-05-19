package com.example.snowisland.controller;

import com.example.snowisland.service.ShelterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 统治者避难所：建造值、避难所物资库存、避难所公共食物/能量（非玩家个人库存）。
 */
@RestController
@RequestMapping("/api/shelter")
@CrossOrigin(origins = "*")
public class ShelterController {

    @Autowired
    private ShelterService shelterService;

    @GetMapping
    public Map<String, Object> getSummary(@RequestParam(required = false) Integer gameDay) {
        return shelterService.getSummary(gameDay);
    }

    /** 统治者：提交当日劳工名单；可标记压榨（最多3人），不含建造值/逃役 */
    @PutMapping("/labor/roster")
    public Map<String, Object> setLaborRoster(@RequestBody Map<String, Object> body) {
        Integer gameDay = toInt(body.get("gameDay"));
        if (gameDay == null) {
            gameDay = shelterService.getCurrentGameDay();
        }
        List<Map<String, Object>> laborers = extractLaborerRows(body);
        if (!laborers.isEmpty()) {
            return shelterService.setLaborRosterWithExploit(gameDay, laborers);
        }
        return shelterService.setLaborRoster(gameDay, extractPlayerIds(body.get("playerIds")));
    }

    /** DM：完整编辑指定日劳工（建造值、压榨、逃役） */
    @PutMapping("/labor")
    public Map<String, Object> setDailyLabor(@RequestBody Map<String, Object> body) {
        Integer gameDay = toInt(body.get("gameDay"));
        if (gameDay == null) {
            gameDay = shelterService.getCurrentGameDay();
        }
        List<Map<String, Object>> laborers = new ArrayList<>();
        Object raw = body.get("laborers");
        if (raw instanceof List) {
            for (Object item : (List<?>) raw) {
                if (item instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> row = (Map<String, Object>) item;
                    laborers.add(row);
                }
            }
        }
        return shelterService.setDailyLabor(gameDay, laborers);
    }

    @GetMapping("/catalog")
    public Map<String, Object> getItemCatalog() {
        return shelterService.getShelterItemCatalog();
    }

    @PutMapping("/stock")
    public Map<String, Object> upsertStock(@RequestBody Map<String, Object> body) {
        String itemType = body.get("itemType") != null ? String.valueOf(body.get("itemType")) : null;
        return shelterService.upsertShelterStock(itemType, toInt(body.get("itemId")), toInt(body.get("quantity")));
    }

    @DeleteMapping("/stock")
    public Map<String, Object> deleteStock(
            @RequestParam String itemType,
            @RequestParam Integer itemId) {
        return shelterService.removeShelterStock(itemType, itemId);
    }

    /** DM：结算前预览建造值与状态效果 */
    @GetMapping("/labor/preview")
    public Map<String, Object> previewLaborSettlement(@RequestParam(required = false) Integer gameDay) {
        if (gameDay == null) {
            gameDay = shelterService.getCurrentGameDay();
        }
        return shelterService.previewLaborSettlement(gameDay);
    }

    /** DM：结算指定日建造日志 */
    @PostMapping("/labor/verify")
    public Map<String, Object> verifyLaborDay(@RequestBody Map<String, Object> body) {
        Integer gameDay = toInt(body.get("gameDay"));
        if (gameDay == null) {
            gameDay = shelterService.getCurrentGameDay();
        }
        return shelterService.verifyLaborDay(gameDay);
    }

    private static List<Map<String, Object>> extractLaborerRows(Map<String, Object> body) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Object raw = body.get("laborers");
        if (!(raw instanceof List)) {
            return rows;
        }
        for (Object item : (List<?>) raw) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> row = (Map<String, Object>) item;
                rows.add(row);
            }
        }
        return rows;
    }

    private static List<Integer> extractPlayerIds(Object raw) {
        List<Integer> ids = new ArrayList<>();
        if (!(raw instanceof List)) {
            return ids;
        }
        for (Object item : (List<?>) raw) {
            Integer id = toInt(item);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static Integer toInt(Object o) {
        if (o == null || "".equals(o)) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
