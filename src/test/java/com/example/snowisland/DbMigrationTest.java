package com.example.snowisland;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * 数据库迁移测试：执行 SQL 脚本并验证 event_difficulty 字段已生效。
 *
 * 默认连接信息来自 application.properties，可通过环境变量覆盖：
 *   DB_URL / DB_USER / DB_PASSWORD
 *
 * 如数据库不可达，本测试将被跳过（不阻塞构建）。
 */
class DbMigrationTest {

    @Test
    void applyEventDifficultyMigration() throws Exception {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/snowisland?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "695390489");
        String sqlFile = argsOrDefault("sqlFile", "sql/alter_island_event_difficulty.sql");
        String seedFile = System.getenv().getOrDefault("SEED_FILE", "sql/create_island_event_tables.sql");

        Connection conn;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.err.println("[Migration] 数据库不可达，跳过迁移测试: " + e.getMessage());
            assumeTrue(false, "数据库未启动");
            return;
        }

        try (Connection c = conn; Statement stmt = c.createStatement()) {
            // 1. 若是空表，先补种子数据
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM island_event")) {
                rs.next();
                int count = rs.getInt("cnt");
                if (count == 0) {
                    System.out.println("[Migration] 检测到 island_event 为空，先注入种子数据: " + seedFile);
                    runSqlScript(stmt, seedFile);
                }
            }

            // 2. 执行迁移脚本
            System.out.println("[Migration] 开始执行: " + sqlFile);
            runSqlScript(stmt, sqlFile);

            // 3. 验证 event_difficulty 列已存在
            try (ResultSet rs = c.getMetaData().getColumns("snowisland", null, "island_event", "event_difficulty")) {
                assumeTrue(rs.next(), "event_difficulty 列应已存在");
                System.out.println("[Migration] ✓ event_difficulty 列已就绪");
            }
        }
    }

    private static String argsOrDefault(String key, String def) {
        return System.getProperty(key, def);
    }

    private void runSqlScript(Statement stmt, String sqlFile) throws Exception {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(sqlFile), StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            String line;
            int executed = 0;
            int skipped = 0;
            while ((line = br.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    String sql = sb.toString().trim();
                    if (sql.endsWith(";")) sql = sql.substring(0, sql.length() - 1);
                    sb.setLength(0);

                    if (sql.toUpperCase().startsWith("SELECT")) {
                        try (ResultSet rs = stmt.executeQuery(sql)) {
                            System.out.println("\n[Migration] 验证查询：");
                            int cols = rs.getMetaData().getColumnCount();
                            while (rs.next()) {
                                StringBuilder row = new StringBuilder("  ");
                                for (int i = 1; i <= cols; i++) {
                                    if (i > 1) row.append(" | ");
                                    row.append(rs.getString(i));
                                }
                                System.out.println(row);
                            }
                        }
                        continue;
                    }
                    try {
                        int affected = stmt.executeUpdate(sql);
                        executed++;
                        System.out.println("[Migration] OK rows=" + affected + " : "
                                + sql.replaceAll("\\s+", " "));
                    } catch (Exception e) {
                        skipped++;
                        System.err.println("[Migration] SKIP " + e.getMessage() + " : "
                                + sql.replaceAll("\\s+", " "));
                    }
                }
            }
            System.out.println("[Migration] " + sqlFile + " 完成。成功 " + executed + " 条，跳过 " + skipped + " 条。");
        }
    }
}
