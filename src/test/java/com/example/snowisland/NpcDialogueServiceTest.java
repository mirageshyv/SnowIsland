package com.example.snowisland;

import com.example.snowisland.service.NpcDialogueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class NpcDialogueServiceTest {

    @Autowired
    private NpcDialogueService npcDialogueService;

    @Test
    public void testGetFavorLevel() {
        assertEquals("敌视", NpcDialogueService.getFavorLevel(-100));
        assertEquals("敌视", NpcDialogueService.getFavorLevel(-60));
        assertEquals("冷漠", NpcDialogueService.getFavorLevel(-59));
        assertEquals("冷漠", NpcDialogueService.getFavorLevel(-20));
        assertEquals("中立", NpcDialogueService.getFavorLevel(-19));
        assertEquals("中立", NpcDialogueService.getFavorLevel(20));
        assertEquals("友善", NpcDialogueService.getFavorLevel(21));
        assertEquals("友善", NpcDialogueService.getFavorLevel(60));
        assertEquals("亲近", NpcDialogueService.getFavorLevel(61));
        assertEquals("亲近", NpcDialogueService.getFavorLevel(100));
        assertEquals("中立", NpcDialogueService.getFavorLevel(null));
    }

    @Test
    public void testGetFavorColor() {
        assertEquals("#ef4444", NpcDialogueService.getFavorColor(-100));
        assertEquals("#ef4444", NpcDialogueService.getFavorColor(-60));
        assertEquals("#f59e0b", NpcDialogueService.getFavorColor(-59));
        assertEquals("#f59e0b", NpcDialogueService.getFavorColor(-20));
        assertEquals("#6b7280", NpcDialogueService.getFavorColor(-19));
        assertEquals("#6b7280", NpcDialogueService.getFavorColor(20));
        assertEquals("#3b82f6", NpcDialogueService.getFavorColor(21));
        assertEquals("#3b82f6", NpcDialogueService.getFavorColor(60));
        assertEquals("#22c55e", NpcDialogueService.getFavorColor(61));
        assertEquals("#22c55e", NpcDialogueService.getFavorColor(100));
        assertEquals("#6b7280", NpcDialogueService.getFavorColor(null));
    }

    @Test
    public void testGetAllNpcsWithFavor() {
        List<Map<String, Object>> npcs = npcDialogueService.getAllNpcsWithFavor(1);
        assertNotNull(npcs);
        assertTrue(npcs.size() >= 0);
        for (Map<String, Object> npc : npcs) {
            assertNotNull(npc.get("id"));
            assertNotNull(npc.get("name"));
            assertNotNull(npc.get("job"));
            assertNotNull(npc.get("gender"));
        }
    }

    @Test
    public void testGetNpcDetail() {
        Map<String, Object> result = npcDialogueService.getNpcDetail(1, 1);
        assertTrue((Boolean) result.get("success"));
        Map<String, Object> npc = (Map<String, Object>) result.get("npc");
        assertNotNull(npc);
        assertNotNull(npc.get("id"));
        assertNotNull(npc.get("name"));
    }

    @Test
    public void testGetNpcDetailNotFound() {
        Map<String, Object> result = npcDialogueService.getNpcDetail(99999, 1);
        assertFalse((Boolean) result.get("success"));
        assertEquals("NPC不存在", result.get("message"));
    }

    @Test
    public void testSendMessageBasic() {
        Map<String, Object> result = npcDialogueService.sendMessage(1, 1, "你好");
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("reply"));
        assertTrue(result.get("reply").toString().length() > 0);
    }

    @Test
    public void testSendMessageWithFavorChange() {
        Map<String, Object> result = npcDialogueService.sendMessage(1, 1, "谢谢你");
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("favorChange"));
        assertTrue((Integer) result.get("favorChange") > 0);
    }

    @Test
    public void testSendMessageHello() {
        Map<String, Object> result = npcDialogueService.sendMessage(1, 1, "hello");
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("reply"));
    }

    @Test
    public void testSendMessageNullNpc() {
        Map<String, Object> result = npcDialogueService.sendMessage(1, 99999, "你好");
        assertFalse((Boolean) result.get("success"));
        assertEquals("NPC不存在", result.get("message"));
    }

    @Test
    public void testSetFavorValid() {
        Map<String, Object> result = npcDialogueService.setFavor(1, 1, 50);
        assertTrue((Boolean) result.get("success"));
        assertEquals(50, result.get("favorValue"));
        assertEquals("友善", result.get("favorLevel"));
    }

    @Test
    public void testSetFavorMin() {
        Map<String, Object> result = npcDialogueService.setFavor(1, 1, -100);
        assertTrue((Boolean) result.get("success"));
        assertEquals(-100, result.get("favorValue"));
        assertEquals("敌视", result.get("favorLevel"));
    }

    @Test
    public void testSetFavorMax() {
        Map<String, Object> result = npcDialogueService.setFavor(1, 1, 100);
        assertTrue((Boolean) result.get("success"));
        assertEquals(100, result.get("favorValue"));
        assertEquals("亲近", result.get("favorLevel"));
    }

    @Test
    public void testSetFavorOutOfRangeLow() {
        Map<String, Object> result = npcDialogueService.setFavor(1, 1, -101);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    public void testSetFavorOutOfRangeHigh() {
        Map<String, Object> result = npcDialogueService.setFavor(1, 1, 101);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    public void testGetPlayerFavors() {
        List<Map<String, Object>> favors = npcDialogueService.getPlayerFavors(1);
        assertNotNull(favors);
        assertTrue(favors.size() >= 0);
        for (Map<String, Object> favor : favors) {
            assertNotNull(favor.get("npcId"));
            assertNotNull(favor.get("playerId"));
            assertNotNull(favor.get("favorValue"));
            assertNotNull(favor.get("favorLevel"));
        }
    }

    @Test
    public void testGetDialogueHistory() {
        npcDialogueService.sendMessage(1, 1, "测试对话");
        List<Map<String, Object>> history = npcDialogueService.getDialogueHistory(1, 1);
        assertNotNull(history);
        assertTrue(history.size() >= 1);
        Map<String, Object> lastDialogue = history.get(history.size() - 1);
        assertEquals("测试对话", lastDialogue.get("playerMessage"));
        assertNotNull(lastDialogue.get("npcReply"));
    }

    @Test
    public void testFavorClamping() {
        npcDialogueService.setFavor(1, 1, 100);
        npcDialogueService.sendMessage(1, 1, "谢谢你");
        Map<String, Object> result = npcDialogueService.sendMessage(1, 1, "谢谢你");
        Integer newFavor = (Integer) result.get("newFavor");
        assertTrue(newFavor <= 100);
        assertTrue(newFavor >= -100);
    }

    @Test
    public void testFavorChangeBasedOnMessage() {
        npcDialogueService.setFavor(1, 1, 0);
        
        Map<String, Object> thanks = npcDialogueService.sendMessage(1, 1, "谢谢你");
        assertEquals(5, thanks.get("favorChange"));
        
        Map<String, Object> hello = npcDialogueService.sendMessage(1, 1, "你好");
        assertEquals(2, hello.get("favorChange"));
        
        Map<String, Object> bye = npcDialogueService.sendMessage(1, 1, "再见");
        assertEquals(1, bye.get("favorChange"));
        
        Map<String, Object> neutral = npcDialogueService.sendMessage(1, 1, "随便说点什么");
        assertEquals(0, neutral.get("favorChange"));
    }
}