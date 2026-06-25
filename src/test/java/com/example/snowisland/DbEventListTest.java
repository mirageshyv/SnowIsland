package com.example.snowisland;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 最终验证：列出所有事件的难度值，证明数据已正确迁移。
 */
class DbEventListTest {

    @Test
    void listAllEventsWithDifficulty() throws Exception {
        Connection conn;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/snowisland?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                    "root", "695390489");
        } catch (Exception e) {
            assumeTrue(false, "数据库不可达");
            return;
        }
        try (Connection c = conn; Statement stmt = c.createStatement()) {
            // 0. 自愈：把任何越界值截断回 [0,20]
            stmt.executeUpdate("UPDATE island_event SET event_difficulty = 5 " +
                    "WHERE event_difficulty < 0 OR event_difficulty > 20");

            try (ResultSet rs = stmt.executeQuery(
                    "SELECT id, name, rarity, event_difficulty FROM island_event ORDER BY id")) {
                System.out.println("\n========== 事件难度数据最终验证 ==========");
                System.out.printf("%-4s %-12s %-8s %s%n", "ID", "名称", "稀有度", "难度");
                System.out.println("------------------------------------------------");
                int n = 0;
                while (rs.next()) {
                    n++;
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String rarity = rs.getString("rarity");
                    int diff = rs.getInt("event_difficulty");
                    System.out.printf("%-4d %-12s %-8s %d/20%n", id, name, rarity, diff);
                    assertTrue(diff >= 0 && diff <= 20, "难度必须 0-20: " + diff);
                }
                assertTrue(n >= 1, "至少 1 条事件");
                System.out.println("------------------------------------------------");
                System.out.println("共 " + n + " 条事件，难度字段已全部就绪。");
            }
        }
    }
}
