package com.example.snowisland.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AiDialogueServiceTest {

    private final AiDialogueService service = new AiDialogueService();

    @Test
    public void parseOpenAiFormatReply() {
        String json = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"{\\\"r\\\":\\\"风雪太大了。\\\",\\\"f\\\":1}\"}}]}";
        assertEquals("{\"r\":\"风雪太大了。\",\"f\":1}", service.parseResponse(json));
    }

    @Test
    public void parseNpcTurnJson() {
        AiDialogueService.NpcTurn turn = service.parseNpcTurnPayload("{\"r\":\"雪还在下，猎物不多。\",\"f\":1}");
        assertEquals("雪还在下，猎物不多。", turn.reply);
        assertEquals(1, turn.favorChange);
    }

    @Test
    public void parseNpcTurnClampsFavor() {
        AiDialogueService.NpcTurn turn = service.parseNpcTurnPayload("{\"r\":\"滚。\",\"f\":-20}");
        assertEquals(-5, turn.favorChange);
    }

    @Test
    public void parseNpcTurnRejectsZeroFavor() {
        AiDialogueService.NpcTurn turn = service.parseNpcTurnPayload("{\"r\":\"嗯。\",\"f\":0}");
        assertEquals(1, turn.favorChange);
        AiDialogueService.NpcTurn missing = service.parseNpcTurnPayload("{\"r\":\"风雪太大了。\"}");
        assertEquals(1, missing.favorChange);
    }

    @Test
    public void parseMessageFormatReply() {
        String json = "{\"output\":{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"风雪太大了。\"}}]}}";
        assertEquals("风雪太大了。", service.parseResponse(json));
    }

    @Test
    public void parseTextFormatReply() {
        String json = "{\"output\":{\"text\":\"先把火生起来。\"}}";
        assertEquals("先把火生起来。", service.parseResponse(json));
    }

    @Test
    public void parseArrearageThrows() {
        String json = "{\"code\":\"Arrearage\",\"message\":\"Access denied\"}";
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.parseResponse(json));
        assertTrue(ex.getMessage().contains("Arrearage"));
    }

    @Test
    public void fallbackNoLongerClaimsConfusion() {
        String reply = service.generateFallbackReply("艾拉", "猎人", "最近怎么样？", 0);
        assertFalse(reply.contains("不太明白"));
        assertTrue(reply.contains("冷") || reply.contains("岛"));
    }

    @Test
    public void spokenChatPromptHasNoCatalog() {
        String prompt = service.buildSpokenSystemPrompt("艾拉", "猎人", "寡言", "短句", "守林人", "", 10, "中立");
        assertTrue(prompt.contains("勿交易细节"));
        assertTrue(prompt.contains("\"r\""));
        assertTrue(prompt.contains("\"f\""));
        assertTrue(prompt.contains("禁止0"));
        assertFalse(prompt.contains("目录"));
        assertFalse(prompt.toLowerCase().contains("surplus"));
        assertFalse(prompt.contains("背包"));
    }

    @Test
    public void parseTradeProposalJson() {
        AiDialogueService.TradeProposalPayload payload = service.parseTradeProposalPayload(
                "{\"give\":[{\"t\":\"item\",\"id\":10,\"q\":1}],\"take\":[{\"t\":\"material\",\"id\":5,\"q\":2}],\"remark\":\"用酒换口粮\"}");
        assertNotNull(payload);
        assertEquals(1, payload.give.size());
        assertEquals("item", payload.give.get(0).get("t"));
        assertEquals(10, ((Number) payload.give.get(0).get("id")).intValue());
        assertEquals(1, payload.take.size());
        assertEquals(5, ((Number) payload.take.get(0).get("id")).intValue());
        assertEquals("用酒换口粮", payload.remark);
        assertNull(payload.want);
    }

    @Test
    public void parseTradeProposalWantFlag() {
        AiDialogueService.TradeProposalPayload no = service.parseTradeProposalPayload(
                "{\"want\":false,\"give\":[],\"take\":[],\"remark\":\"不要手电筒\"}");
        assertEquals(Boolean.FALSE, no.want);
        AiDialogueService.TradeProposalPayload yes = service.parseTradeProposalPayload(
                "{\"want\":true,\"give\":[{\"t\":\"material\",\"id\":3,\"q\":1}],\"take\":[{\"t\":\"item\",\"id\":2,\"q\":1}],\"remark\":\"留下手电\"}");
        assertEquals(Boolean.TRUE, yes.want);
        assertEquals(2, ((Number) yes.take.get(0).get("id")).intValue());
    }

    @Test
    public void parseTradeProposalIgnoresSpokenTurnJson() {
        assertNull(service.parseTradeProposalPayload("not json"));
        AiDialogueService.TradeProposalPayload chat = service.parseTradeProposalPayload("{\"r\":\"雪还在下。\",\"f\":1}");
        assertNotNull(chat);
        assertTrue(chat.give.isEmpty());
        assertTrue(chat.take.isEmpty());
    }

}
