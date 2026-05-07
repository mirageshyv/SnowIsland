package com.example.snowisland.service;

import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.PlayerFoodStock;
import com.example.snowisland.entity.PlayerFoodStockId;
import com.example.snowisland.entity.PlayerEnergyStock;
import com.example.snowisland.entity.PlayerEnergyStockId;
import com.example.snowisland.entity.FoodCatalog;
import com.example.snowisland.entity.EnergyCatalog;
import com.example.snowisland.entity.Trade;
import com.example.snowisland.entity.TradeItem;
import com.example.snowisland.repository.FoodCatalogRepository;
import com.example.snowisland.repository.EnergyCatalogRepository;
import com.example.snowisland.repository.PlayerFoodStockRepository;
import com.example.snowisland.repository.PlayerEnergyStockRepository;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.TradeItemRepository;
import com.example.snowisland.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class TradeService {
    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private TradeItemRepository tradeItemRepository;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    @Autowired
    private FoodCatalogRepository foodCatalogRepository;

    @Autowired
    private EnergyCatalogRepository energyCatalogRepository;

    @Autowired
    private PlayerFoodStockRepository playerFoodStockRepository;

    @Autowired
    private PlayerEnergyStockRepository playerEnergyStockRepository;

    private Map<String, Map<Integer, String>> itemNames = new HashMap<>();
    private Map<String, Map<Integer, String>> itemUnits = new HashMap<>();

    @PostConstruct
    public void initItemData() {
        itemNames.put("item", new HashMap<>());
        itemNames.get("item").put(1, "医疗包");
        itemNames.get("item").put(2, "手电筒");
        itemNames.get("item").put(4, "哨子");
        itemNames.get("item").put(8, "维修工具包");
        itemNames.get("item").put(19, "仓库钥匙");
        itemNames.get("item").put(20, "燃料仓库钥匙");
        itemNames.get("item").put(21, "镇武库钥匙");
        itemNames.get("item").put(22, "码头集购站钥匙");

        itemNames.put("weapon", new HashMap<>());
        itemNames.get("weapon").put(1, "制式手枪");
        itemNames.get("weapon").put(2, "猎枪");
        itemNames.get("weapon").put(3, "警棍");
        itemNames.get("weapon").put(4, "刺刀");
        itemNames.get("weapon").put(9, "斧头");
        itemNames.get("weapon").put(11, "手术刀");
        itemNames.get("weapon").put(12, "炸药");

        itemNames.put("ammo", new HashMap<>());
        itemNames.get("ammo").put(1, "手枪弹");
        itemNames.get("ammo").put(2, "猎枪弹");

        itemNames.put("material", new HashMap<>());
        itemNames.get("material").put(1, "金属制品");
        itemNames.get("material").put(5, "食物");
        itemNames.get("material").put(7, "石料");
        itemNames.get("material").put(12, "发电机");

        itemUnits.put("item", new HashMap<>());
        itemUnits.get("item").put(1, "个");
        itemUnits.get("item").put(2, "个");
        itemUnits.get("item").put(4, "个");
        itemUnits.get("item").put(8, "个");
        itemUnits.get("item").put(19, "把");
        itemUnits.get("item").put(20, "把");
        itemUnits.get("item").put(21, "把");
        itemUnits.get("item").put(22, "把");

        itemUnits.put("weapon", new HashMap<>());
        itemUnits.get("weapon").put(1, "把");
        itemUnits.get("weapon").put(2, "把");
        itemUnits.get("weapon").put(3, "把");
        itemUnits.get("weapon").put(4, "把");
        itemUnits.get("weapon").put(9, "把");
        itemUnits.get("weapon").put(11, "把");
        itemUnits.get("weapon").put(12, "kg");

        itemUnits.put("ammo", new HashMap<>());
        itemUnits.get("ammo").put(1, "枚");
        itemUnits.get("ammo").put(2, "枚");

        itemUnits.put("material", new HashMap<>());
        itemUnits.get("material").put(1, "kg");
        itemUnits.get("material").put(5, "kg");
        itemUnits.get("material").put(7, "kg");
        itemUnits.get("material").put(12, "个");
    }

    private String getItemName(String itemType, Integer itemId) {
        return itemNames.getOrDefault(itemType, Collections.emptyMap()).getOrDefault(itemId, "未知物品");
    }

    private String getItemUnit(String itemType, Integer itemId) {
        return itemUnits.getOrDefault(itemType, Collections.emptyMap()).getOrDefault(itemId, "个");
    }

    public List<Trade> getTradesByPlayerId(Integer playerId) {
        List<Trade> trades = tradeRepository.findByFromPlayerIdOrToPlayerId(playerId, playerId);
        populateItemInfo(trades);
        return trades;
    }

    public List<Trade> getIncomingTrades(Integer playerId) {
        List<Trade> trades = tradeRepository.findByToPlayerId(playerId);
        populateItemInfo(trades);
        return trades;
    }

    /** 侧栏徽章轮询：单条 COUNT，避免每几秒拉全量 incoming + trade_items */
    public long countIncomingPendingTrades(Integer playerId) {
        return tradeRepository.countByToPlayerIdAndStatus(playerId, Trade.TradeStatus.pending);
    }

    private void populateItemInfo(List<Trade> trades) {
        for (Trade trade : trades) {
            List<TradeItem> items = trade.getItems();
            if (items != null) {
                for (TradeItem item : items) {
                    fillTradeItemDisplay(item);
                }
            }
        }
    }

    private void fillTradeItemDisplay(TradeItem item) {
        String specialSupplyType = parseSpecialSupplyType(item.getItemKey());
        if ("food".equals(specialSupplyType)) {
            String itemKey = stripSpecialSupplyKey(item.getItemKey());
            if (itemKey == null) return;
            foodCatalogRepository.findById(itemKey).ifPresent(def -> {
                item.setItemType(ItemType.food);
                item.setName(def.getName());
                item.setUnit(def.getUnit());
                item.setKcalPerUnit(def.getKcalPerUnit());
                item.setLineKcal((long) item.getQuantity() * def.getKcalPerUnit());
            });
            return;
        }
        if ("energy".equals(specialSupplyType)) {
            String itemKey = stripSpecialSupplyKey(item.getItemKey());
            if (itemKey == null) return;
            energyCatalogRepository.findById(itemKey).ifPresent(def -> {
                item.setItemType(ItemType.energy);
                item.setName(def.getName());
                item.setUnit(def.getUnit());
                item.setKcalPerUnit(def.getKcalPerUnit());
                item.setLineKcal((long) item.getQuantity() * def.getKcalPerUnit());
            });
            return;
        }

        String type = item.getItemType().name();
        if (item.getItemType() == ItemType.food) {
            String itemKey = item.getItemKey();
            if (itemKey == null) return;
            foodCatalogRepository.findById(itemKey).ifPresent(def -> {
                item.setName(def.getName());
                item.setUnit(def.getUnit());
                item.setKcalPerUnit(def.getKcalPerUnit());
                item.setLineKcal((long) item.getQuantity() * def.getKcalPerUnit());
            });
            return;
        }
        if (item.getItemType() == ItemType.energy) {
            String itemKey = item.getItemKey();
            if (itemKey == null) return;
            energyCatalogRepository.findById(itemKey).ifPresent(def -> {
                item.setName(def.getName());
                item.setUnit(def.getUnit());
                item.setKcalPerUnit(def.getKcalPerUnit());
                item.setLineKcal((long) item.getQuantity() * def.getKcalPerUnit());
            });
            return;
        }
        item.setName(getItemName(type, item.getItemId()));
        item.setUnit(getItemUnit(type, item.getItemId()));
    }

    public Map<String, Object> getTradeDetail(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Trade> tradeOpt = tradeRepository.findById(id);
            if (!tradeOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "交易不存在");
                return result;
            }
            
            Trade trade = tradeOpt.get();
            List<TradeItem> items = tradeItemRepository.findByTradeId(id);
            
            result.put("success", true);
            result.put("trade", trade);
            result.put("items", items);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取交易详情失败: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> createTrade(Trade trade, List<Map<String, Object>> items) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 发送交易即暂扣发起方“提供”物品；若后续失败由事务回滚
            Map<String, Object> reserveResult = reserveOfferedItems(trade.getFromPlayerId(), items);
            if (!(Boolean) reserveResult.getOrDefault("success", false)) {
                result.put("success", false);
                result.put("message", reserveResult.getOrDefault("message", "库存不足，无法发起交易"));
                return result;
            }

            trade.setStatus(Trade.TradeStatus.pending);
            Trade savedTrade = tradeRepository.save(trade);
            
            for (Map<String, Object> item : items) {
                TradeItem tradeItem = new TradeItem();
                tradeItem.setTradeId(savedTrade.getId());
                String requestType = (String) item.get("itemType");
                if ("food".equals(requestType) || "energy".equals(requestType)) {
                    // 兼容旧库 ENUM(item_type)：food/energy 落库为 material，真实类型编码到 item_key
                    tradeItem.setItemType(TradeItem.ItemType.material);
                } else {
                    tradeItem.setItemType(TradeItem.ItemType.valueOf(requestType));
                }
                Object itemIdObj = item.get("itemId");
                if (itemIdObj instanceof Number) {
                    tradeItem.setItemId(((Number) itemIdObj).intValue());
                } else {
                    // 兼容旧表结构 item_id NOT NULL：food/energy 使用 item_key，item_id 置 0 占位
                    tradeItem.setItemId(0);
                }
                String itemKey = (String) item.get("itemKey");
                if ("food".equals(requestType) || "energy".equals(requestType)) {
                    tradeItem.setItemKey(requestType + ":" + itemKey);
                } else {
                    tradeItem.setItemKey(itemKey);
                }
                Object qtyObj = item.get("quantity");
                tradeItem.setQuantity(qtyObj instanceof Number ? ((Number) qtyObj).intValue() : 1);
                tradeItem.setDirection(TradeItem.Direction.valueOf((String) item.get("direction")));
                tradeItemRepository.save(tradeItem);
            }
            
            result.put("success", true);
            result.put("trade", savedTrade);
            
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            result.put("success", false);
            result.put("message", "创建交易失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> acceptTrade(Integer id, Integer playerId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<Trade> tradeOpt = tradeRepository.findById(id);
            if (!tradeOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "交易不存在");
                return result;
            }

            Trade trade = tradeOpt.get();

            if (!trade.getToPlayerId().equals(playerId)) {
                result.put("success", false);
                result.put("message", "无权操作此交易");
                return result;
            }

            if (trade.getStatus() != Trade.TradeStatus.pending) {
                result.put("success", false);
                result.put("message", "交易状态不是待处理");
                return result;
            }

            List<TradeItem> tradeItems = tradeItemRepository.findByTradeId(id);
            String transferMessage = executeItemTransfer(trade, tradeItems);

            trade.setStatus(Trade.TradeStatus.completed);
            tradeRepository.save(trade);
            result.put("success", true);
            result.put("message", "交易成功！" + transferMessage);
            result.put("trade", trade);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认交易失败: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private String executeItemTransfer(Trade trade, List<TradeItem> tradeItems) {
        StringBuilder sb = new StringBuilder();
        int successCount = 0;

        for (TradeItem item : tradeItems) {
            Integer fromPlayerId = trade.getFromPlayerId();
            Integer toPlayerId = trade.getToPlayerId();

            if (item.getDirection() == TradeItem.Direction.give) {
                // give 已在创建交易时暂扣，这里仅发放给接收方
                int affected = addStockByType(toPlayerId, item);
                if (affected > 0) {
                    successCount++;
                }
            } else if (item.getDirection() == TradeItem.Direction.take) {
                int affected = transferStock(toPlayerId, fromPlayerId, item);
                if (affected > 0) {
                    successCount++;
                }
            }
        }

        sb.append("成功添加 ").append(successCount).append(" 项物资到玩家背包。");
        return sb.toString();
    }

    private int transferStock(Integer fromPlayerId, Integer toPlayerId, TradeItem item) {
        String specialSupplyType = parseSpecialSupplyType(item.getItemKey());
        if ("food".equals(specialSupplyType)) {
            return transferFoodStock(fromPlayerId, toPlayerId, stripSpecialSupplyKey(item.getItemKey()), item.getQuantity());
        }
        if ("energy".equals(specialSupplyType)) {
            return transferEnergyStock(fromPlayerId, toPlayerId, stripSpecialSupplyKey(item.getItemKey()), item.getQuantity());
        }
        if (item.getItemType() == ItemType.food) {
            return transferFoodStock(fromPlayerId, toPlayerId, item.getItemKey(), item.getQuantity());
        }
        if (item.getItemType() == ItemType.energy) {
            return transferEnergyStock(fromPlayerId, toPlayerId, item.getItemKey(), item.getQuantity());
        }
        int reduced = reducePlayerItem(fromPlayerId, item.getItemType(), item.getItemId(), item.getQuantity());
        if (reduced <= 0) return 0;
        return addStockByType(toPlayerId, item);
    }

    private int addStockByType(Integer playerId, TradeItem item) {
        String specialSupplyType = parseSpecialSupplyType(item.getItemKey());
        if ("food".equals(specialSupplyType)) {
            return addFoodStock(playerId, stripSpecialSupplyKey(item.getItemKey()), item.getQuantity());
        }
        if ("energy".equals(specialSupplyType)) {
            return addEnergyStock(playerId, stripSpecialSupplyKey(item.getItemKey()), item.getQuantity());
        }
        if (item.getItemType() == ItemType.food) {
            return addFoodStock(playerId, item.getItemKey(), item.getQuantity());
        }
        if (item.getItemType() == ItemType.energy) {
            return addEnergyStock(playerId, item.getItemKey(), item.getQuantity());
        }
        return addPlayerItem(playerId, item.getItemType(), item.getItemId(), item.getQuantity());
    }

    private int transferFoodStock(Integer fromPlayerId, Integer toPlayerId, String itemKey, Integer quantity) {
        if (itemKey == null || quantity == null || quantity <= 0) return 0;
        PlayerFoodStockId fromId = new PlayerFoodStockId();
        fromId.setPlayerId(fromPlayerId);
        fromId.setItemKey(itemKey);
        Optional<PlayerFoodStock> fromOpt = playerFoodStockRepository.findById(fromId);
        if (!fromOpt.isPresent()) return 0;
        PlayerFoodStock from = fromOpt.get();
        if (from.getQuantity() < quantity) return 0;
        from.setQuantity(from.getQuantity() - quantity);
        playerFoodStockRepository.save(from);

        PlayerFoodStockId toId = new PlayerFoodStockId();
        toId.setPlayerId(toPlayerId);
        toId.setItemKey(itemKey);
        PlayerFoodStock to = playerFoodStockRepository.findById(toId).orElseGet(() -> {
            PlayerFoodStock created = new PlayerFoodStock();
            created.setId(toId);
            created.setQuantity(0);
            return created;
        });
        to.setQuantity(to.getQuantity() + quantity);
        playerFoodStockRepository.save(to);
        return 1;
    }

    private int transferEnergyStock(Integer fromPlayerId, Integer toPlayerId, String itemKey, Integer quantity) {
        if (itemKey == null || quantity == null || quantity <= 0) return 0;
        PlayerEnergyStockId fromId = new PlayerEnergyStockId();
        fromId.setPlayerId(fromPlayerId);
        fromId.setItemKey(itemKey);
        Optional<PlayerEnergyStock> fromOpt = playerEnergyStockRepository.findById(fromId);
        if (!fromOpt.isPresent()) return 0;
        PlayerEnergyStock from = fromOpt.get();
        if (from.getQuantity() < quantity) return 0;
        from.setQuantity(from.getQuantity() - quantity);
        playerEnergyStockRepository.save(from);

        PlayerEnergyStockId toId = new PlayerEnergyStockId();
        toId.setPlayerId(toPlayerId);
        toId.setItemKey(itemKey);
        PlayerEnergyStock to = playerEnergyStockRepository.findById(toId).orElseGet(() -> {
            PlayerEnergyStock created = new PlayerEnergyStock();
            created.setId(toId);
            created.setQuantity(0);
            return created;
        });
        to.setQuantity(to.getQuantity() + quantity);
        playerEnergyStockRepository.save(to);
        return 1;
    }

    private int addFoodStock(Integer playerId, String itemKey, Integer quantity) {
        if (itemKey == null || quantity == null || quantity <= 0) return 0;
        PlayerFoodStockId id = new PlayerFoodStockId();
        id.setPlayerId(playerId);
        id.setItemKey(itemKey);
        PlayerFoodStock row = playerFoodStockRepository.findById(id).orElseGet(() -> {
            PlayerFoodStock created = new PlayerFoodStock();
            created.setId(id);
            created.setQuantity(0);
            return created;
        });
        row.setQuantity(row.getQuantity() + quantity);
        playerFoodStockRepository.save(row);
        return 1;
    }

    private int addEnergyStock(Integer playerId, String itemKey, Integer quantity) {
        if (itemKey == null || quantity == null || quantity <= 0) return 0;
        PlayerEnergyStockId id = new PlayerEnergyStockId();
        id.setPlayerId(playerId);
        id.setItemKey(itemKey);
        PlayerEnergyStock row = playerEnergyStockRepository.findById(id).orElseGet(() -> {
            PlayerEnergyStock created = new PlayerEnergyStock();
            created.setId(id);
            created.setQuantity(0);
            return created;
        });
        row.setQuantity(row.getQuantity() + quantity);
        playerEnergyStockRepository.save(row);
        return 1;
    }

    private Map<String, Object> reserveOfferedItems(Integer fromPlayerId, List<Map<String, Object>> items) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> giveItems = new ArrayList<>();
        for (Map<String, Object> item : items) {
            String direction = String.valueOf(item.get("direction"));
            if ("give".equals(direction)) {
                giveItems.add(item);
            }
        }

        for (Map<String, Object> item : giveItems) {
            String itemType = String.valueOf(item.get("itemType"));
            int qty = ((Number) item.get("quantity")).intValue();
            if (qty <= 0) {
                result.put("success", false);
                result.put("message", "交易数量必须大于 0");
                return result;
            }
            if ("food".equals(itemType)) {
                String itemKey = (String) item.get("itemKey");
                PlayerFoodStockId id = new PlayerFoodStockId();
                id.setPlayerId(fromPlayerId);
                id.setItemKey(itemKey);
                int stock = playerFoodStockRepository.findById(id).map(PlayerFoodStock::getQuantity).orElse(0);
                if (stock < qty) {
                    String name = foodCatalogRepository.findById(itemKey).map(FoodCatalog::getName).orElse("食物");
                    result.put("success", false);
                    result.put("message", name + " 库存不足");
                    return result;
                }
            } else if ("energy".equals(itemType)) {
                String itemKey = (String) item.get("itemKey");
                PlayerEnergyStockId id = new PlayerEnergyStockId();
                id.setPlayerId(fromPlayerId);
                id.setItemKey(itemKey);
                int stock = playerEnergyStockRepository.findById(id).map(PlayerEnergyStock::getQuantity).orElse(0);
                if (stock < qty) {
                    String name = energyCatalogRepository.findById(itemKey).map(EnergyCatalog::getName).orElse("燃料");
                    result.put("success", false);
                    result.put("message", name + " 库存不足");
                    return result;
                }
            } else {
                Integer itemId = ((Number) item.get("itemId")).intValue();
                ItemType type = ItemType.valueOf(itemType);
                int stock = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(fromPlayerId, type, itemId)
                        .map(PlayerItem::getQuantity)
                        .orElse(0);
                if (stock < qty) {
                    result.put("success", false);
                    result.put("message", getItemName(itemType, itemId) + " 库存不足");
                    return result;
                }
            }
        }

        for (Map<String, Object> item : giveItems) {
            String itemType = String.valueOf(item.get("itemType"));
            int qty = ((Number) item.get("quantity")).intValue();
            if ("food".equals(itemType)) {
                String itemKey = (String) item.get("itemKey");
                PlayerFoodStockId id = new PlayerFoodStockId();
                id.setPlayerId(fromPlayerId);
                id.setItemKey(itemKey);
                playerFoodStockRepository.findById(id).ifPresent(row -> {
                    row.setQuantity(row.getQuantity() - qty);
                    playerFoodStockRepository.save(row);
                });
            } else if ("energy".equals(itemType)) {
                String itemKey = (String) item.get("itemKey");
                PlayerEnergyStockId id = new PlayerEnergyStockId();
                id.setPlayerId(fromPlayerId);
                id.setItemKey(itemKey);
                playerEnergyStockRepository.findById(id).ifPresent(row -> {
                    row.setQuantity(row.getQuantity() - qty);
                    playerEnergyStockRepository.save(row);
                });
            } else {
                Integer itemId = ((Number) item.get("itemId")).intValue();
                ItemType type = ItemType.valueOf(itemType);
                reducePlayerItem(fromPlayerId, type, itemId, qty);
            }
        }

        result.put("success", true);
        return result;
    }

    private String parseSpecialSupplyType(String itemKey) {
        if (itemKey == null) return null;
        if (itemKey.startsWith("food:")) return "food";
        if (itemKey.startsWith("energy:")) return "energy";
        return null;
    }

    private String stripSpecialSupplyKey(String itemKey) {
        if (itemKey == null) return null;
        if (itemKey.startsWith("food:")) return itemKey.substring("food:".length());
        if (itemKey.startsWith("energy:")) return itemKey.substring("energy:".length());
        return itemKey;
    }

    private int reducePlayerItem(Integer playerId, TradeItem.ItemType itemType, Integer itemId, Integer quantity) {
        return playerItemRepository.updateQuantity(playerId, itemType, itemId, -quantity);
    }

    private int addPlayerItem(Integer playerId, TradeItem.ItemType itemType, Integer itemId, Integer quantity) {
        Optional<PlayerItem> existingOpt = playerItemRepository.findByPlayerIdAndItemTypeAndItemId(
            playerId, itemType, itemId);

        if (existingOpt.isPresent()) {
            return playerItemRepository.updateQuantity(playerId, itemType, itemId, quantity);
        } else {
            PlayerItem newItem = new PlayerItem();
            newItem.setPlayerId(playerId);
            newItem.setItemType(itemType);
            newItem.setItemId(itemId);
            newItem.setQuantity(quantity);
            playerItemRepository.save(newItem);
            return 1;
        }
    }

    @Transactional
    public Map<String, Object> rejectTrade(Integer id, Integer playerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Optional<Trade> tradeOpt = tradeRepository.findById(id);
            if (!tradeOpt.isPresent()) {
                result.put("success", false);
                result.put("message", "交易不存在");
                return result;
            }
            
            Trade trade = tradeOpt.get();
            
            if (!trade.getToPlayerId().equals(playerId)) {
                result.put("success", false);
                result.put("message", "无权操作此交易");
                return result;
            }

            if (trade.getStatus() != Trade.TradeStatus.pending) {
                result.put("success", false);
                result.put("message", "该交易不是待处理状态");
                return result;
            }

            // 交易被拒绝：退回发起方在 pending 时被暂扣的 give 物资
            List<TradeItem> tradeItems = tradeItemRepository.findByTradeId(id);
            for (TradeItem item : tradeItems) {
                if (item.getDirection() == TradeItem.Direction.give) {
                    addStockByType(trade.getFromPlayerId(), item);
                }
            }
            
            trade.setStatus(Trade.TradeStatus.rejected);
            tradeRepository.save(trade);
            
            result.put("success", true);
            result.put("message", "交易已拒绝");
            result.put("trade", trade);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "拒绝交易失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}
