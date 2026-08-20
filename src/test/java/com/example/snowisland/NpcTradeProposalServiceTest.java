package com.example.snowisland;

import com.example.snowisland.entity.NpcFavor;
import com.example.snowisland.entity.NpcItem;
import com.example.snowisland.entity.NpcTradeProposal;
import com.example.snowisland.entity.Player;
import com.example.snowisland.entity.PlayerItem;
import com.example.snowisland.entity.TradeItem.ItemType;
import com.example.snowisland.repository.NpcFavorRepository;
import com.example.snowisland.repository.NpcItemRepository;
import com.example.snowisland.repository.NpcTradeProposalRepository;
import com.example.snowisland.repository.PlayerItemRepository;
import com.example.snowisland.repository.PlayerRepository;
import com.example.snowisland.service.AiDialogueService;
import com.example.snowisland.service.GameStateService;
import com.example.snowisland.service.NpcInventoryService;
import com.example.snowisland.service.NpcTradeProposalService;
import com.example.snowisland.util.ItemCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
public class NpcTradeProposalServiceTest {

    private static final int TEST_NPC_ID = 1;
    private static final int ROPE_ID = 3;

    private int playerId;

    @Autowired
    private NpcTradeProposalService proposalService;

    @Autowired
    private NpcTradeProposalRepository proposalRepository;

    @Autowired
    private NpcItemRepository npcItemRepository;

    @Autowired
    private PlayerItemRepository playerItemRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private NpcFavorRepository npcFavorRepository;

    @Autowired
    private GameStateService gameStateService;

    @Autowired
    private NpcInventoryService npcInventoryService;

    @MockBean
    private AiDialogueService aiDialogueService;

    @BeforeEach
    public void setUp() {
        when(aiDialogueService.generateTradeProposal(any(), any(), any(), anyBoolean(),
                any(), any(), any(), anyInt(), anyInt(), nullable(String.class), anyInt())).thenReturn(null);
        playerId = playerRepository.findAll().stream()
                .map(Player::getId)
                .findFirst()
                .orElseGet(() -> {
                    Player created = new Player();
                    created.setName("proposal-test");
                    created.setFaction(Player.Faction.平民);
                    created.setJobId(1);
                    return playerRepository.save(created).getId();
                });
        playerItemRepository.deleteByPlayerId(playerId);
        npcItemRepository.deleteByNpcId(TEST_NPC_ID);
        int day = gameStateService.getCurrentDay();
        proposalRepository.deleteAll(proposalRepository.findByNpcIdAndPlayerIdAndGameDay(TEST_NPC_ID, playerId, day));
        setFavor(40);
    }

