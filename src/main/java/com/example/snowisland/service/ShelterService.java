package com.example.snowisland.service;

import com.example.snowisland.entity.GameState;
import com.example.snowisland.entity.Job;
import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.ShelterDailyLabor;
import com.example.snowisland.entity.ShelterLaborDay;
import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.repository.GameStateRepository;
import com.example.snowisland.repository.JobRepository;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.repository.ShelterDailyLaborRepository;
import com.example.snowisland.repository.ShelterLaborDayRepository;
import com.example.snowisland.repository.ShelterStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShelterService {

    private static final Object[][] DEFAULT_STOCK = {
            {ShelterStock.ItemType.material, 2, 45},
            {ShelterStock.ItemType.material, 7, 32},
            {ShelterStock.ItemType.item, 1, 8},
            {ShelterStock.ItemType.item, 2, 4},
            {ShelterStock.ItemType.item, 3, 2},
            {ShelterStock.ItemType.item, 4, 3},
            {ShelterStock.ItemType.item, 5, 1},
            {ShelterStock.ItemType.item, 6, 1},
            {ShelterStock.ItemType.item, 7, 1},
            {ShelterStock.ItemType.item, 8, 5},
            {ShelterStock.ItemType.item, 9, 2},
            {ShelterStock.ItemType.item, 10, 10},
            {ShelterStock.ItemType.item, 11, 12},
            {ShelterStock.ItemType.item, 12, 2},
            {ShelterStock.ItemType.item, 13, 18},
            {ShelterStock.ItemType.item, 14, 3},
            {ShelterStock.ItemType.item, 15, 6},
            {ShelterStock.ItemType.item, 16, 4},
            {ShelterStock.ItemType.item, 17, 1},
            {ShelterStock.ItemType.weapon, 1, 1},
            {ShelterStock.ItemType.weapon, 2, 1},
            {ShelterStock.ItemType.weapon, 3, 2},
            {ShelterStock.ItemType.weapon, 4, 1},
            {ShelterStock.ItemType.weapon, 6, 1},
            {ShelterStock.ItemType.weapon, 7, 1},
            {ShelterStock.ItemType.weapon, 8, 2},
            {ShelterStock.ItemType.weapon, 9, 1},
            {ShelterStock.ItemType.material, 4, 24},
            {ShelterStock.ItemType.material, 3, 35},
            {ShelterStock.ItemType.material, com.example.snowisland.util.ItemCatalog.FOOD_MATERIAL_ID, 127},
            {ShelterStock.ItemType.material, com.example.snowisland.util.ItemCatalog.FUEL_MATERIAL_ID, 40},
    };

    @Autowired
    private ShelterStockRepository shelterStockRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ShelterSupplyService shelterSupplyService;

    @Autowired
    private ShelterDailyLaborRepository shelterDailyLaborRepository;

    @Autowired
    private ShelterLaborDayRepository shelterLaborDayRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private GameStateRepository gameStateRepository;

    public int getCurrentGameDay() {
        GameState state = gameStateRepository.findFirstByOrderByIdAsc();
        if (state == null || state.getCurrentDay() == null) {
            return 1;
        }
        return state.getCurrentDay();
    }

    public int getTotalBuildValue() {
        return shelterDailyLaborRepository.sumVerifiedBuildValue();
    }

    public boolean isDayVerified(int gameDay) {
        return shelterLaborDayRepository.findById(gameDay)
                .map(d -> Boolean.TRUE.equals(d.getVerified()))
                .orElse(false);
    }

    public List<Integer> getLaborPlayerIdsForDay(int gameDay) {
        return shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay).stream()
                .filter(l -> !Boolean.TRUE.equals(l.getEscaped()))
                .map(ShelterDailyLabor::getPlayerId)
                .collect(Collectors.toList());
    }

    public boolean isPlayerLaborerForDay(int playerId, int gameDay) {
        return shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay).stream()
                .anyMatch(l -> playerId == l.getPlayerId() && !Boolean.TRUE.equals(l.getEscaped()));
    }

    @Transactional
    public Map<String, Object> getSummary(Integer viewGameDay) {
        Map<String, Object> out = new LinkedHashMap<>();

        List<ShelterStock> rows = shelterStockRepository.findAllByOrderByItemTypeAscItemIdAsc();
        if (rows.isEmpty()) {
            seedDefaultStock();
            rows = shelterStockRepository.findAllByOrderByItemTypeAscItemIdAsc();
        }

        List<Map<String, Object>> inventory = new ArrayList<>();
        for (ShelterStock row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stockId", row.getId());
            item.put("itemType", row.getItemType().name());
            item.put("itemId", row.getItemId());
            item.put("quantity", row.getQuantity());
            enrichItemInfo(item);
            inventory.add(item);
        }

        shelterSupplyService.ensureReady();

        int currentGameDay = getCurrentGameDay();
        int gameDay = viewGameDay != null && viewGameDay >= 1 ? viewGameDay : currentGameDay;
        Map<Integer, Player> playersById = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getId, p -> p, (a, b) -> a));
        Map<Integer, String> jobNames = jobRepository.findAll().stream()
                .collect(Collectors.toMap(Job::getId, Job::getName, (a, b) -> a));

        List<Map<String, Object>> dailyLabor = buildLaborRowMaps(
                shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay),
                playersById,
                jobNames
        );

        out.put("success", true);
        out.put("currentGameDay", currentGameDay);
        out.put("gameDay", gameDay);
        out.put("dayVerified", isDayVerified(gameDay));
        out.put("currentBuildValue", getTotalBuildValue());
        out.put("inventory", inventory);
        out.put("foodSupply", shelterSupplyService.buildShelterFoodSupply());
        out.put("energyReserve", shelterSupplyService.buildShelterEnergyReserve());
        out.put("laborCandidates", buildLaborCandidates(jobNames));
        out.put("dailyLabor", dailyLabor);
        out.put("buildLogs", buildLogsGroupedByDay(playersById, jobNames));
        out.put("laborDays", buildLaborDayOptions(currentGameDay));
        return out;
    }

    @Transactional
    public Map<String, Object> setLaborRoster(Integer gameDay, List<Integer> playerIds) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (gameDay == null || gameDay < 1) {
            out.put("success", false);
            out.put("message", "无效的游戏天数");
            return out;
        }
        if (playerIds == null) {
            playerIds = Collections.emptyList();
        }
        Set<Integer> seen = new HashSet<>();
        for (Integer playerId : playerIds) {
            if (playerId == null) {
                out.put("success", false);
                out.put("message", "劳工条目缺少 playerId");
                return out;
            }
            if (!seen.add(playerId)) {
                out.put("success", false);
                out.put("message", "重复的玩家: " + playerId);
                return out;
            }
            if (!playerRepository.findById(playerId).isPresent()) {
                out.put("success", false);
                out.put("message", "玩家不存在: " + playerId);
                return out;
            }
        }
        if (isDayVerified(gameDay)) {
            out.put("success", false);
            out.put("message", "第 " + gameDay + " 天已结算，无法再修改劳工名单");
            return out;
        }

        mergeLaborRoster(gameDay, playerIds);
        ensureLaborDayRow(gameDay, false);

        return enrichLaborResponse(out, gameDay);
    }

    @Transactional
    public Map<String, Object> setDailyLabor(Integer gameDay, List<Map<String, Object>> laborers) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (gameDay == null || gameDay < 1) {
            out.put("success", false);
            out.put("message", "无效的游戏天数");
            return out;
        }
        if (laborers == null) {
            laborers = Collections.emptyList();
        }

        Set<Integer> seenPlayerIds = new HashSet<>();
        for (Map<String, Object> row : laborers) {
            Integer playerId = toInt(row.get("playerId"));
            if (playerId == null) {
                out.put("success", false);
                out.put("message", "劳工条目缺少 playerId");
                return out;
            }
            if (!seenPlayerIds.add(playerId)) {
                out.put("success", false);
                out.put("message", "重复的玩家: " + playerId);
                return out;
            }
            if (!playerRepository.findById(playerId).isPresent()) {
                out.put("success", false);
                out.put("message", "玩家不存在: " + playerId);
                return out;
            }
        }

        mergeDailyLabor(gameDay, laborers);
        ensureLaborDayRow(gameDay, isDayVerified(gameDay));

        out.put("message", "劳工名单已保存");
        return enrichLaborResponse(out, gameDay);
    }

    @Transactional
    public Map<String, Object> verifyLaborDay(Integer gameDay) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (gameDay == null || gameDay < 1) {
            out.put("success", false);
            out.put("message", "无效的游戏天数");
            return out;
        }
        List<ShelterDailyLabor> rows = shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay);
        if (rows.isEmpty()) {
            out.put("success", false);
            out.put("message", "第 " + gameDay + " 天尚无劳工记录");
            return out;
        }

        ShelterLaborDay day = shelterLaborDayRepository.findById(gameDay).orElseGet(() -> {
            ShelterLaborDay d = new ShelterLaborDay();
            d.setGameDay(gameDay);
            return d;
        });
        day.setVerified(true);
        day.setVerifiedAt(LocalDateTime.now());
        shelterLaborDayRepository.save(day);

        out.put("message", "第 " + gameDay + " 天建造已结算");
        return enrichLaborResponse(out, gameDay);
    }

    private Map<String, Object> enrichLaborResponse(Map<String, Object> out, int gameDay) {
        Map<Integer, Player> playersById = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getId, p -> p, (a, b) -> a));
        Map<Integer, String> jobNames = jobRepository.findAll().stream()
                .collect(Collectors.toMap(Job::getId, Job::getName, (a, b) -> a));

        out.put("success", true);
        out.put("gameDay", gameDay);
        out.put("currentGameDay", getCurrentGameDay());
        out.put("dayVerified", isDayVerified(gameDay));
        out.put("currentBuildValue", getTotalBuildValue());
        out.put("dailyLabor", buildLaborRowMaps(
                shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay),
                playersById,
                jobNames
        ));
        out.put("buildLogs", buildLogsGroupedByDay(playersById, jobNames));
        out.put("laborDays", buildLaborDayOptions(getCurrentGameDay()));
        return out;
    }

    /**
     * Upsert roster without delete-all-then-insert (avoids UK violations on repeated saves).
     */
    private void mergeLaborRoster(int gameDay, List<Integer> playerIds) {
        List<ShelterDailyLabor> existingRows = shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay);
        Map<Integer, ShelterDailyLabor> existingByPlayer = existingRows.stream()
                .collect(Collectors.toMap(ShelterDailyLabor::getPlayerId, l -> l, (a, b) -> a));
        Set<Integer> targetIds = new LinkedHashSet<>(playerIds);

        for (ShelterDailyLabor row : existingRows) {
            if (!targetIds.contains(row.getPlayerId())) {
                shelterDailyLaborRepository.delete(row);
            }
        }
        entityManager.flush();

        for (Integer playerId : targetIds) {
            ShelterDailyLabor labor = existingByPlayer.get(playerId);
            if (labor == null) {
                labor = new ShelterDailyLabor();
                labor.setGameDay(gameDay);
                labor.setPlayerId(playerId);
                labor.setBuildValue(0);
                labor.setExploited(false);
                labor.setEscaped(false);
            }
            shelterDailyLaborRepository.save(labor);
        }
    }

    private void mergeDailyLabor(int gameDay, List<Map<String, Object>> laborers) {
        List<ShelterDailyLabor> existingRows = shelterDailyLaborRepository.findByGameDayOrderByPlayerIdAsc(gameDay);
        Map<Integer, ShelterDailyLabor> existingByPlayer = existingRows.stream()
                .collect(Collectors.toMap(ShelterDailyLabor::getPlayerId, l -> l, (a, b) -> a));

        Set<Integer> targetIds = new HashSet<>();
        for (Map<String, Object> row : laborers) {
            targetIds.add(toInt(row.get("playerId")));
        }

        for (ShelterDailyLabor row : existingRows) {
            if (!targetIds.contains(row.getPlayerId())) {
                shelterDailyLaborRepository.delete(row);
            }
        }
        entityManager.flush();

        for (Map<String, Object> row : laborers) {
            Integer playerId = toInt(row.get("playerId"));
            ShelterDailyLabor labor = existingByPlayer.get(playerId);
            if (labor == null) {
                labor = new ShelterDailyLabor();
                labor.setGameDay(gameDay);
                labor.setPlayerId(playerId);
            }
            int buildValue = toInt(row.get("buildValue")) != null ? Math.max(0, toInt(row.get("buildValue"))) : 0;
            labor.setBuildValue(buildValue);
            labor.setExploited(toBool(row.get("exploited")));
            labor.setEscaped(toBool(row.get("escaped")));
            shelterDailyLaborRepository.save(labor);
        }
    }

    private void ensureLaborDayRow(int gameDay, boolean verified) {
        if (!shelterLaborDayRepository.findById(gameDay).isPresent()) {
            ShelterLaborDay day = new ShelterLaborDay();
            day.setGameDay(gameDay);
            day.setVerified(verified);
            if (verified) {
                day.setVerifiedAt(LocalDateTime.now());
            }
            shelterLaborDayRepository.save(day);
        }
    }

    private List<Map<String, Object>> buildLaborDayOptions(int currentGameDay) {
        Set<Integer> days = new TreeSet<>();
        for (int d = 1; d <= Math.max(1, currentGameDay); d++) {
            days.add(d);
        }
        shelterDailyLaborRepository.findAllByOrderByGameDayDescPlayerIdAsc().forEach(l -> days.add(l.getGameDay()));
        List<Map<String, Object>> options = new ArrayList<>();
        for (Integer day : days) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("gameDay", day);
            m.put("verified", isDayVerified(day));
            options.add(m);
        }
        options.sort((a, b) -> Integer.compare((Integer) b.get("gameDay"), (Integer) a.get("gameDay")));
        return options;
    }

    private List<Map<String, Object>> buildLaborRowMaps(
            List<ShelterDailyLabor> labors,
            Map<Integer, Player> playersById,
            Map<Integer, String> jobNames
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShelterDailyLabor labor : labors) {
            Player player = playersById.get(labor.getPlayerId());
            if (player == null) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", labor.getId());
            row.put("playerId", labor.getPlayerId());
            row.put("name", player.getName());
            row.put("jobId", player.getJobId());
            row.put("jobName", player.getJobId() != null ? jobNames.getOrDefault(player.getJobId(), "—") : "—");
            row.put("buildValue", labor.getBuildValue() != null ? labor.getBuildValue() : 0);
            row.put("exploited", Boolean.TRUE.equals(labor.getExploited()));
            row.put("escaped", Boolean.TRUE.equals(labor.getEscaped()));
            row.put("isWeak", Boolean.TRUE.equals(player.getIsWeak()));
            row.put("isOverworked", Boolean.TRUE.equals(player.getIsOverworked()));
            row.put("isInjured", Boolean.TRUE.equals(player.getIsInjured()));
            result.add(row);
        }
        return result;
    }

    private List<Map<String, Object>> buildLogsGroupedByDay(
            Map<Integer, Player> playersById,
            Map<Integer, String> jobNames
    ) {
        Map<Integer, List<ShelterDailyLabor>> byDay = new TreeMap<>(Comparator.reverseOrder());
        for (ShelterDailyLabor labor : shelterDailyLaborRepository.findAllByOrderByGameDayDescPlayerIdAsc()) {
            byDay.computeIfAbsent(labor.getGameDay(), k -> new ArrayList<>()).add(labor);
        }

        List<Map<String, Object>> logs = new ArrayList<>();
        for (Map.Entry<Integer, List<ShelterDailyLabor>> entry : byDay.entrySet()) {
            boolean verified = isDayVerified(entry.getKey());
            int dayTotal = 0;
            List<Map<String, Object>> workers = new ArrayList<>();
            for (ShelterDailyLabor labor : entry.getValue()) {
                Player player = playersById.get(labor.getPlayerId());
                if (player == null) {
                    continue;
                }
                int value = labor.getBuildValue() != null ? labor.getBuildValue() : 0;
                if (verified) {
                    dayTotal += value;
                }
                Map<String, Object> w = new LinkedHashMap<>();
                w.put("playerId", labor.getPlayerId());
                w.put("name", player.getName());
                w.put("jobName", player.getJobId() != null ? jobNames.getOrDefault(player.getJobId(), "—") : "—");
                if (verified) {
                    w.put("value", value);
                }
                w.put("isProfessional", player.getJobId() != null);
                w.put("isOppressed", Boolean.TRUE.equals(labor.getExploited()));
                w.put("escaped", Boolean.TRUE.equals(labor.getEscaped()));
                workers.add(w);
            }
            if (workers.isEmpty()) {
                continue;
            }
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("day", entry.getKey());
            log.put("verified", verified);
            log.put("dayTotal", verified ? dayTotal : null);
            log.put("workers", workers);
            logs.add(log);
        }
        return logs;
    }

    private List<Map<String, Object>> buildLaborCandidates(Map<Integer, String> jobNames) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Player player : playerRepository.findAll()) {
            if (player.getFaction() == Player.Faction.统治者) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", player.getId());
            row.put("name", player.getName());
            row.put("faction", player.getFaction() != null ? player.getFaction().name() : null);
            row.put("jobId", player.getJobId());
            row.put("jobName", player.getJobId() != null ? jobNames.getOrDefault(player.getJobId(), "—") : "—");
            row.put("isWeak", Boolean.TRUE.equals(player.getIsWeak()));
            row.put("isOverworked", Boolean.TRUE.equals(player.getIsOverworked()));
            row.put("isInjured", Boolean.TRUE.equals(player.getIsInjured()));
            rows.add(row);
        }
        rows.sort(Comparator.comparing(m -> String.valueOf(m.get("name"))));
        return rows;
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

    private static boolean toBool(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        String s = String.valueOf(o).trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }

    private void enrichItemInfo(Map<String, Object> item) {
        String itemType = (String) item.get("itemType");
        Integer itemId = (Integer) item.get("itemId");
        String tableName;
        switch (itemType) {
            case "weapon": tableName = "weapon"; break;
            case "ammo": tableName = "ammo"; break;
            case "material": tableName = "material"; break;
            default: tableName = "item"; break;
        }
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT name, unit, remark FROM " + tableName + " WHERE id = ?1");
            query.setParameter(1, itemId);
            List<Object[]> results = query.getResultList();
            if (!results.isEmpty()) {
                item.put("name", results.get(0)[0]);
                item.put("unit", results.get(0)[1]);
                item.put("description", results.get(0)[2]);
            } else {
                item.put("name", "未知物品");
                item.put("unit", "");
                item.put("description", "");
            }
        } catch (Exception e) {
            item.put("name", "未知物品");
            item.put("unit", "");
            item.put("description", "");
        }
    }

    protected void seedDefaultStock() {
        for (Object[] row : DEFAULT_STOCK) {
            ShelterStock s = new ShelterStock();
            s.setItemType((ShelterStock.ItemType) row[0]);
            s.setItemId((Integer) row[1]);
            s.setQuantity((Integer) row[2]);
            shelterStockRepository.save(s);
        }
    }

    public Map<String, Object> getShelterItemCatalog() {
        Map<String, Object> out = new LinkedHashMap<>();
        @SuppressWarnings("unchecked")
        List<Object[]> raw = entityManager.createNativeQuery(
                "SELECT 'item' as type, id, name, unit FROM item "
                        + "UNION ALL SELECT 'weapon', id, name, unit FROM weapon "
                        + "UNION ALL SELECT 'ammo', id, name, unit FROM ammo "
                        + "UNION ALL SELECT 'material', id, name, unit FROM material "
                        + "ORDER BY type, id"
        ).getResultList();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Object[] row : raw) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("itemType", row[0]);
            item.put("itemId", ((Number) row[1]).intValue());
            item.put("name", row[2]);
            item.put("unit", row[3]);
            items.add(item);
        }
        out.put("success", true);
        out.put("items", items);
        return out;
    }

    @Transactional
    public Map<String, Object> upsertShelterStock(String itemType, Integer itemId, Integer quantity) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (itemType == null || itemType.isBlank() || itemId == null) {
            out.put("success", false);
            out.put("message", "参数不完整");
            return out;
        }
        if (quantity == null || quantity < 0) {
            out.put("success", false);
            out.put("message", "数量不能为负数");
            return out;
        }
        ShelterStock.ItemType type;
        try {
            type = ShelterStock.ItemType.valueOf(itemType.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            out.put("success", false);
            out.put("message", "无效的物品类型");
            return out;
        }
        Optional<ShelterStock> opt = shelterStockRepository.findByItemTypeAndItemId(type, itemId);
        if (quantity == 0) {
            opt.ifPresent(shelterStockRepository::delete);
            out.put("success", true);
            out.put("message", "已从避难所库存移除");
            return out;
        }
        ShelterStock row = opt.orElseGet(() -> {
            ShelterStock s = new ShelterStock();
            s.setItemType(type);
            s.setItemId(itemId);
            return s;
        });
        row.setQuantity(quantity);
        shelterStockRepository.save(row);
        out.put("success", true);
        out.put("message", "库存已更新");
        return out;
    }

    @Transactional
    public Map<String, Object> removeShelterStock(String itemType, Integer itemId) {
        Map<String, Object> out = new LinkedHashMap<>();
        if (itemType == null || itemType.isBlank() || itemId == null) {
            out.put("success", false);
            out.put("message", "参数不完整");
            return out;
        }
        ShelterStock.ItemType type;
        try {
            type = ShelterStock.ItemType.valueOf(itemType.trim().toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            out.put("success", false);
            out.put("message", "无效的物品类型");
            return out;
        }
        shelterStockRepository.findByItemTypeAndItemId(type, itemId)
                .ifPresent(shelterStockRepository::delete);
        out.put("success", true);
        out.put("message", "已从避难所库存移除");
        return out;
    }
}
