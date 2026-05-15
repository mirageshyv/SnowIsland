package com.example.snowisland.service;

import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.User;
import com.example.snowisland.entity.Job;
import com.example.snowisland.entity.Skill;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.repository.UserRepository;
import com.example.snowisland.repository.JobRepository;
import com.example.snowisland.repository.SkillRepository;
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

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private SkillRepository skillRepository;

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
        System.out.println("=== getPlayerItems called for playerId: " + playerId);
        loadItemNames();
        System.out.println("=== itemNames loaded, material size: " + itemNames.get("material").size());
        
        // Try native query first for debugging
        List<PlayerItem> playerItemsNative = playerItemRepository.findByPlayerIdNative(playerId);
        System.out.println("=== [NATIVE] Found " + playerItemsNative.size() + " player items for player " + playerId);
        
        List<PlayerItem> playerItems = playerItemRepository.findByPlayerId(playerId);
        System.out.println("=== [JPA] Found " + playerItems.size() + " player items for player " + playerId);
        
        List<Map<String, Object>> result = new ArrayList<>();

        for (PlayerItem pi : playerItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", pi.getItemId());
            item.put("type", pi.getItemType().name().toLowerCase());
            item.put("quantity", pi.getQuantity());
            String type = pi.getItemType().name().toLowerCase();
            String name = itemNames.get(type).getOrDefault(pi.getItemId(), "未知物品");
            item.put("name", name);
            item.put("unit", itemUnits.get(type).getOrDefault(pi.getItemId(), "个"));
            result.add(item);
            
            System.out.println("=== Added item: itemId=" + pi.getItemId() + ", type=" + type + ", quantity=" + pi.getQuantity() + ", name=" + name);
        }

        System.out.println("=== Returning " + result.size() + " items");
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
            
            // Partial update: frontend (e.g. Player.vue profile save) only sends edited fields.
            // Do not overwrite with null — would violate NOT NULL on faction / clear job & skill.
            if (player.getName() != null) {
                existingPlayer.setName(player.getName());
            }
            if (player.getIsWeak() != null) {
                existingPlayer.setIsWeak(player.getIsWeak());
            }
            if (player.getIsOverworked() != null) {
                existingPlayer.setIsOverworked(player.getIsOverworked());
            }
            if (player.getIsInjured() != null) {
                existingPlayer.setIsInjured(player.getIsInjured());
            }
            if (player.getJobId() != null) {
                existingPlayer.setJobId(player.getJobId());
            }
            if (player.getSkillId() != null) {
                existingPlayer.setSkillId(player.getSkillId());
            }
            if (player.getFaction() != null) {
                existingPlayer.setFaction(player.getFaction());
            }
            
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

    @Transactional
    public Map<String, Object> migrateFaction() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Player> players = playerRepository.findAll();
            int updatedCount = 0;
            
            for (Player player : players) {
                if (player.getFaction() != null && player.getFaction().name().equals("杀戮者")) {
                    player.setFaction(Player.Faction.天灾使者);
                    playerRepository.save(player);
                    updatedCount++;
                }
            }
            
            result.put("success", true);
            result.put("message", "阵营迁移完成");
            result.put("updatedCount", updatedCount);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "迁移失败: " + e.getMessage());
        }
        
        return result;
    }

    public Map<String, Object> getPlayerDetails(Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Player player = playerRepository.findById(id).orElse(null);
            if (player == null) {
                result.put("success", false);
                result.put("message", "玩家不存在");
                return result;
            }

            result.put("success", true);
            result.put("id", player.getId());
            result.put("name", player.getName());
            result.put("isWeak", player.getIsWeak());
            result.put("isOverworked", player.getIsOverworked());
            result.put("isInjured", player.getIsInjured());
            result.put("faction", player.getFaction() != null ? player.getFaction().name() : null);
            result.put("jobId", player.getJobId());
            result.put("skillId", player.getSkillId());
            result.put("createdAt", player.getCreatedAt());
            result.put("updatedAt", player.getUpdatedAt());

            if (player.getJobId() != null) {
                Job job = jobRepository.findById(player.getJobId()).orElse(null);
                if (job != null) {
                    result.put("job", job.getName());
                    result.put("jobSkills", job.getSkills());
                    result.put("jobDescription", job.getDescription());
                }
            }

            if (player.getSkillId() != null) {
                Skill skill = skillRepository.findById(player.getSkillId()).orElse(null);
                if (skill != null) {
                    result.put("personalSkill", skill.getName());
                    result.put("personalSkillFunction", skill.getFunction());
                }
            }

            String avatar = "🧑";
            if (player.getFaction() != null) {
                switch (player.getFaction()) {
                    case 统治者: avatar = "⚔️"; break;
                    case 反叛者: avatar = "🔮"; break;
                    case 冒险者: avatar = "🗡️"; break;
                    case 天灾使者: avatar = "✨"; break;
                    case 平民: avatar = "🏹"; break;
                }
            }
            result.put("avatar", avatar);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取玩家信息失败: " + e.getMessage());
        }
        
        return result;
    }
}
