package com.example.snowisland.controller;

import com.example.snowisland.entity.User;
import com.example.snowisland.repository.UserRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private com.example.snowisland.service.AuthService authService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return authService.login(username, password);
    }

    /**
     * 初始化默认账号（仅当数据库为空时）
     */
    @PostMapping("/init")
    @Transactional
    public Map<String, Object> initDefaultUsers() {
        Map<String, Object> result = new HashMap<>();

        // 检查是否已有用户
        if (userRepository.count() > 0) {
            result.put("success", false);
            result.put("message", "数据库已有用户，无需初始化");
            return result;
        }

        try {
            List<Map<String, Object>> createdUsers = new ArrayList<>();

            // 创建DM账号
            User dm1 = new User();
            dm1.setUsername("dm");
            dm1.setPassword("dm123");
            dm1.setRole(User.Role.DM);
            dm1.setPlayerId(null);
            dm1.setStatus(1);
            userRepository.save(dm1);
            createdUsers.add(createUserInfo(dm1, "DM管理员"));

            User dm2 = new User();
            dm2.setUsername("dm2");
            dm2.setPassword("dm123");
            dm2.setRole(User.Role.DM);
            dm2.setPlayerId(null);
            dm2.setStatus(1);
            userRepository.save(dm2);
            createdUsers.add(createUserInfo(dm2, "DM管理员"));

            // 创建玩家测试账号
            if (playerRepository.count() > 0) {
                List<?> players = playerRepository.findAll();
                int count = 0;
                for (Object p : players) {
                    if (count >= 5) break;
                    try {
                        Object playerId = p.getClass().getMethod("getId").invoke(p);
                        Object playerName = p.getClass().getMethod("getName").invoke(p);

                        User playerUser = new User();
                        String username = "player" + (count + 1);
                        playerUser.setUsername(username);
                        playerUser.setPassword("test123");
                        playerUser.setRole(User.Role.PLAYER);
                        playerUser.setPlayerId((Integer) playerId);
                        playerUser.setStatus(1);
                        userRepository.save(playerUser);
                        createdUsers.add(createUserInfo(playerUser, playerName.toString()));
                        count++;
                    } catch (Exception e) {
                        logger.warn("创建玩家账号失败: {}", e.getMessage());
                    }
                }
            }

            logger.info("默认账号初始化成功，共创建 {} 个用户", createdUsers.size());

            result.put("success", true);
            result.put("message", "账号初始化成功");
            result.put("users", createdUsers);
            return result;

        } catch (Exception e) {
            logger.error("账号初始化失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "初始化失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Transactional
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        if (username == null || newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名和新密码不能为空");
            return result;
        }

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        try {
            User user = userOpt.get();
            user.setPassword(newPassword);
            userRepository.save(user);

            logger.info("用户 {} 密码重置成功", username);
            result.put("success", true);
            result.put("message", "密码重置成功");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取所有用户名（仅DM）
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(@RequestParam(required = false) String role) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> users = new ArrayList<>();

        try {
            for (User user : userRepository.findAll()) {
                if (role != null && !role.isEmpty()) {
                    if (!user.getRole().name().equalsIgnoreCase(role)) {
                        continue;
                    }
                }
                users.add(createUserInfo(user, null));
            }
            result.put("success", true);
            result.put("users", users);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

    private Map<String, Object> createUserInfo(User user, String displayName) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("role", user.getRole().name());
        info.put("playerId", user.getPlayerId());
        if (displayName != null) {
            info.put("displayName", displayName);
        }
        return info;
    }
}