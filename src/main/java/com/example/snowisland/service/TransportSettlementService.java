package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.ShelterStock;
import com.example.snowisland.entity.WarehouseConfig;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.repository.ShelterStockRepository;
import com.example.snowisland.repository.WarehouseConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Parses transport action notes, validates stock/keys, previews settlement, and applies inventory on publish.
 */
@Service
public class TransportSettlementService {

    public static final String PENDING_MARKER = "\n\n【搬运待发布】\n";
    public static final String SETTLEMENT_HEADER = "【搬运结算】";
    public static final String SHELTER_KEY = "shelter";

    private static final Set<String> VALID_WAREHOUSE_KEYS = new HashSet<>(Arrays.asList(
            "general", "fuel", "armory", "dock", "rebel", "ark", SHELTER_KEY
    ));

    @Autowired private EntityManager entityManager;
    @Autowired private WarehouseConfigRepository warehouseConfigRepository;
    @Autowired private WarehouseService warehouseService;
    @Autowired private ShelterStockRepository shelterStockRepository;
    @Autowired private PlayerRepository playerRepository;

    public static class TransportItem {
        public String itemType;
        public int itemId;
        public int requestedQty;
        public double weightPerUnit;
        public int actualQty;
    }

    public static class TransportPlan {
        public String mode;
        public String sourceWarehouse;
        public String destWarehouse;
        public Integer targetPlayerId;
        /** free = 不影响行动额度；action = 跳过/占用行动额度（默认） */
        public String tier = "action";
        /** 小镇 / 海岛；未指定时由仓库推断 */
        public String area;
        /** 个人→仓库：提交行动时已从玩家背包扣除 */
        public boolean playerDeducted;
        public List<TransportItem> items = new ArrayList<>();
    }

    public TransportPlan parseNotes(String notes, Integer actionTargetId) {
        TransportPlan plan = new TransportPlan();
        if (notes == null) return plan;
        for (String line : notes.split("\n")) {
            line = line.trim();
            int closeIdx = line.indexOf(']');
            if (closeIdx < 0) continue;
            if (line.startsWith("[mode:")) {
                plan.mode = line.substring(6, closeIdx);
            } else if (line.startsWith("[source:")) {
                plan.sourceWarehouse = line.substring(8, closeIdx);
            } else if (line.startsWith("[dest:")) {
                plan.destWarehouse = line.substring(6, closeIdx);
            } else if (line.startsWith("[target:")) {
                try {
                    plan.targetPlayerId = Integer.parseInt(line.substring(8, closeIdx));
                } catch (NumberFormatException ignored) { }
            } else if (line.startsWith("[player_deducted:")) {
                plan.playerDeducted = line.contains("1");
            } else if (line.startsWith("[tier:")) {
                plan.tier = line.substring(6, closeIdx);
            } else if (line.startsWith("[area:")) {
                plan.area = line.substring(6, closeIdx);
            } else if (line.startsWith("[item:")) {
                String itemStr = line.substring(6, closeIdx);
                String[] parts = itemStr.split("\\|");
                if (parts.length >= 4) {
                    try {
                        TransportItem item = new TransportItem();
                        item.itemType = parts[0];
                        item.itemId = Integer.parseInt(parts[1]);
                        item.requestedQty = Integer.parseInt(parts[2]);
                        item.weightPerUnit = Double.parseDouble(parts[3]);
                        item.actualQty = 0;
                        plan.items.add(item);
                    } catch (NumberFormatException ignored) { }
                }
            }
        }
        if (plan.targetPlayerId == null && actionTargetId != null) {
            plan.targetPlayerId = actionTargetId;
        }
        normalizeShelterAliases(plan);
        return plan;
    }