    @Test
    public void shortfallWithSurplusCreatesOneProposalPerDay() {
        seedNpcStock(0, 0, 0, 2);
        Map<String, Object> first = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID);
        assertTrue(Boolean.TRUE.equals(first.get("tradeProposed")));
        NpcTradeProposal row = proposalRepository.findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
                TEST_NPC_ID, playerId, gameStateService.getCurrentDay(), NpcTradeProposal.STATUS_OPEN).orElse(null);
        assertNotNull(row);
        assertEquals(NpcTradeProposal.STATUS_OPEN, row.getStatus());
        assertTrue(row.getTakeItems().contains("\"id\":" + ItemCatalog.FOOD_MATERIAL_ID)
                || row.getTakeItems().contains("\"itemId\":" + ItemCatalog.FOOD_MATERIAL_ID));
        assertTrue(row.getGiveItems().contains("\"id\":" + ROPE_ID)
                || row.getGiveItems().contains("\"itemId\":" + ROPE_ID));

        Map<String, Object> second = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID);
        assertFalse(Boolean.TRUE.equals(second.get("tradeProposed")));
        assertEquals(1, proposalRepository.findByNpcIdAndStatus(TEST_NPC_ID, NpcTradeProposal.STATUS_OPEN).stream()
                .filter(p -> playerId == p.getPlayerId()).count());
    }

    @Test
    public void survivingWithoutLlmSkipsOpportunistic() {
        seedNpcStock(20, 40, 2, 2);
        Map<String, Object> result = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID);
        assertFalse(Boolean.TRUE.equals(result.get("tradeProposed")));
        assertFalse(proposalRepository.findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
                TEST_NPC_ID, playerId, gameStateService.getCurrentDay(), NpcTradeProposal.STATUS_OPEN).isPresent());
    }

    @Test
    public void openGiveIsReservedFromSellableSurplus() {
        seedNpcStock(0, 0, 0, 2);
        saveOpenProposal("[{\"t\":\"material\",\"id\":3,\"q\":2,\"name\":\"绳索\"}]",
                "[{\"t\":\"material\",\"id\":5,\"q\":2,\"name\":\"食物\"}]");
        assertEquals(2, proposalService.reservedGiveQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        assertEquals(0, npcInventoryService.sellableQuantity(
                TEST_NPC_ID, ItemType.material, ROPE_ID, 2, 25));
    }

    @Test
    public void acceptAbortsWhenPlayerLacksTakeItems() {
        seedNpcStock(0, 0, 0, 2);
        NpcTradeProposal proposal = saveOpenProposal(
                "[{\"t\":\"material\",\"id\":3,\"q\":1,\"name\":\"绳索\"}]",
                "[{\"t\":\"material\",\"id\":5,\"q\":2,\"name\":\"食物\"}]");
        Map<String, Object> result = proposalService.accept(playerId, TEST_NPC_ID);
        assertFalse(Boolean.TRUE.equals(result.get("success")));
        assertTrue(String.valueOf(result.get("message")).contains("缺少食物"));
        assertEquals(NpcTradeProposal.STATUS_OPEN, proposalRepository.findById(proposal.getId()).get().getStatus());
        assertEquals(2, npcInventoryService.getQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        assertEquals(0, playerQty(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID));
        assertEquals(0, playerQty(ItemType.material, ROPE_ID));
    }

    @Test
    public void acceptTransfersBothSidesWhenStockIsFull() {
        seedNpcStock(0, 0, 0, 2);
        saveOpenProposal(
                "[{\"t\":\"material\",\"id\":3,\"q\":1,\"name\":\"绳索\"}]",
                "[{\"t\":\"material\",\"id\":5,\"q\":2,\"name\":\"食物\"}]");
        addPlayerItem(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID, 2);
        Map<String, Object> result = proposalService.accept(playerId, TEST_NPC_ID);
        assertTrue(Boolean.TRUE.equals(result.get("success")), String.valueOf(result.get("message")));
        assertEquals(1, npcInventoryService.getQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        assertEquals(2, npcInventoryService.getQuantity(TEST_NPC_ID, ItemType.material, ItemCatalog.FOOD_MATERIAL_ID));
        assertEquals(0, playerQty(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID));
        assertEquals(1, playerQty(ItemType.material, ROPE_ID));
        assertEquals(NpcTradeProposal.STATUS_COMPLETED, proposalRepository
                .findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
                        TEST_NPC_ID, playerId, gameStateService.getCurrentDay(), NpcTradeProposal.STATUS_COMPLETED)
                .get().getStatus());
    }

    @Test
    public void rejectDoesNotMoveItems() {
        seedNpcStock(0, 0, 0, 2);
        saveOpenProposal(
                "[{\"t\":\"material\",\"id\":3,\"q\":1,\"name\":\"绳索\"}]",
                "[{\"t\":\"material\",\"id\":5,\"q\":2,\"name\":\"食物\"}]");
        addPlayerItem(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID, 2);
        Map<String, Object> result = proposalService.reject(playerId, TEST_NPC_ID);
        assertTrue(Boolean.TRUE.equals(result.get("success")));
        assertEquals(2, npcInventoryService.getQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        assertEquals(2, playerQty(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID));
        assertEquals(0, playerQty(ItemType.material, ROPE_ID));
        assertEquals(NpcTradeProposal.STATUS_REJECTED, proposalRepository
                .findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
                        TEST_NPC_ID, playerId, gameStateService.getCurrentDay(), NpcTradeProposal.STATUS_REJECTED)
                .get().getStatus());
    }

    @Test
    public void rejectAllowsANewProposalTheSameDay() {
        seedNpcStock(0, 0, 0, 2);
        Map<String, Object> first = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID, "有什么任务吗？");
        assertTrue(Boolean.TRUE.equals(first.get("tradeProposed")));
        Map<String, Object> rejected = proposalService.reject(playerId, TEST_NPC_ID);
        assertTrue(Boolean.TRUE.equals(rejected.get("success")));
        Map<String, Object> second = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID, "那我可以给你渔网");
        assertTrue(Boolean.TRUE.equals(second.get("tradeProposed")));
        assertEquals(1, proposalRepository.findByNpcIdAndStatus(TEST_NPC_ID, NpcTradeProposal.STATUS_OPEN).stream()
                .filter(p -> playerId == p.getPlayerId()).count());
    }

    @Test
    public void friendlyChatOfferCanAddTakeBesideSurvival() {
        java.util.List<Map<String, Object>> give = new java.util.ArrayList<>();
        Map<String, Object> giveLine = new java.util.HashMap<>();
        giveLine.put("t", "material");
        giveLine.put("id", ROPE_ID);
        giveLine.put("q", 1);
        give.add(giveLine);
        java.util.List<Map<String, Object>> take = new java.util.ArrayList<>();
        Map<String, Object> takeLine = new java.util.HashMap<>();
        takeLine.put("t", "item");
        takeLine.put("id", 2);
        takeLine.put("q", 1);
        take.add(takeLine);
        when(aiDialogueService.generateTradeProposal(any(), any(), any(), anyBoolean(),
                any(), any(), any(), anyInt(), anyInt(), nullable(String.class), anyInt()))
                .thenReturn(new AiDialogueService.TradeProposalPayload(give, take, "手电筒留下，绳索给你。", true));
        seedNpcStock(0, 0, 0, 2);
        Map<String, Object> result = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID, "我可以给你手电筒");
        assertTrue(Boolean.TRUE.equals(result.get("tradeProposed")));
        NpcTradeProposal row = proposalRepository.findFirstByNpcIdAndPlayerIdAndGameDayAndStatusOrderByIdDesc(
                TEST_NPC_ID, playerId, gameStateService.getCurrentDay(), NpcTradeProposal.STATUS_OPEN).get();
        assertTrue(row.getTakeItems().contains("\"id\":2") || row.getTakeItems().contains("\"itemId\":2"));
        assertTrue(row.getTakeItems().contains("\"id\":" + ItemCatalog.FOOD_MATERIAL_ID)
                || row.getTakeItems().contains("\"itemId\":" + ItemCatalog.FOOD_MATERIAL_ID));
    }

    @Test
    public void survivingNpcSkipsWhenTheyDoNotWantTheOffer() {
        when(aiDialogueService.generateTradeProposal(any(), any(), any(), anyBoolean(),
                any(), any(), any(), anyInt(), anyInt(), nullable(String.class), anyInt()))
                .thenReturn(new AiDialogueService.TradeProposalPayload(
                        java.util.Collections.emptyList(), java.util.Collections.emptyList(), "不要。", false));
        seedNpcStock(20, 40, 2, 2);
        Map<String, Object> result = proposalService.maybeProposeAfterChat(playerId, TEST_NPC_ID, "给你手电筒");
        assertFalse(Boolean.TRUE.equals(result.get("tradeProposed")));
    }

    @Test
    public void expireOpenForDayReleasesReservation() {
        seedNpcStock(0, 0, 0, 2);
        int day = gameStateService.getCurrentDay();
        saveOpenProposal("[{\"t\":\"material\",\"id\":3,\"q\":2,\"name\":\"绳索\"}]",
                "[{\"t\":\"material\",\"id\":5,\"q\":2,\"name\":\"食物\"}]");
        assertEquals(2, proposalService.reservedGiveQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        int expired = proposalService.expireOpenForDay(day);
        assertTrue(expired >= 1);
        assertEquals(0, proposalService.reservedGiveQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
        assertEquals(2, npcInventoryService.getQuantity(TEST_NPC_ID, ItemType.material, ROPE_ID));
    }

    private void seedNpcStock(int food, int wood, int fuel, int rope) {
        npcItemRepository.deleteByNpcId(TEST_NPC_ID);
        if (food > 0) {
            addNpcItem(ItemType.material, ItemCatalog.FOOD_MATERIAL_ID, food);
        }
        if (wood > 0) {
            addNpcItem(ItemType.material, ItemCatalog.WOOD_MATERIAL_ID, wood);
        }
        if (fuel > 0) {
            addNpcItem(ItemType.material, ItemCatalog.FUEL_MATERIAL_ID, fuel);
        }
        if (rope > 0) {
            addNpcItem(ItemType.material, ROPE_ID, rope);
        }
    }

    private void addNpcItem(ItemType type, int itemId, int qty) {
        NpcItem row = new NpcItem();
        row.setNpcId(TEST_NPC_ID);
        row.setItemType(type);
        row.setItemId(itemId);
        row.setQuantity(qty);
        npcItemRepository.save(row);
    }

    private void addPlayerItem(ItemType type, int itemId, int qty) {
        PlayerItem row = new PlayerItem();
        row.setPlayerId(playerId);
        row.setItemType(type);
        row.setItemId(itemId);
        row.setQuantity(qty);
        playerItemRepository.save(row);
    }

    private int playerQty(ItemType type, int itemId) {
        return playerItemRepository.findByPlayerIdAndItemTypeAndItemId(playerId, type, itemId)
                .map(PlayerItem::getQuantity)
                .orElse(0);
    }

    private NpcTradeProposal saveOpenProposal(String giveJson, String takeJson) {
        NpcTradeProposal row = new NpcTradeProposal();
        row.setNpcId(TEST_NPC_ID);
        row.setPlayerId(playerId);
        row.setGameDay(gameStateService.getCurrentDay());
        row.setStatus(NpcTradeProposal.STATUS_OPEN);
        row.setGiveItems(giveJson);
        row.setTakeItems(takeJson);
        row.setRemark("test");
        return proposalRepository.save(row);
    }

    private void setFavor(int value) {
        NpcFavor favor = npcFavorRepository.findByNpcIdAndPlayerId(TEST_NPC_ID, playerId)
                .orElseGet(() -> {
                    NpcFavor created = new NpcFavor();
                    created.setNpcId(TEST_NPC_ID);
                    created.setPlayerId(playerId);
                    return created;
                });
        favor.setFavorValue(value);
        npcFavorRepository.save(favor);
    }
}
