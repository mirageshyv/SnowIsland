package com.example.snowisland.config;

import com.example.snowisland.entity.User;
import com.example.snowisland.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否已有用户
        if (userRepository.count() > 0) {
            logger.info("数据库已有 {} 个用户，跳过初始化", userRepository.count());
            return;
        }

        logger.info("开始初始化默认用户账号...");

        try {
            // 创建DM账号
            User dm = new User();
            dm.setUsername("dm");
            dm.setPassword("dm123");
            dm.setRole(User.Role.DM);
            dm.setPlayerId(null);
            dm.setStatus(1);
            userRepository.save(dm);
            logger.info("创建DM账号: dm / dm123");

            // 创建测试玩家账号
            User player = new User();
            player.setUsername("player");
            player.setPassword("test123");
            player.setRole(User.Role.PLAYER);
            player.setPlayerId(1); // 假设玩家ID为1
            player.setStatus(1);
            userRepository.save(player);
            logger.info("创建玩家账号: player / test123");

            logger.info("默认用户账号初始化完成，共创建 2 个用户");
        } catch (Exception e) {
            logger.error("初始化默认用户账号失败: {}", e.getMessage(), e);
        }
    }
}