    /** 旧版避难所专属模式视为普通仓库搬运（库存仍写入 shelter_stock）。 */
    private void normalizeShelterAliases(TransportPlan plan) {
        if (plan == null || plan.mode == null) return;
        switch (plan.mode) {
            case "warehouse_to_shelter":
                plan.mode = "warehouse_to_warehouse";
                if (plan.destWarehouse == null || plan.destWarehouse.isEmpty()) {
                    plan.destWarehouse = SHELTER_KEY;
                }
                break;
            case "shelter_to_warehouse":
                plan.mode = "warehouse_to_warehouse";
                if (plan.sourceWarehouse == null || plan.sourceWarehouse.isEmpty()) {
                    plan.sourceWarehouse = SHELTER_KEY;
                }
                break;
            case "shelter_to_player":
                plan.mode = "warehouse_to_player";
                if (plan.sourceWarehouse == null || plan.sourceWarehouse.isEmpty()) {
                    plan.sourceWarehouse = SHELTER_KEY;
                }
                break;
            case "player_to_shelter":
                plan.mode = "player_to_warehouse";
                if (plan.destWarehouse == null || plan.destWarehouse.isEmpty()) {
                    plan.destWarehouse = SHELTER_KEY;
                }
                break;
            default:
                break;
        }
    }

