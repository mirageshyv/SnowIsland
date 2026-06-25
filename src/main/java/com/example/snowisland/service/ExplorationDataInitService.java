package com.example.snowisland.service;

import com.example.snowisland.entity.IslandEvent;
import com.example.snowisland.entity.IslandEventReward;
import com.example.snowisland.entity.TradeItem;
import com.example.snowisland.repository.IslandEventRepository;
import com.example.snowisland.repository.IslandEventRewardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExplorationDataInitService {

    private static final Logger logger = LoggerFactory.getLogger(ExplorationDataInitService.class);

    @Autowired
    private IslandEventRepository islandEventRepository;

    @Autowired
    private IslandEventRewardRepository islandEventRewardRepository;

    @PostConstruct
    public void init() {
        try {
            long count = islandEventRepository.count();
            if (count == 0) {
                logger.info("探索事件表为空，开始初始化数据...");
                importEventsFromFile();
            } else {
                logger.info("探索事件表已有 {} 条记录，跳过初始化", count);
            }
        } catch (Exception e) {
            logger.error("初始化探索事件数据失败: {}", e.getMessage(), e);
        }
    }

    private List<String> readEventLines() {
        List<String> lines = readFromClasspath("exploration_events.txt");
        if (!lines.isEmpty()) {
            logger.info("从classpath读取探索事件数据");
            return lines;
        }

        String filePath = "tansuo.txt";
        Path path = Paths.get(filePath);
        logger.info("尝试从文件系统读取: {}", path.toAbsolutePath());
        if (Files.exists(path)) {
            lines = readFileWithEncoding(path, "UTF-8");
            if (lines.isEmpty()) {
                lines = readFileWithEncoding(path, "GBK");
            }
        }
        return lines;
    }

    private List<String> readFromClasspath(String resourceName) {
        List<String> lines = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            if (is == null) {
                return lines;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (Exception e) {
            logger.warn("从classpath读取 {} 失败: {}", resourceName, e.getMessage());
        }
        return lines;
    }

    private List<String> readFileWithEncoding(Path path, String encoding) {
        try {
            long size = Files.size(path);
            logger.info("文件大小: {} 字节, 尝试编码: {}", size, encoding);
            List<String> lines = Files.readAllLines(path, java.nio.charset.Charset.forName(encoding));
            logger.info("使用编码 {} 读取到 {} 行", encoding, lines.size());
            if (!lines.isEmpty()) {
                logger.info("第一行内容: {}", lines.get(0));
            }
            return lines;
        } catch (Exception e) {
            logger.warn("使用编码 {} 读取文件失败: {}", encoding, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Transactional
    public void importEventsFromFile() {
        try {
            List<String> lines = readEventLines();
            if (lines.isEmpty()) {
                logger.warn("未找到探索事件数据");
                return;
            }
            logger.info("读取到 {} 行文本", lines.size());
            List<ExplorationEventData> events = parseEvents(lines);
            logger.info("解析到 {} 个事件", events.size());
            
            int eventNum = 1;
            for (ExplorationEventData eventData : events) {
                IslandEvent event = new IslandEvent();
                event.setName(eventData.name);
                event.setDescription(eventData.fullText);
                event.setLocationDesc(eventData.locationDesc);
                event.setLoreFragment(eventData.loreFragment);
                event.setEventDifficulty(eventData.difficulty);
                event.setTriggered(false);
                event.setRarity(getRarityByDifficulty(eventData.difficulty));
                event.setIsSpecial(eventData.difficulty == 20);
                
                IslandEvent savedEvent = islandEventRepository.save(event);
                logger.info("导入事件 #{}: {} (难度: {})", eventNum, eventData.name, eventData.difficulty);
                
                for (RewardData reward : eventData.rewards) {
                    IslandEventReward eventReward = new IslandEventReward();
                    eventReward.setEventId(savedEvent.getId());
                    eventReward.setItemType(reward.itemType);
                    eventReward.setItemId(reward.itemId);
                    eventReward.setQuantity(reward.quantity);
                    islandEventRewardRepository.save(eventReward);
                }
                
                eventNum++;
            }
            
            logger.info("成功导入 {} 个探索事件", events.size());
        } catch (Exception e) {
            logger.error("导入探索事件失败: {}", e.getMessage(), e);
        }
    }

    private String getRarityByDifficulty(int difficulty) {
        if (difficulty >= 18) return "legendary";
        if (difficulty >= 14) return "epic";
        if (difficulty >= 10) return "rare";
        if (difficulty >= 6) return "uncommon";
        return "common";
    }

    static class ExplorationEventData {
        String name;
        String locationDesc;
        String loreFragment;
        String fullText;
        int difficulty;
        List<RewardData> rewards = new ArrayList<>();
    }

    static class RewardData {
        TradeItem.ItemType itemType;
        int itemId;
        int quantity;
    }

    private List<ExplorationEventData> parseEvents(List<String> lines) {
        List<ExplorationEventData> events = new ArrayList<>();
        ExplorationEventData currentEvent = null;
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.matches("^事件\\d+.*")) {
                if (currentEvent != null) {
                    events.add(currentEvent);
                }
                currentEvent = new ExplorationEventData();
                Matcher matcher = Pattern.compile("^事件\\d+[（(]?[^）)]*[）)]?[：:]\\s*(.*)").matcher(line);
                if (matcher.find()) {
                    currentEvent.name = matcher.group(1).trim();
                } else {
                    String[] parts = line.split("：", 2);
                    if (parts.length > 1) {
                        currentEvent.name = parts[1].trim();
                    } else {
                        currentEvent.name = line;
                    }
                }
            } else if (currentEvent != null) {
                if (line.startsWith("地点描述") || line.startsWith("地点描述：")) {
                    String[] parts = line.split("：", 2);
                    currentEvent.locationDesc = parts.length > 1 ? parts[1].trim() : "";
                } else if (line.startsWith("可获得物资") || line.startsWith("可获得物资：")) {
                    String[] parts = line.split("：", 2);
                    String rewardsStr = parts.length > 1 ? parts[1].trim() : "";
                    currentEvent.rewards = parseRewards(rewardsStr);
                } else if (line.startsWith("历史秘密碎片") || line.startsWith("历史秘密碎片：")) {
                    String[] parts = line.split("：", 2);
                    currentEvent.loreFragment = parts.length > 1 ? parts[1].trim() : "";
                } else if (line.startsWith("难度") || line.startsWith("难度：")) {
                    String[] parts = line.split("：", 2);
                    if (parts.length > 1) {
                        try {
                            currentEvent.difficulty = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException e) {
                            currentEvent.difficulty = 1;
                        }
                    }
                }
                
                if (currentEvent.fullText == null) {
                    currentEvent.fullText = line;
                } else {
                    currentEvent.fullText += "\n" + line;
                }
            }
        }
        
        if (currentEvent != null) {
            events.add(currentEvent);
        }
        
        return events;
    }

    private List<RewardData> parseRewards(String rewardsStr) {
        List<RewardData> rewards = new ArrayList<>();
        if (rewardsStr == null || rewardsStr.isEmpty()) return rewards;
        if (rewardsStr.contains("无直接物资") || rewardsStr.contains("无物资")) return rewards;
        if (rewardsStr.contains("请私信dm") || rewardsStr.contains("私信dm")) return rewards;
        
        String[] items = rewardsStr.split("[，,、]");
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            
            RewardData reward = parseSingleReward(item);
            if (reward != null) {
                rewards.add(reward);
            }
        }
        
        return rewards;
    }

    private RewardData parseSingleReward(String itemStr) {
        itemStr = itemStr.trim();
        RewardData reward = new RewardData();
        
        Map<String, TradeItem.ItemType> typeMap = new HashMap<>();
        typeMap.put("绳索", TradeItem.ItemType.item);
        typeMap.put("火把", TradeItem.ItemType.item);
        typeMap.put("医疗包", TradeItem.ItemType.item);
        typeMap.put("手电筒", TradeItem.ItemType.item);
        typeMap.put("维修工具包", TradeItem.ItemType.item);
        typeMap.put("哨子", TradeItem.ItemType.item);
        typeMap.put("朗姆酒", TradeItem.ItemType.item);
        typeMap.put("渔网", TradeItem.ItemType.item);
        typeMap.put("点火工具", TradeItem.ItemType.item);
        typeMap.put("蜡烛", TradeItem.ItemType.item);
        typeMap.put("铅笔", TradeItem.ItemType.item);
        typeMap.put("火柴", TradeItem.ItemType.item);
        
        typeMap.put("猎弓", TradeItem.ItemType.weapon);
        typeMap.put("制式手枪", TradeItem.ItemType.weapon);
        typeMap.put("旧式手枪", TradeItem.ItemType.weapon);
        typeMap.put("猎枪", TradeItem.ItemType.weapon);
        typeMap.put("信号枪", TradeItem.ItemType.weapon);
        
        typeMap.put("猎枪弹", TradeItem.ItemType.ammo);
        typeMap.put("手枪弹", TradeItem.ItemType.ammo);
        typeMap.put("信号弹", TradeItem.ItemType.ammo);
        
        typeMap.put("金属制品", TradeItem.ItemType.material);
        typeMap.put("木材", TradeItem.ItemType.material);
        typeMap.put("食物", TradeItem.ItemType.material);
        typeMap.put("帆布", TradeItem.ItemType.material);
        typeMap.put("煤油", TradeItem.ItemType.material);
        typeMap.put("沥青", TradeItem.ItemType.material);
        typeMap.put("燃料", TradeItem.ItemType.material);
        typeMap.put("石料", TradeItem.ItemType.material);
        typeMap.put("炸药", TradeItem.ItemType.material);
        typeMap.put("医疗资源", TradeItem.ItemType.item);
        typeMap.put("发动机", TradeItem.ItemType.material);
        typeMap.put("发电机", TradeItem.ItemType.material);
        typeMap.put("草药", TradeItem.ItemType.item);
        typeMap.put("木板", TradeItem.ItemType.material);
        
        Map<String, Integer> idMap = new HashMap<>();
        idMap.put("绳索", 3);
        idMap.put("火把", 6);
        idMap.put("医疗包", 1);
        idMap.put("手电筒", 2);
        idMap.put("维修工具包", 8);
        idMap.put("哨子", 4);
        idMap.put("朗姆酒", 10);
        idMap.put("渔网", 12);
        idMap.put("点火工具", 15);
        idMap.put("蜡烛", 9);
        idMap.put("铅笔", 11);
        idMap.put("火柴", 13);
        
        idMap.put("猎弓", 5);
        idMap.put("制式手枪", 1);
        idMap.put("旧式手枪", 6);
        idMap.put("猎枪", 2);
        idMap.put("信号枪", 7);
        
        idMap.put("猎枪弹", 2);
        idMap.put("手枪弹", 1);
        idMap.put("信号弹", 3);
        
        idMap.put("金属制品", 1);
        idMap.put("木材", 2);
        idMap.put("食物", 5);
        idMap.put("帆布", 9);
        idMap.put("煤油", 8);
        idMap.put("沥青", 6);
        idMap.put("燃料", 8);
        idMap.put("石料", 7);
        idMap.put("炸药", 14);
        idMap.put("医疗资源", 1);
        idMap.put("发动机", 11);
        idMap.put("发电机", 12);
        idMap.put("草药", 16);
        idMap.put("木板", 4);
        
        Pattern pattern = Pattern.compile("(.+?)\\s*[（(]([^）)]+?)[）)]");
        Matcher matcher = pattern.matcher(itemStr);
        
        String itemName;
        int quantity = 1;
        
        if (matcher.find()) {
            itemName = matcher.group(1).trim();
            String qtyStr = matcher.group(2).trim();
            
            Pattern qtyPattern = Pattern.compile("(\\d+\\.?\\d*)\\s*(吨|千克|kg|公斤|个|把|瓶|米|升|份|单位|盒|张|支|发|枚)");
            Matcher qtyMatcher = qtyPattern.matcher(qtyStr);
            
            if (qtyMatcher.find()) {
                double qty = Double.parseDouble(qtyMatcher.group(1));
                String unit = qtyMatcher.group(2);
                
                if (unit.equals("吨")) {
                    qty = qty * 1000;
                } else if (unit.equals("千克") || unit.equals("公斤")) {
                    
                }
                
                quantity = (int) Math.ceil(qty);
            } else {
                try {
                    quantity = Integer.parseInt(qtyStr.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    quantity = 1;
                }
            }
        } else {
            itemName = itemStr;
            quantity = 1;
        }
        
        TradeItem.ItemType itemType = typeMap.get(itemName);
        Integer itemId = idMap.get(itemName);
        
        if (itemType != null && itemId != null) {
            reward.itemType = itemType;
            reward.itemId = itemId;
            reward.quantity = quantity;
            return reward;
        }
        
        logger.warn("无法识别的物资: {}", itemName);
        return null;
    }

    @Transactional
    public void reimportEvents() {
        islandEventRewardRepository.deleteAll();
        islandEventRepository.deleteAll();
        importEventsFromFile();
    }
}
