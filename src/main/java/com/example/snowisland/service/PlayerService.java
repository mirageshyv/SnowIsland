package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.User;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Map<String, Map<Integer, String>> itemNames = new HashMap<>();
    private Map<String, Map<Integer, String>> itemUnits = new HashMap<>();

    public PlayerService() {
        initItemData();
    }

    private void initItemData() {
        itemNames.put("item", new HashMap<>());
        itemNames.put("weapon", new HashMap<>());
        itemNames.put("ammo", new HashMap<>());
        itemNames.put("material", new HashMap<>());

        itemUnits.put("item", new HashMap<>());
        itemUnits.put("weapon", new HashMap<>());
        itemUnits.put("ammo", new HashMap<>());
        itemUnits.put("material", new HashMap<>());
    }

    public void loadItemNames() {
        List<Object[]> items = entityManager.createNativeQuery(
            "SELECT 'item' as type, id, name, unit FROM item " +
            "UNION ALL SELECT 'weapon', id, name, unit FROM weapon " +
            "UNION ALL SELECT 'ammo', id, name, unit FROM ammo " +
            "UNION ALL SELECT 'material', id, name, unit FROM material"
        ).getResultList();

        for (Object[] row : items) {
            String type = (String) row[0];
            Integer id = ((Number) row[1]).intValue();
            String name = (String) row[2];
            String unit = (String) row[3];
            itemNames.get(type).put(id, name);
            itemUnits.get(type).put(id, unit);
        }
    }

    public List<Map<String, Object>> getPlayerItems(Integer playerId) {
        loadItemNames();
        List<PlayerItem> playerItems = playerItemRepository.findByPlayerId(playerId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (PlayerItem pi : playerItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pi.getItemId());
            item.put("type", pi.getItemType().name().toLowerCase());
            item.put("quantity", pi.getQuantity());
            String type = pi.getItemType().name().toLowerCase();
            item.put("name", itemNames.get(type).getOrDefault(pi.getItemId(), "未知物品"));
            item.put("unit", itemUnits.get(type).getOrDefault(pi.getItemId(), "个"));
            result.add(item);
        }

        return result;
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    public Player getPlayerById(Integer id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Transactional
    public Map<String, Object> createPlayer(Player player, String loginUsername) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Player savedPlayer = playerRepository.save(player);
            
            // 创建对应的用户账号
            User user = new User();
            user.setUsername("player" + savedPlayer.getId());
            user.setPassword("test123");
            user.setRole(User.Role.PLAYER);
            user.setPlayerId(savedPlayer.getId());
            userRepository.save(user);
            
            result.put("success", true);
            result.put("player", savedPlayer);
            result.put("username", "player" + savedPlayer.getId());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建玩家失败: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> updatePlayer(Integer id, Player player) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Player existingPlayer = playerRepository.findById(id).orElse(null);
            if (existingPlayer == null) {
                result.put("success", false);
                result.put("message", "玩家不存在");
                return result;
            }
            
            existingPlayer.setName(player.getName());
            existingPlayer.setIsWeak(player.getIsWeak());
            existingPlayer.setIsOverworked(player.getIsOverworked());
            existingPlayer.setIsInjured(player.getIsInjured());
            existingPlayer.setJobId(player.getJobId());
            existingPlayer.setSkillId(player.getSkillId());
            existingPlayer.setFaction(player.getFaction());
            
            playerRepository.save(existingPlayer);
            result.put("success", true);
            result.put("player", existingPlayer);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新玩家失败: " + e.getMessage());
        }
        
        return result;
    }

    @Transactional
    public Map<String, Object> deletePlayer(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            playerRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "删除成功");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }
        
        return result;
    }
}