    /** 将结构化搬运备注转为中文摘要（供日志与展示） */
    public String formatNotesChinese(String notes) {
        TransportPlan plan = parseNotes(notes, null);
        if (plan.mode == null || plan.mode.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("模式：").append(modeLabel(plan.mode));
        if (plan.sourceWarehouse != null && !plan.sourceWarehouse.isEmpty()) {
            sb.append("；源：").append(resolveWarehouseName(plan.sourceWarehouse));
        }
        if (plan.destWarehouse != null && !plan.destWarehouse.isEmpty()) {
            sb.append("；目标：").append(resolveWarehouseName(plan.destWarehouse));
        }
        if (plan.targetPlayerId != null) {
            sb.append("；目标玩家ID：").append(plan.targetPlayerId);
        }
        for (TransportItem item : plan.items) {
            if (item.requestedQty <= 0 && item.actualQty <= 0) continue;
            int qty = item.actualQty > 0 ? item.actualQty : item.requestedQty;
            sb.append("；").append(resolveItemName(item.itemType, item.itemId))
                    .append("×").append(qty);
        }
        if (plan.playerDeducted) {
            sb.append("；已预扣个人物资");
        }
        return sb.toString();
    }

    public List<String> validateKeys(Integer playerId, TransportPlan plan) {
        List<String> errors = new ArrayList<>();
        if (plan.mode == null || plan.mode.isEmpty()) {
            errors.add("未指定搬运模式");
            return errors;
        }
        normalizeShelterAliases(plan);
        switch (plan.mode) {
            case "warehouse_to_warehouse":
                requireKey(playerId, plan.sourceWarehouse, "源仓库", errors);
                requireKey(playerId, plan.destWarehouse, "目标仓库", errors);
                if (plan.sourceWarehouse != null && plan.sourceWarehouse.equals(plan.destWarehouse)) {
                    errors.add("源仓库与目标仓库不能相同");
                }
                break;
            case "warehouse_to_player":
                requireKey(playerId, plan.sourceWarehouse, "源仓库", errors);
                break;
            case "player_to_warehouse":
                requireKey(playerId, plan.destWarehouse, "目标仓库", errors);
                break;
            default:
                errors.add("未知的搬运模式: " + plan.mode);
        }
        return errors;
    }

    private boolean isShelterWarehouse(String warehouseKey) {
        return SHELTER_KEY.equals(warehouseKey);
    }

    private void requireKey(Integer playerId, String warehouseKey, String label, List<String> errors) {
        if (warehouseKey == null || warehouseKey.isEmpty()) {
            errors.add(label + "未选择");
            return;
        }
        if (!warehouseService.playerHasWarehouseKey(playerId, warehouseKey)) {
            errors.add("没有" + label + "「" + resolveWarehouseName(warehouseKey) + "」的钥匙");
        }
    }

    public List<String> validatePlanStructure(TransportPlan plan) {
        List<String> errors = new ArrayList<>();
        if (plan.mode == null || plan.mode.isEmpty()) {
            errors.add("搬运数据格式错误：缺少模式");
            return errors;
        }
        normalizeShelterAliases(plan);
        if (plan.items.isEmpty()) {
            errors.add("未选择任何搬运物资");
        }
        switch (plan.mode) {
            case "warehouse_to_warehouse":
                if (plan.sourceWarehouse == null || plan.destWarehouse == null) {
                    errors.add("仓库→仓库需要选择源仓库和目标仓库");
                }
                break;
            case "warehouse_to_player":
                if (plan.sourceWarehouse == null) errors.add("仓库→个人需要选择源仓库");
                break;
            case "player_to_warehouse":
                if (plan.destWarehouse == null) errors.add("个人→仓库需要选择目标仓库");
                break;
            default:
                errors.add("未知的搬运模式");
        }
        return errors;
    }

    /**
     * 规则书137（免费玩家↔仓库已统一为 50kg）：
     * 玩家↔仓库：免费50/行动300（小镇与海岛相同）
     * 仓库↔仓库：小镇免费100/行动500；海岛免费50/行动300
     * 避难所相关按海岛仓库额度；装卸工职业搬运量×2
     */
    public int resolveMaxWeight(TransportPlan plan, Integer actingPlayerId) {
        if (plan != null) {
            normalizeShelterAliases(plan);
        }
        boolean free = plan != null && "free".equalsIgnoreCase(plan.tier);
        String area = resolveTransportArea(plan);
        boolean island = area != null && (area.contains("海岛") || "特殊".equals(area));
        boolean warehouseLink = plan != null && "warehouse_to_warehouse".equals(plan.mode);

        int max;
        if (warehouseLink) {
            max = free ? (island ? 50 : 100) : (island ? 300 : 500);
        } else {
            max = free ? 50 : 300;
        }
        if (actingPlayerId != null && isStevedore(actingPlayerId)) {
            max *= 2;
        }
        return max;
    }

    private String resolveTransportArea(TransportPlan plan) {
        if (plan == null) return "小镇";
        if (plan.area != null && !plan.area.trim().isEmpty()) {
            return plan.area.trim();
        }
        String fromSource = warehouseArea(plan.sourceWarehouse);
        String fromDest = warehouseArea(plan.destWarehouse);
        if ("海岛".equals(fromSource) || "海岛".equals(fromDest)) return "海岛";
        if (fromSource != null) return fromSource;
        if (fromDest != null) return fromDest;
        return "小镇";
    }

    private String warehouseArea(String key) {
        if (key == null || key.isEmpty()) return null;
        if (SHELTER_KEY.equals(key)) return "海岛";
        switch (key) {
            case "general":
                return "海岛"; // 矿场仓库
            case "fuel":
            case "armory":
            case "dock":
            case "rebel":
            case "ark":
                return "小镇";
            default:
                return "小镇";
        }
    }

    private boolean isStevedore(Integer playerId) {
        try {
            Optional<Player> opt = playerRepository.findById(playerId);
            if (!opt.isPresent() || opt.get().getJobId() == null) return false;
            Query q = entityManager.createNativeQuery("SELECT name FROM job WHERE id = ?1");
            q.setParameter(1, opt.get().getJobId());
            List<?> rows = q.getResultList();
            if (rows.isEmpty()) return false;
            String name = String.valueOf(rows.get(0));
            return name != null && name.contains("装卸");
        } catch (Exception e) {
            return false;
        }
    }

    /** Compute actualQty; returns error messages if any line cannot be satisfied. */
    public List<String> computeTransfer(TransportPlan plan, Integer actingPlayerId) {
        normalizeShelterAliases(plan);
        List<String> errors = new ArrayList<>();
        errors.addAll(validatePlanStructure(plan));
        if (!errors.isEmpty()) return errors;

        int maxWeight = resolveMaxWeight(plan, actingPlayerId);
        int totalMoved = 0;

        for (TransportItem item : plan.items) {
            if (item.requestedQty <= 0) continue;

            int available = getSourceQuantity(plan, actingPlayerId, item.itemType, item.itemId);
            if (!plan.playerDeducted && available <= 0) {
                errors.add(resolveItemName(item.itemType, item.itemId) + "：源侧无库存");
                continue;
            }

            int maxQtyByWeight = (int) Math.floor((maxWeight - totalMoved) / Math.max(item.weightPerUnit, 0.1));
            int cap = plan.playerDeducted ? item.requestedQty : Math.min(item.requestedQty, available);
            int actualQty = Math.min(cap, maxQtyByWeight);
            if (actualQty <= 0) {
                if (maxQtyByWeight <= 0) {
                    errors.add("总搬运重量已达上限（" + maxWeight + "千克）");
                } else {
                    errors.add(resolveItemName(item.itemType, item.itemId) + "：申请数量超过可用库存或重量上限");
                }
                continue;
            }

            item.actualQty = actualQty;
            totalMoved += (int) (actualQty * item.weightPerUnit);
        }

        boolean anyMoved = plan.items.stream().anyMatch(i -> i.actualQty > 0);
        if (!anyMoved && errors.isEmpty()) {
            errors.add("没有可搬运的物资");
        }
        return errors;
    }

    private int getSourceQuantity(TransportPlan plan, Integer actingPlayerId, String itemType, int itemId) {
        switch (plan.mode) {
            case "warehouse_to_warehouse":
            case "warehouse_to_player":
                return getStockFromWarehouseOrShelter(plan.sourceWarehouse, itemType, itemId);
            case "player_to_warehouse":
                return getPlayerItemQuantity(actingPlayerId, itemType, itemId);
            default:
                return 0;
        }
    }

    private int getStockFromWarehouseOrShelter(String warehouseKey, String itemType, int itemId) {
        if (warehouseKey == null || warehouseKey.isEmpty()) return 0;
        if (!VALID_WAREHOUSE_KEYS.contains(warehouseKey)) return 0;
        if (isShelterWarehouse(warehouseKey)) {
            return getShelterStock(itemType, itemId);
        }
        return getWarehouseStock("warehouse_" + warehouseKey, itemType, itemId);
    }

    private int getShelterStock(String itemType, int itemId) {
        try {
            ShelterStock.ItemType type = ShelterStock.ItemType.valueOf(itemType.toLowerCase(Locale.ROOT));
            Optional<ShelterStock> opt = shelterStockRepository.findByItemTypeAndItemId(type, itemId);
            return opt.map(ShelterStock::getQuantity).orElse(0);
        } catch (Exception ignored) {
            return 0;
        }
    }

    public String buildResultLog(TransportPlan plan) {
        StringBuilder sb = new StringBuilder(SETTLEMENT_HEADER).append("\n");
        sb.append("模式：").append(modeLabel(plan.mode)).append("\n");
        if (plan.sourceWarehouse != null && !plan.sourceWarehouse.isEmpty()) {
            sb.append("源仓库：").append(resolveWarehouseName(plan.sourceWarehouse)).append("\n");
        }
        if (plan.destWarehouse != null && !plan.destWarehouse.isEmpty()) {
            sb.append("目标仓库：").append(resolveWarehouseName(plan.destWarehouse)).append("\n");
        }
        int totalKg = 0;
        for (TransportItem item : plan.items) {
            if (item.actualQty <= 0) continue;
            int kg = (int) (item.actualQty * item.weightPerUnit);
            totalKg += kg;
            String name = resolveItemName(item.itemType, item.itemId);
            switch (plan.mode) {
                case "warehouse_to_warehouse":
                    sb.append(name).append("：搬运").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                case "warehouse_to_player":
                    sb.append(name).append("：搬运").append(item.actualQty).append("单位到个人背包（").append(kg).append("千克）\n");
                    break;
                case "player_to_warehouse":
                    sb.append(name).append("：从个人搬入仓库").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                case "warehouse_to_shelter":
                    sb.append(name).append("：从仓库搬入避难所").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                case "shelter_to_warehouse":
                    sb.append(name).append("：从避难所搬入仓库").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                case "shelter_to_player":
                    sb.append(name).append("：从避难所搬入个人背包").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                case "player_to_shelter":
                    sb.append(name).append("：从个人投入避难所").append(item.actualQty).append("单位（").append(kg).append("千克）\n");
                    break;
                default:
                    break;
            }
        }
        sb.append("总计搬运：").append(totalKg).append("千克");
        boolean needsDeferred = "player_to_warehouse".equals(plan.mode) && plan.playerDeducted;
        if (needsDeferred) {
            sb.append("\n（个人背包已扣除，入仓将在发布反馈后生效）");
        } else {
            sb.append("\n（库存变更将在发布反馈后生效）");
        }
        return sb.toString();
    }

    /** 个人→仓库：提交行动时从玩家背包扣除（须在事务内调用）。 */
    public String deductPlayerItemsForSubmit(TransportPlan plan, Integer playerId) {
        normalizeShelterAliases(plan);
        if (!"player_to_warehouse".equals(plan.mode)) {
            return "仅个人→仓库可在提交时扣除背包";
        }
        List<String> errors = computeTransfer(plan, playerId);
        if (!errors.isEmpty()) {
            return String.join("；", errors);
        }
        try {
            for (TransportItem item : plan.items) {
                if (item.actualQty <= 0) continue;
                removeItemFromPlayer(playerId, item.itemType, item.itemId, item.actualQty);
            }
            plan.playerDeducted = true;
            return null;
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String appendPlayerDeductedMarker(String notes) {
        if (notes == null) notes = "";
        if (notes.contains("[player_deducted:1]")) return notes;
        return notes + (notes.isEmpty() ? "" : "\n") + "[player_deducted:1]";
    }

    public String serializePlan(TransportPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("mode=").append(plan.mode).append("\n");
        if (plan.sourceWarehouse != null) sb.append("source=").append(plan.sourceWarehouse).append("\n");
        if (plan.destWarehouse != null) sb.append("dest=").append(plan.destWarehouse).append("\n");
        if (plan.targetPlayerId != null) sb.append("target=").append(plan.targetPlayerId).append("\n");
        if (plan.playerDeducted) sb.append("playerDeducted=1\n");
        for (TransportItem item : plan.items) {
            if (item.actualQty > 0) {
                sb.append("item=").append(item.itemType).append("|").append(item.itemId).append("|")
                        .append(item.actualQty).append("|").append(item.weightPerUnit).append("\n");
            }
        }
        return sb.toString().trim();
    }

    public TransportPlan deserializePlan(String payload) {
        TransportPlan plan = new TransportPlan();
        if (payload == null) return plan;
        for (String line : payload.split("\n")) {
            line = line.trim();
            if (line.startsWith("mode=")) plan.mode = line.substring(5);
            else if (line.startsWith("source=")) plan.sourceWarehouse = line.substring(7);
            else if (line.startsWith("dest=")) plan.destWarehouse = line.substring(5);
            else if (line.startsWith("target=")) {
                try { plan.targetPlayerId = Integer.parseInt(line.substring(7)); } catch (NumberFormatException ignored) { }
            } else if (line.startsWith("playerDeducted=")) {
                plan.playerDeducted = "1".equals(line.substring(15).trim());
            } else if (line.startsWith("item=")) {
                String[] parts = line.substring(5).split("\\|");
                if (parts.length >= 4) {
                    TransportItem item = new TransportItem();
                    item.itemType = parts[0];
                    item.itemId = Integer.parseInt(parts[1]);
                    item.actualQty = Integer.parseInt(parts[2]);
                    item.weightPerUnit = Double.parseDouble(parts[3]);
                    item.requestedQty = item.actualQty;
                    plan.items.add(item);
                }
            }
        }
        normalizeShelterAliases(plan);
        return plan;
    }

    public static String extractPendingPayload(String result) {
        if (result == null) return null;
        int idx = result.indexOf(PENDING_MARKER);
        if (idx < 0) return null;
        return result.substring(idx + PENDING_MARKER.length()).trim();
    }

    public static String stripPendingBlock(String text) {
        if (text == null) return "";
        int idx = text.indexOf(PENDING_MARKER);
        if (idx >= 0) return text.substring(0, idx).trim();
        return text.trim();
    }

    public String attachPendingPayload(String settlementLog, String payload) {
        return settlementLog + PENDING_MARKER + payload;
    }

    /** Apply inventory changes; returns error message or null on success. */
    public String executePlan(TransportPlan plan, Integer actingPlayerId) {
        normalizeShelterAliases(plan);
        boolean needsCompute = plan.items.stream().allMatch(i -> i.actualQty <= 0);
        if (needsCompute) {
            List<String> errors = computeTransfer(plan, actingPlayerId);
            if (!errors.isEmpty()) {
                return String.join("；", errors);
            }
        }
        if (plan.items.stream().noneMatch(i -> i.actualQty > 0)) {
            return "没有可执行的搬运物资";
        }
        try {
            switch (plan.mode) {
                case "warehouse_to_warehouse":
                    executeWarehouseToWarehouse(plan);
                    break;
                case "warehouse_to_player":
                    executeWarehouseToPlayer(plan, actingPlayerId);
                    break;
                case "player_to_warehouse":
                    if (plan.playerDeducted) {
                        executePlayerToWarehouseInboundOnly(plan);
                    } else {
                        executePlayerToWarehouse(plan, actingPlayerId);
                    }
                    break;
                default:
                    return "未知的搬运模式";
            }
            return null;
        } catch (Exception e) {
            return "搬运执行失败: " + e.getMessage();
        }
    }

    private void applyStockDelta(String warehouseKey, String itemType, int itemId, int delta) {
        if (warehouseKey == null || warehouseKey.isEmpty()) {
            throw new RuntimeException("未指定仓库");
        }
        if (!VALID_WAREHOUSE_KEYS.contains(warehouseKey)) {
            throw new RuntimeException("无效仓库");
        }
        if (isShelterWarehouse(warehouseKey)) {
            updateShelterStock(itemType, itemId, delta);
            return;
        }
        updateWarehouseStock("warehouse_" + warehouseKey, itemType, itemId, delta);
    }

    private void updateShelterStock(String itemType, int itemId, int delta) {
        try {
            ShelterStock.ItemType type = ShelterStock.ItemType.valueOf(itemType.toLowerCase(Locale.ROOT));
            Optional<ShelterStock> opt = shelterStockRepository.findByItemTypeAndItemId(type, itemId);
            if (opt.isPresent()) {
                ShelterStock stock = opt.get();
                int newQty = stock.getQuantity() + delta;
                if (newQty <= 0) {
                    shelterStockRepository.delete(stock);
                } else {
                    stock.setQuantity(newQty);
                    shelterStockRepository.save(stock);
                }
            } else if (delta > 0) {
                ShelterStock stock = new ShelterStock();
                stock.setItemType(type);
                stock.setItemId(itemId);
                stock.setQuantity(delta);
                shelterStockRepository.save(stock);
            }
        } catch (Exception e) {
            throw new RuntimeException("更新避难所库存失败: " + e.getMessage());
        }
    }

    private void executeWarehouseToWarehouse(TransportPlan plan) {
        if (plan.sourceWarehouse == null || plan.sourceWarehouse.isEmpty()) {
            throw new RuntimeException("仓库→仓库模式需要指定源仓库");
        }
        if (plan.destWarehouse == null || plan.destWarehouse.isEmpty()) {
            throw new RuntimeException("仓库→仓库模式需要指定目标仓库");
        }
        for (TransportItem item : plan.items) {
            if (item.actualQty <= 0) continue;
            applyStockDelta(plan.sourceWarehouse, item.itemType, item.itemId, -item.actualQty);
            applyStockDelta(plan.destWarehouse, item.itemType, item.itemId, item.actualQty);
        }
    }

    private void executeWarehouseToPlayer(TransportPlan plan, Integer playerId) {
        if (plan.sourceWarehouse == null || plan.sourceWarehouse.isEmpty()) {
            throw new RuntimeException("仓库→个人模式需要指定源仓库");
        }
        for (TransportItem item : plan.items) {
            if (item.actualQty <= 0) continue;
            applyStockDelta(plan.sourceWarehouse, item.itemType, item.itemId, -item.actualQty);
            addItemToPlayer(playerId, item.itemType, item.itemId, item.actualQty);
        }
    }

    private void executePlayerToWarehouse(TransportPlan plan, Integer playerId) {
        if (plan.destWarehouse == null || plan.destWarehouse.isEmpty()) {
            throw new RuntimeException("个人→仓库模式需要指定目标仓库");
        }
        for (TransportItem item : plan.items) {
            if (item.actualQty <= 0) continue;
            removeItemFromPlayer(playerId, item.itemType, item.itemId, item.actualQty);
            applyStockDelta(plan.destWarehouse, item.itemType, item.itemId, item.actualQty);
        }
    }

    private void executePlayerToWarehouseInboundOnly(TransportPlan plan) {
        if (plan.destWarehouse == null || plan.destWarehouse.isEmpty()) {
            throw new RuntimeException("个人→仓库模式需要指定目标仓库");
        }
        for (TransportItem item : plan.items) {
            if (item.actualQty <= 0) continue;
            applyStockDelta(plan.destWarehouse, item.itemType, item.itemId, item.actualQty);
        }
    }

    private String modeLabel(String mode) {
        switch (mode) {
            case "warehouse_to_warehouse": return "仓库→仓库";
            case "warehouse_to_player": return "仓库→个人";
            case "player_to_warehouse": return "个人→仓库";
            case "warehouse_to_shelter": return "仓库→避难所";
            case "shelter_to_warehouse": return "避难所→仓库";
            case "shelter_to_player": return "避难所→个人";
            case "player_to_shelter": return "个人→避难所";
            default: return mode;
        }
    }

    public String resolveWarehouseName(String warehouseKey) {
        if (warehouseKey == null || warehouseKey.isEmpty()) return "未知仓库";
        if (SHELTER_KEY.equals(warehouseKey)) return "避难所仓库";
        Optional<WarehouseConfig> opt = warehouseConfigRepository.findByWarehouseKey(warehouseKey);
        if (opt.isPresent() && opt.get().getWarehouseName() != null && !opt.get().getWarehouseName().isEmpty()) {
            return opt.get().getWarehouseName();
        }
        switch (warehouseKey) {
            case "general": return "通用仓库";
            case "fuel": return "燃料仓库";
            case "armory": return "镇武库";
            case "dock": return "码头集换站";
            case "rebel": return "反叛者基地";
            case "ark": return "方舟仓库";
            default: return warehouseKey;
        }
    }

    public String resolveItemName(String itemType, int itemId) {
        String tableName;
        switch (itemType) {
            case "weapon": tableName = "weapon"; break;
            case "ammo": tableName = "ammo"; break;
            case "material": tableName = "material"; break;
            default: tableName = "item"; break;
        }
        try {
            Query query = entityManager.createNativeQuery("SELECT name FROM " + tableName + " WHERE id = ?1");
            query.setParameter(1, itemId);
            List<?> results = query.getResultList();
            if (!results.isEmpty() && results.get(0) != null) {
                return results.get(0).toString();
            }
        } catch (Exception ignored) { }
        return "未知物品";
    }

    private int getWarehouseStock(String tableName, String itemType, int itemId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT quantity FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
            query.setParameter(1, itemType);
            query.setParameter(2, itemId);
            List<?> results = query.getResultList();
            if (!results.isEmpty()) return ((Number) results.get(0)).intValue();
        } catch (Exception ignored) { }
        return 0;
    }

    private int getPlayerItemQuantity(Integer playerId, String itemType, int itemId) {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT quantity FROM player_items WHERE player_id = ?1 AND item_type = ?2 AND item_id = ?3");
            query.setParameter(1, playerId);
            query.setParameter(2, itemType);
            query.setParameter(3, itemId);
            List<?> results = query.getResultList();
            if (!results.isEmpty()) return ((Number) results.get(0)).intValue();
        } catch (Exception ignored) { }
        return 0;
    }

    private void updateWarehouseStock(String tableName, String itemType, int itemId, int delta) {
        try {
            Query checkQuery = entityManager.createNativeQuery(
                    "SELECT id, quantity FROM " + tableName + " WHERE item_type = ?1 AND item_id = ?2");
            checkQuery.setParameter(1, itemType);
            checkQuery.setParameter(2, itemId);
            List<?> results = checkQuery.getResultList();
            if (!results.isEmpty()) {
                Object[] row = (Object[]) results.get(0);
                int currentQty = ((Number) row[1]).intValue();
                int newQty = currentQty + delta;
                if (newQty <= 0) {
                    Query deleteQuery = entityManager.createNativeQuery(
                            "DELETE FROM " + tableName + " WHERE id = ?1");
                    deleteQuery.setParameter(1, ((Number) row[0]).intValue());
                    deleteQuery.executeUpdate();
                } else {
                    Query updateQuery = entityManager.createNativeQuery(
                            "UPDATE " + tableName + " SET quantity = ?1 WHERE id = ?2");
                    updateQuery.setParameter(1, newQty);
                    updateQuery.setParameter(2, ((Number) row[0]).intValue());
                    updateQuery.executeUpdate();
                }
            } else if (delta > 0) {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO " + tableName + " (item_type, item_id, quantity) VALUES (?1, ?2, ?3)");
                insertQuery.setParameter(1, itemType);
                insertQuery.setParameter(2, itemId);
                insertQuery.setParameter(3, delta);
                insertQuery.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("更新仓库库存失败: " + e.getMessage());
        }
    }

    private void addItemToPlayer(Integer playerId, String itemType, Integer itemId, Integer quantity) {
        try {
            Query checkQuery = entityManager.createNativeQuery(
                    "SELECT quantity FROM player_items WHERE player_id = ?1 AND item_type = ?2 AND item_id = ?3");
            checkQuery.setParameter(1, playerId);
            checkQuery.setParameter(2, itemType);
            checkQuery.setParameter(3, itemId);
            List<?> existing = checkQuery.getResultList();
            if (!existing.isEmpty()) {
                int currentQty = ((Number) existing.get(0)).intValue();
                Query updateQuery = entityManager.createNativeQuery(
                        "UPDATE player_items SET quantity = ?1 WHERE player_id = ?2 AND item_type = ?3 AND item_id = ?4");
                updateQuery.setParameter(1, currentQty + quantity);
                updateQuery.setParameter(2, playerId);
                updateQuery.setParameter(3, itemType);
                updateQuery.setParameter(4, itemId);
                updateQuery.executeUpdate();
            } else {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO player_items (player_id, item_type, item_id, quantity) VALUES (?1, ?2, ?3, ?4)");
                insertQuery.setParameter(1, playerId);
                insertQuery.setParameter(2, itemType);
                insertQuery.setParameter(3, itemId);
                insertQuery.setParameter(4, quantity);
                insertQuery.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("添加物品失败: " + e.getMessage());
        }
    }

    private void removeItemFromPlayer(Integer playerId, String itemType, int itemId, int quantity) {
        int current = getPlayerItemQuantity(playerId, itemType, itemId);
        if (current < quantity) {
            throw new RuntimeException(resolveItemName(itemType, itemId) + " 个人库存不足（需要 " + quantity + "，当前 " + current + "）");
        }
        int newQty = current - quantity;
        try {
            if (newQty <= 0) {
                Query deleteQuery = entityManager.createNativeQuery(
                        "DELETE FROM player_items WHERE player_id = ?1 AND item_type = ?2 AND item_id = ?3");
                deleteQuery.setParameter(1, playerId);
                deleteQuery.setParameter(2, itemType);
                deleteQuery.setParameter(3, itemId);
                deleteQuery.executeUpdate();
            } else {
                Query updateQuery = entityManager.createNativeQuery(
                        "UPDATE player_items SET quantity = ?1 WHERE player_id = ?2 AND item_type = ?3 AND item_id = ?4");
                updateQuery.setParameter(1, newQty);
                updateQuery.setParameter(2, playerId);
                updateQuery.setParameter(3, itemType);
                updateQuery.setParameter(4, itemId);
                updateQuery.executeUpdate();
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("扣除个人物品失败: " + e.getMessage());
        }
    }
}
