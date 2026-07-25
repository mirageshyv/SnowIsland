package com.example.snowisland.service;

import com.example.snowisland.entity.*;
import com.example.snowisland.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CatastropheService {

    @Autowired
    private CatastropheProgressRepository progressRepository;

    @Autowired
    private CatastropheCardRepository cardRepository;

    @Autowired
    private CatastropheDeckRepository deckRepository;

    @Autowired
    private GameStateRepository gameStateRepository;

    @Autowired
    private SelectedCatastropheRepository selectedRepository;

    @Autowired
    private DrawnCardsRepository drawnCardsRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerConsumptionService playerConsumptionService;

    public Map<String, Object> getProgress() {
        CatastropheProgress progress = progressRepository.findFirstByOrderByIdAsc();
        if (progress == null) {
            progress = new CatastropheProgress();
            progress.setProgress(0);
            progress = progressRepository.save(progress);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("progress", progress.getProgress());
        result.put("lastUpdatedAt", progress.getLastUpdatedAt());
        return result;
    }

    @Transactional
    public Map<String, Object> updateProgress(Integer value, String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改天灾进度");
            return result;
        }

        if (value < 0 || value > 100) {
            result.put("success", false);
            result.put("message", "进度值必须在0-100之间");
            return result;
        }

        CatastropheProgress progress = progressRepository.findFirstByOrderByIdAsc();
        if (progress == null) {
            progress = new CatastropheProgress();
        }
        progress.setProgress(value);
        progress.setLastUpdatedAt(LocalDateTime.now());
        progress = progressRepository.save(progress);

        result.put("success", true);
        result.put("progress", progress.getProgress());
        result.put("message", "天灾进度已更新");

        if (progress.getProgress() >= 100) {
            triggerCatastrophe();
            result.put("catastropheTriggered", true);
        }

        return result;
    }

    @Transactional
    public Map<String, Object> advanceDay() {
        Map<String, Object> result = new HashMap<>();

        GameState gameState = gameStateRepository.findFirstByOrderByIdAsc();
        if (gameState == null) {
            gameState = new GameState();
        }

        int currentDay = gameState.getCurrentDay();
        int advanceAmount = currentDay < 3 ? 33 : 34;

        CatastropheProgress progress = progressRepository.findFirstByOrderByIdAsc();
        if (progress == null) {
            progress = new CatastropheProgress();
        }

        int newProgress = Math.min(100, progress.getProgress() + advanceAmount);
        progress.setProgress(newProgress);
        progress.setLastUpdatedAt(LocalDateTime.now());
        progress = progressRepository.save(progress);

        playerConsumptionService.applyMissedConsumptionPenalties(currentDay);

        gameState.setCurrentDay(currentDay + 1);
        gameState = gameStateRepository.save(gameState);

        result.put("success", true);
        result.put("progress", progress.getProgress());
        result.put("currentDay", gameState.getCurrentDay());
        result.put("advanceAmount", advanceAmount);

        if (progress.getProgress() >= 100) {
            triggerCatastrophe();
            result.put("catastropheTriggered", true);
            result.put("message", "天灾已触发！");
        }

        return result;
    }

    private void triggerCatastrophe() {
        GameState gameState = gameStateRepository.findFirstByOrderByIdAsc();
        if (gameState == null) {
            gameState = new GameState();
        }
        gameState.setCatastropheTriggered(true);
        gameState.setIsGameOver(true);
        gameStateRepository.save(gameState);
    }

    @Transactional
    public Map<String, Object> drawCards(String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以抽取天灾牌");
            return result;
        }

        List<CatastropheDeck> undrawnCards = deckRepository.findByIsDrawnFalseAndIsUsedFalse();
        if (undrawnCards.size() < 3) {
            result.put("success", false);
            result.put("message", "剩余卡牌不足3张");
            return result;
        }

        Collections.shuffle(undrawnCards);
        List<CatastropheDeck> drawn = undrawnCards.subList(0, 3);

        Integer maxRound = drawnCardsRepository.findMaxDrawRound();
        int currentRound = maxRound == null ? 1 : maxRound + 1;

        List<Map<String, Object>> drawnCardDetails = new ArrayList<>();

        for (int i = 0; i < drawn.size(); i++) {
            CatastropheDeck deck = drawn.get(i);
            deck.setIsDrawn(true);
            deck.setDrawnAt(LocalDateTime.now());
            deck = deckRepository.save(deck);

            CatastropheCard card = cardRepository.findById(deck.getCardId()).orElse(null);

            DrawnCards drawnCard = new DrawnCards();
            drawnCard.setDrawRound(currentRound);
            drawnCard.setDeckId(deck.getId());
            drawnCard.setPosition(i + 1);
            drawnCardsRepository.save(drawnCard);

            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("deckId", deck.getId());
            cardInfo.put("cardId", card != null ? card.getId() : null);
            cardInfo.put("cardNumber", card != null ? card.getCardNumber() : null);
            cardInfo.put("name", card != null ? card.getName() : "未知卡牌");
            cardInfo.put("description", card != null ? card.getDescription() : "");
            cardInfo.put("position", i + 1);
            drawnCardDetails.add(cardInfo);
        }

        result.put("success", true);
        result.put("drawRound", currentRound);
        result.put("cards", drawnCardDetails);
        result.put("message", "成功抽取3张天灾牌");

        return result;
    }

    public Map<String, Object> getDrawnCards(Integer round) {
        Map<String, Object> result = new HashMap<>();

        List<DrawnCards> drawnCards;
        if (round != null) {
            drawnCards = drawnCardsRepository.findByDrawRoundOrderByPositionAsc(round);
        } else {
            Integer maxRound = drawnCardsRepository.findMaxDrawRound();
            if (maxRound == null) {
                result.put("success", true);
                result.put("cards", new ArrayList<>());
                result.put("drawRound", 0);
                return result;
            }
            drawnCards = drawnCardsRepository.findByDrawRoundOrderByPositionAsc(maxRound);
        }

        List<Map<String, Object>> cardDetails = new ArrayList<>();
        for (DrawnCards dc : drawnCards) {
            CatastropheDeck deck = deckRepository.findById(dc.getDeckId()).orElse(null);
            CatastropheCard card = deck != null ? cardRepository.findById(deck.getCardId()).orElse(null) : null;

            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("drawnCardId", dc.getId());
            cardInfo.put("deckId", dc.getDeckId());
            cardInfo.put("cardId", card != null ? card.getId() : null);
            cardInfo.put("cardNumber", card != null ? card.getCardNumber() : null);
            cardInfo.put("name", card != null ? card.getName() : "未知卡牌");
            cardInfo.put("description", card != null ? card.getDescription() : "");
            cardInfo.put("position", dc.getPosition());
            cardInfo.put("isSelected", dc.getIsSelected());
            cardDetails.add(cardInfo);
        }

        result.put("success", true);
        result.put("cards", cardDetails);
        result.put("drawRound", round != null ? round : drawnCardsRepository.findMaxDrawRound());

        return result;
    }

    @Transactional
    public Map<String, Object> confirmCards(String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以确认牌组");
            return result;
        }

        Integer maxRound = drawnCardsRepository.findMaxDrawRound();
        if (maxRound == null) {
            result.put("success", false);
            result.put("message", "没有已抽取的牌组");
            return result;
        }

        List<DrawnCards> drawnCards = drawnCardsRepository.findByDrawRound(maxRound);
        if (drawnCards.isEmpty()) {
            result.put("success", false);
            result.put("message", "当前轮次没有卡牌");
            return result;
        }

        for (SelectedCatastrophe sc : selectedRepository.findByIsActiveTrue()) {
            sc.setIsActive(false);
            selectedRepository.save(sc);
        }

        List<Map<String, Object>> confirmedCards = new ArrayList<>();
        for (DrawnCards dc : drawnCards) {
            CatastropheDeck deck = deckRepository.findById(dc.getDeckId()).orElse(null);
            CatastropheCard card = deck != null ? cardRepository.findById(deck.getCardId()).orElse(null) : null;

            SelectedCatastrophe selected = new SelectedCatastrophe();
            selected.setDeckId(dc.getDeckId());
            selected.setIsActive(true);
            selected = selectedRepository.save(selected);

            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("selectedId", selected.getId());
            cardInfo.put("deckId", dc.getDeckId());
            cardInfo.put("cardId", card != null ? card.getId() : null);
            cardInfo.put("cardNumber", card != null ? card.getCardNumber() : null);
            cardInfo.put("name", card != null ? card.getName() : "未知卡牌");
            cardInfo.put("description", card != null ? card.getDescription() : "");
            cardInfo.put("position", dc.getPosition());
            cardInfo.put("isSelected", false);
            cardInfo.put("playerId", null);
            confirmedCards.add(cardInfo);
        }

        result.put("success", true);
        result.put("cards", confirmedCards);
        result.put("message", "牌组已确认并发送至选择模块");

        return result;
    }

    public Map<String, Object> getSelectableCards() {
        Map<String, Object> result = new HashMap<>();

        List<SelectedCatastrophe> selectedList = selectedRepository.findByIsActiveTrue();
        List<Map<String, Object>> cards = new ArrayList<>();

        for (SelectedCatastrophe sc : selectedList) {
            CatastropheDeck deck = deckRepository.findById(sc.getDeckId()).orElse(null);
            CatastropheCard card = deck != null ? cardRepository.findById(deck.getCardId()).orElse(null) : null;

            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("selectedId", sc.getId());
            cardInfo.put("deckId", sc.getDeckId());
            cardInfo.put("cardId", card != null ? card.getId() : null);
            cardInfo.put("cardNumber", card != null ? card.getCardNumber() : null);
            cardInfo.put("name", card != null ? card.getName() : "未知卡牌");
            cardInfo.put("description", card != null ? card.getDescription() : "");
            cardInfo.put("isSelected", sc.getPlayerId() != null);
            cardInfo.put("playerId", sc.getPlayerId());
            cards.add(cardInfo);
        }

        result.put("success", true);
        result.put("cards", cards);
        return result;
    }

    @Transactional
    public Map<String, Object> selectCard(Integer selectedId, Integer playerId, String userRole) {
        Map<String, Object> result = new HashMap<>();

        SelectedCatastrophe selected = selectedRepository.findById(selectedId).orElse(null);
        if (selected == null) {
            result.put("success", false);
            result.put("message", "选择记录不存在");
            return result;
        }

        boolean isDm = "dm".equalsIgnoreCase(userRole);
        boolean isScourge = false;
        if (!isDm && playerId != null) {
            Player player = playerRepository.findById(playerId).orElse(null);
            if (player != null && player.getFaction() == Player.Faction.天灾使者) {
                isScourge = true;
            }
        }

        if (!isDm && !isScourge) {
            result.put("success", false);
            result.put("message", "只有天灾使者或DM可以选择卡牌");
            return result;
        }

        for (SelectedCatastrophe sc : selectedRepository.findByIsActiveTrue()) {
            sc.setPlayerId(sc.getId().equals(selectedId) ? playerId : null);
            sc.setSelectedAt(sc.getId().equals(selectedId) ? LocalDateTime.now() : null);
            selectedRepository.save(sc);
        }

        CatastropheDeck deck = deckRepository.findById(selected.getDeckId()).orElse(null);
        CatastropheCard card = deck != null ? cardRepository.findById(deck.getCardId()).orElse(null) : null;

        deck.setIsUsed(true);
        deck.setUsedAt(LocalDateTime.now());
        deck.setRoundUsed(1);
        deckRepository.save(deck);

        result.put("success", true);
        result.put("cardName", card != null ? card.getName() : "未知卡牌");
        result.put("message", "已选择天灾牌: " + (card != null ? card.getName() : "未知卡牌"));

        return result;
    }

    public Map<String, Object> getGameState() {
        GameState gameState = gameStateRepository.findFirstByOrderByIdAsc();
        if (gameState == null) {
            gameState = new GameState();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("currentDay", gameState.getCurrentDay());
        result.put("currentPhase", gameState.getCurrentPhase());
        result.put("isGameOver", gameState.getIsGameOver());
        result.put("catastropheTriggered", gameState.getCatastropheTriggered());
        result.put("extraCardDue", gameState.getExtraCardDue());

        CatastropheProgress progress = progressRepository.findFirstByOrderByIdAsc();
        result.put("catastropheProgress", progress != null ? progress.getProgress() : 0);

        return result;
    }

    @Transactional
    public Map<String, Object> setExtraCardDue(Boolean extraCardDue, String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以设置额外天灾牌");
            return result;
        }

        GameState gameState = gameStateRepository.findFirstByOrderByIdAsc();
        if (gameState == null) {
            gameState = new GameState();
        }
        gameState.setExtraCardDue(extraCardDue);
        gameStateRepository.save(gameState);

        result.put("success", true);
        result.put("extraCardDue", extraCardDue);
        result.put("message", "额外天灾牌状态已更新");

        return result;
    }

    public List<CatastropheCard> getAllCards() {
        return cardRepository.findAllByOrderByCardNumberAsc();
    }

    @Transactional
    public Map<String, Object> resetCatastrophe(String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以复原天灾牌");
            return result;
        }

        drawnCardsRepository.deleteAll();
        selectedRepository.deleteAll();

        List<CatastropheDeck> allDecks = deckRepository.findAll();
        for (CatastropheDeck deck : allDecks) {
            deck.setIsDrawn(false);
            deck.setIsUsed(false);
            deck.setDrawnAt(null);
            deck.setUsedAt(null);
            deck.setRoundUsed(0);
        }
        deckRepository.saveAll(allDecks);

        CatastropheProgress progress = progressRepository.findFirstByOrderByIdAsc();
        if (progress != null) {
            progress.setProgress(0);
            progress.setLastUpdatedAt(LocalDateTime.now());
            progressRepository.save(progress);
        }

        GameState gameState = gameStateRepository.findFirstByOrderByIdAsc();
        if (gameState != null) {
            gameState.setCatastropheTriggered(false);
            gameState.setExtraCardDue(false);
            gameState.setIsGameOver(false);
            gameStateRepository.save(gameState);
        }

        result.put("success", true);
        result.put("message", "天灾牌已复原到初始状态");
        return result;
    }

    @Transactional
    public Map<String, Object> clearAndReloadCards(String userRole) {
        Map<String, Object> result = new HashMap<>();

        if (!"dm".equalsIgnoreCase(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以重置天灾牌数据");
            return result;
        }

        try {
            selectedRepository.deleteAllInBatch();
            drawnCardsRepository.deleteAllInBatch();
            deckRepository.deleteAllInBatch();
            cardRepository.deleteAllInBatch();

            List<CatastropheCard> newCards = new ArrayList<>();

            CatastropheCard card1 = new CatastropheCard();
            card1.setCardNumber(1);
            card1.setName("低温侵袭");
            card1.setDescription("某处墙体结冰，寒气渗入。本天所有燃料消耗增加100%，木材消耗量15kg→30kg。");
            card1.setEffectType("CONSUMPTION_INCREASE");
            card1.setEffectParam1(100);
            card1.setEffectParam2(15);
            card1.setEffectParam3("30");
            card1.setIsUnique(false);
            newCards.add(card1);

            CatastropheCard card2 = new CatastropheCard();
            card2.setCardNumber(2);
            card2.setName("灾难蔓延");
            card2.setDescription("增加5天暴雪持续时间，方舟航行难度增加航行时间加2天。");
            card2.setEffectType("EXTEND_STORM");
            card2.setEffectParam1(5);
            card2.setEffectParam2(2);
            card2.setIsUnique(false);
            newCards.add(card2);

            CatastropheCard card3 = new CatastropheCard();
            card3.setCardNumber(3);
            card3.setName("粮仓鼠患");
            card3.setDescription("由天灾使者选择两个仓库\n仓库中储存的粮食被老鼠啃食，损失20%的食物储备（向下取整）");
            card3.setEffectType("FOOD_LOSS");
            card3.setEffectParam1(20);
            card3.setEffectParam2(2);
            card3.setIsUnique(false);
            newCards.add(card3);

            CatastropheCard card4 = new CatastropheCard();
            card4.setCardNumber(4);
            card4.setName("燃料泄漏");
            card4.setDescription("储油桶老化破裂，损失一处仓库的20%的燃料储备（优先扣除煤油/燃油）");
            card4.setEffectType("FUEL_LOSS");
            card4.setEffectParam1(20);
            card4.setIsUnique(false);
            newCards.add(card4);

            CatastropheCard card5 = new CatastropheCard();
            card5.setCardNumber(5);
            card5.setName("工具锈蚀");
            card5.setDescription("生产工具普遍老化。当天所有生产行动（渔猎、伐木、挖矿等）产量-50%。");
            card5.setEffectType("PRODUCTION_DECREASE");
            card5.setEffectParam1(50);
            card5.setIsUnique(false);
            newCards.add(card5);

            CatastropheCard card6 = new CatastropheCard();
            card6.setCardNumber(6);
            card6.setName("海水倒灌");
            card6.setDescription("风暴潮淹没码头设施，沿海仓库的部分物资被冲走（损失20%），方舟受损30%。");
            card6.setEffectType("DOCK_DAMAGE");
            card6.setEffectParam1(20);
            card6.setEffectParam2(30);
            card6.setIsUnique(false);
            newCards.add(card6);

            CatastropheCard card7 = new CatastropheCard();
            card7.setCardNumber(7);
            card7.setName("水源污染");
            card7.setDescription("岛上淡水水源被动物尸体污染，所有玩家当天需额外消耗1升煤油（烧开水）或面临患病风险");
            card7.setEffectType("WATER_CONTAMINATION");
            card7.setEffectParam1(1);
            card7.setIsUnique(false);
            newCards.add(card7);

            CatastropheCard card8 = new CatastropheCard();
            card8.setCardNumber(8);
            card8.setName("信仰崩塌");
            card8.setDescription("神父以及占卜师等精神领袖陷入自我怀疑，当天无法使用\"布道\"或\"占星\"技能。若第三天抽中不影响终局结算加成。");
            card8.setEffectType("SKILL_DISABLE");
            card8.setEffectParam3("布道,占星");
            card8.setIsUnique(false);
            newCards.add(card8);

            CatastropheCard card9 = new CatastropheCard();
            card9.setCardNumber(9);
            card9.setName("燃料受潮");
            card9.setDescription("露天堆放的木柴被雨淋湿。随机一个仓库或玩家损失30kg木材。");
            card9.setEffectType("WOOD_LOSS");
            card9.setEffectParam1(30);
            card9.setIsUnique(false);
            newCards.add(card9);

            CatastropheCard card10 = new CatastropheCard();
            card10.setCardNumber(10);
            card10.setName("逃役");
            card10.setDescription("一名劳工趁夜色逃走了。统治者当天指定的劳工名单中，随机一人自动失效（不会劳作，也不会计入劳工）。主持人随机选择，不公开是谁。该玩家知道自己被逃役释放，当天正常进行行动。");
            card10.setEffectType("ESCAPE_LABOR");
            card10.setIsUnique(false);
            newCards.add(card10);

            CatastropheCard card11 = new CatastropheCard();
            card11.setCardNumber(11);
            card11.setName("祭品");
            card11.setDescription("有人在教堂门口发现一只被割喉的黑羊。第二天必定触发两张额外天灾牌（也就是第二天触发3张天灾牌）。");
            card11.setEffectType("EXTRA_CARD");
            card11.setEffectParam1(2);
            card11.setIsUnique(false);
            newCards.add(card11);

            CatastropheCard card12 = new CatastropheCard();
            card12.setCardNumber(12);
            card12.setName("屋顶坍塌");
            card12.setDescription("某栋小镇建筑（随机）的屋顶因积雪过厚而垮塌，压坏内部设施。\n效果：主持人随机选择一个小镇地点，该地点的防御值永久-2，且内部一个随机设施损坏（如发电机、烘焙炉、电报机等），需消耗 50kg木材 和 1次维修行动 修复。");
            card12.setEffectType("BUILDING_COLLAPSE");
            card12.setEffectParam1(2);
            card12.setEffectParam2(50);
            card12.setIsUnique(false);
            newCards.add(card12);

            CatastropheCard card13 = new CatastropheCard();
            card13.setCardNumber(13);
            card13.setName("道路冰封");
            card13.setDescription("一夜之间，连接各主要地点的土路冻成镜面，车和人都打滑难行。\n当天所有地点之间的移动搬运量减半（任何运输行动效率-50%）。");
            card13.setEffectType("TRANSPORT_DECREASE");
            card13.setEffectParam1(50);
            card13.setIsUnique(false);
            newCards.add(card13);

            List<CatastropheCard> savedCards = cardRepository.saveAll(newCards);

            List<CatastropheDeck> decks = new ArrayList<>();
            for (CatastropheCard card : savedCards) {
                for (int i = 0; i < 3; i++) {
                    CatastropheDeck deck = new CatastropheDeck();
                    deck.setCardId(card.getId());
                    deck.setIsDrawn(false);
                    deck.setIsUsed(false);
                    decks.add(deck);
                }
            }
            deckRepository.saveAll(decks);

            result.put("success", true);
            result.put("message", "天灾牌数据已重置，共导入13张卡牌，创建39份牌组");
            result.put("cardCount", savedCards.size());
            result.put("deckCount", decks.size());

            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "重置失败: " + e.getMessage());
            return result;
        }
    }
}