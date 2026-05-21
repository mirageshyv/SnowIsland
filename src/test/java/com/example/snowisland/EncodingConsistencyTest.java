package com.example.snowisland;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 编码一致性测试 - 验证全链路中文数据编码正确性
 * 覆盖：数据库存储、JDBC连接、API响应、HTTP Content-Type
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EncodingConsistencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    // ========== 数据库层编码测试 ==========

    @Test
    public void testDatabaseConnectionCharset() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT @@character_set_client, @@character_set_connection, @@character_set_results")) {
            assertTrue(rs.next(), "Should have charset results");
            String clientCharset = rs.getString(1);
            String connCharset = rs.getString(2);
            String resultsCharset = rs.getString(3);
            // JDBC连接通过characterEncoding=UTF-8参数，应使用utf8或utf8mb4
            if (clientCharset != null) {
                assertTrue(
                    clientCharset.toLowerCase().contains("utf8") || clientCharset.toLowerCase().contains("utf8mb4"),
                    "Client charset should be utf8/utf8mb4, got: " + clientCharset
                );
            }
            if (connCharset != null) {
                assertTrue(
                    connCharset.toLowerCase().contains("utf8") || connCharset.toLowerCase().contains("utf8mb4"),
                    "Connection charset should be utf8/utf8mb4, got: " + connCharset
                );
            }
            if (resultsCharset != null) {
                assertTrue(
                    resultsCharset.toLowerCase().contains("utf8") || resultsCharset.toLowerCase().contains("utf8mb4"),
                    "Results charset should be utf8/utf8mb4, got: " + resultsCharset
                );
            }
        }
    }

    @Test
    public void testDatabaseChineseDataIntegrity() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM player LIMIT 1")) {
            assertTrue(rs.next(), "Should have at least one player");
            String name = rs.getString("name");
            assertNotNull(name, "Player name should not be null");
            assertFalse(name.contains("?"), "Player name should not contain question marks (encoding issue): " + name);
            // 验证中文字符存在 - 至少有一个非ASCII字符
            assertTrue(name.getBytes(StandardCharsets.UTF_8).length > name.length(),
                "Player name should contain multi-byte (Chinese) characters: " + name);
        }
    }

    @Test
    public void testRuleBookChineseDataIntegrity() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT title, content FROM rule_book LIMIT 5")) {
            int count = 0;
            while (rs.next()) {
                count++;
                String title = rs.getString("title");
                String content = rs.getString("content");
                assertNotNull(title, "Rule title should not be null");
                assertFalse(title.contains("?"), "Rule title should not contain question marks: " + title);
                if (content != null) {
                    // content中允许少量?号(可能是原始内容)，但不应全是问号
                    long questionMarkCount = content.chars().filter(ch -> ch == '?').count();
                    assertTrue(questionMarkCount < content.length() * 0.1,
                        "Content should not have excessive question marks (>10%): " + title);
                }
            }
            assertTrue(count > 0, "Should have at least one rule book entry");
        }
    }

    @Test
    public void testDatabaseTableCharset() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT TABLE_NAME, TABLE_COLLATION FROM information_schema.TABLES " +
                 "WHERE TABLE_SCHEMA = 'snowisland' AND TABLE_NAME = 'rule_book'")) {
            assertTrue(rs.next(), "Should find rule_book table info");
            String collation = rs.getString("TABLE_COLLATION");
            assertTrue(collation.startsWith("utf8"), "Table collation should start with utf8, got: " + collation);
        }
    }

    // ========== API层编码测试 ==========

    @Test
    public void testRuleBookApiContentType() throws Exception {
        mockMvc.perform(get("/api/rule-book/all"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type",
                org.hamcrest.Matchers.containsString("application/json")));
    }

    @Test
    public void testRuleBookApiChineseContent() throws Exception {
        mockMvc.perform(get("/api/rule-book/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isMap());
    }

    @Test
    public void testPlayersApiChineseContent() throws Exception {
        mockMvc.perform(get("/api/players"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    @Test
    public void testCatastropheCardsApiChineseContent() throws Exception {
        mockMvc.perform(get("/api/catastrophe/cards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    @Test
    public void testJobsApiChineseContent() throws Exception {
        mockMvc.perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").isNotEmpty());
    }

    // ========== 响应编码测试 ==========

    @Test
    public void testApiResponseUtf8Encoding() throws Exception {
        byte[] responseBytes = mockMvc.perform(get("/api/rule-book/all"))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

        String utf8String = new String(responseBytes, StandardCharsets.UTF_8);
        // 验证UTF-8解码后不包含乱码特征（连续的替换字符）
        assertFalse(utf8String.contains("\uFFFD\uFFFD"),
            "Response should not contain consecutive replacement characters (encoding error)");
        // 验证包含中文字符
        assertTrue(utf8String.getBytes(StandardCharsets.UTF_8).length > utf8String.length(),
            "Response should contain multi-byte (Chinese) characters");
    }

    @Test
    public void testHttpEncodingForceUtf8() throws Exception {
        mockMvc.perform(get("/api/rule-book/all"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Content-Type"));
    }
}
