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
            selectedRepository.save(selected);

            Map<String, Object> cardInfo = new HashMap<>();
            cardInfo.put("deckId", dc.getDeckId());
            cardInfo.put("cardId", card != null ? card.getId() : null);
            cardInfo.put("cardNumber", card != null ? card.getCardNumber() : null);
            cardInfo.put("name", card != null ? card.getName() : "未知卡牌");
            cardInfo.put("description", card != null ? card.getDescription() : "");
            cardInfo.put("position", dc.getPosition());
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
}