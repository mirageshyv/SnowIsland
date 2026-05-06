package com.example.snowisland.service;

import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.Trade;
import com.example.snowisland.entity.TradeItem;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.TradeItemRepository;
import com.example.snowisland.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                    item.setName(getItemName(item.getItemType().name(), item.getItemId()));
                    item.setUnit(getItemUnit(item.getItemType().name(), item.getItemId()));
                }
            }
        }
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
            trade.setStatus(Trade.TradeStatus.pending);
            Trade savedTrade = tradeRepository.save(trade);
            
            for (Map<String, Object> item : items) {
                TradeItem tradeItem = new TradeItem();
                tradeItem.setTradeId(savedTrade.getId());
                tradeItem.setItemType(TradeItem.ItemType.valueOf((String) item.get("itemType")));
                tradeItem.setItemId((Integer) item.get("itemId"));
                tradeItem.setQuantity((Integer) item.get("quantity"));
                tradeItem.setDirection(TradeItem.Direction.valueOf((String) item.get("direction")));
                tradeItemRepository.save(tradeItem);
            }
            
            result.put("success", true);
            result.put("trade", savedTrade);
            
        } catch (Exception e) {
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
                int toAdded = addPlayerItem(toPlayerId, item.getItemType(), item.getItemId(), item.getQuantity());
                if (toAdded > 0) {
                    successCount++;
                }
            } else if (item.getDirection() == TradeItem.Direction.take) {
                int toAdded = addPlayerItem(fromPlayerId, item.getItemType(), item.getItemId(), item.getQuantity());
                if (toAdded > 0) {
                    successCount++;
                }
            }
        }

        sb.append("成功添加 ").append(successCount).append(" 项物资到玩家背包。");
        return sb.toString();
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
