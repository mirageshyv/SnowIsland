package com.example.snowisland.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AiDialogueService {

    private static final Logger logger = LoggerFactory.getLogger(AiDialogueService.class);
    private static final long BILLING_COOLDOWN_MS = 10 * 60 * 1000L;
    private static final int MAX_PLAYER_MESSAGE = 180;
    private static final int MAX_FIELD = 72;
    private static final int MAX_CLUE = 120;

    @Value("${ai.api-key:${ai.dashscope.api-key:}}")
    private String apiKey;

    @Value("${ai.api-url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${ai.model:deepseek-v4-flash}")
    private String model;

    @Value("${ai.max-tokens:80}")
    private int maxTokens;

    private final WebClient webClient;
    private final Gson gson;
    private final AtomicLong billingBlockedUntil = new AtomicLong(0);

    public AiDialogueService() {
        this.webClient = WebClient.create();
        this.gson = new Gson();
    }

    public static class NpcTurn {
        public final String reply;
        public final int favorChange;

        public NpcTurn(String reply, int favorChange) {
            this.reply = reply;
            this.favorChange = favorChange;
        }
    }

    public String generateNpcReply(String npcName, String npcJob, String npcPersonality,
                                   String npcDialogueStyle, String npcIntroduction,
                                   String playerMessage, int favorValue) {
        return generateNpcTurn(npcName, npcJob, npcPersonality, npcDialogueStyle,
                npcIntroduction, playerMessage, favorValue, null).reply;
    }

    public String generateClueReply(String npcName, String npcJob, String npcPersonality,
                                    String npcDialogueStyle, String npcIntroduction,
                                    String playerMessage, String clueContent, int favorValue) {
        return generateNpcTurn(npcName, npcJob, npcPersonality, npcDialogueStyle,
                npcIntroduction, playerMessage, favorValue, clueContent).reply;
    }

    /** Local keyword table only — favor is already included in generateNpcTurn. */
    public int calculateFavorChangeWithAI(String npcName, String npcJob, String npcPersonality,
                                          String playerMessage, int currentFavor) {
        return calculateFallbackFavorChange(npcJob, playerMessage);
    }

    public NpcTurn generateNpcTurn(String npcName, String npcJob, String npcPersonality,
                                   String npcDialogueStyle, String npcIntroduction,
                                   String playerMessage, int favorValue, String clueContent) {
        String name = clip(npcName, 24);
        if (name.isEmpty()) {
            name = "居民";
        }
        String job = clip(npcJob, 24);
        if (job.isEmpty()) {
            job = "居民";
        }
        String personality = clip(npcPersonality, MAX_FIELD);
        if (personality.isEmpty()) {
            personality = "普通";
        }
        String style = clip(npcDialogueStyle, MAX_FIELD);
        if (style.isEmpty()) {
            style = "短句口语";
        }
        String intro = clip(npcIntroduction, MAX_FIELD);
        String clue = clip(clueContent, MAX_CLUE);
        String player = clip(playerMessage, MAX_PLAYER_MESSAGE);
        String favorLevel = getFavorLevel(favorValue);

        String system = buildSpokenSystemPrompt(name, job, personality, style, intro, clue, favorValue, favorLevel);

        if (!canCallRemote()) {
            return fallbackTurn(name, job, player, favorValue);
        }

        try {
            String raw = callDeepSeekApi(system, player);
            NpcTurn parsed = parseNpcTurnPayload(raw);
            if (parsed != null && parsed.reply != null && !parsed.reply.trim().isEmpty()) {
                return parsed;
            }
            logger.warn("AI NPC turn empty, using fallback");
        } catch (WebClientResponseException e) {
            noteBillingFailure(e.getStatusCode().value(), e.getResponseBodyAsString());
            logger.error("AI HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("AI NPC turn failed: {}", e.getMessage());
        }
        return fallbackTurn(name, job, player, favorValue);
    }

    String buildSpokenSystemPrompt(String name, String job, String personality, String style,
                                   String intro, String clue, int favorValue, String favorLevel) {
        StringBuilder system = new StringBuilder(220);
        system.append("雪岛NPC。名:").append(name)
                .append(" 职:").append(job)
                .append(" 性:").append(personality)
                .append(" 口吻:").append(style)
                .append(" 好感:").append(favorValue).append('(').append(favorLevel).append(')');
        if (intro != null && !intro.isEmpty()) {
            system.append(" 背景:").append(intro);
        }
        if (clue != null && !clue.isEmpty()) {
            system.append(" 情报须自然带出勿照抄:").append(clue);
        }
        system.append(" 只输出JSON:{\"r\":\"1-2句中文台词\",\"f\":整数} f为-5到+5的非0整数，禁止0。勿OOC，勿交易细节。");
        return system.toString();
    }

    public static class TradeProposalPayload {
        public final List<Map<String, Object>> give;
        public final List<Map<String, Object>> take;
        public final String remark;
        public final Boolean want;

        public TradeProposalPayload(List<Map<String, Object>> give, List<Map<String, Object>> take, String remark) {
            this(give, take, remark, null);
        }

        public TradeProposalPayload(List<Map<String, Object>> give, List<Map<String, Object>> take, String remark,
                                    Boolean want) {
            this.give = give == null ? new ArrayList<Map<String, Object>>() : give;
            this.take = take == null ? new ArrayList<Map<String, Object>>() : take;
            this.remark = remark == null ? "" : remark;
            this.want = want;
        }
    }

    /**
     * Separate from chat. Inventory and catalog belong only in this prompt.
     * Returns null if the remote call cannot produce JSON.
     */
    public TradeProposalPayload generateTradeProposal(String npcName, String npcJob, String personality,
                                                      boolean survivalPath, String inventoryBrief,
                                                      String surplusBrief, String catalogBrief,
                                                      int foodNeed, int heatNeed) {
        return generateTradeProposal(npcName, npcJob, personality, survivalPath, inventoryBrief, surplusBrief,
                catalogBrief, foodNeed, heatNeed, null, 0);
    }

    public TradeProposalPayload generateTradeProposal(String npcName, String npcJob, String personality,
                                                      boolean survivalPath, String inventoryBrief,
                                                      String surplusBrief, String catalogBrief,
                                                      int foodNeed, int heatNeed, String playerMessage, int favorValue) {
        if (!canCallRemote()) {
            return null;
        }
        String name = clip(npcName, 24);
        String job = clip(npcJob, 24);
        String persona = clip(personality, MAX_FIELD);
        String spoken = clip(playerMessage, MAX_PLAYER_MESSAGE);
        String favorLevel = getFavorLevel(favorValue);
        StringBuilder system = new StringBuilder(500);
        system.append("雪岛NPC私下盘点物资，禁止把目录念给玩家。名:").append(name.isEmpty() ? "居民" : name)
                .append(" 职:").append(job.isEmpty() ? "居民" : job)
                .append(" 性:").append(persona.isEmpty() ? "普通" : persona)
                .append(" 好感:").append(favorValue).append('(').append(favorLevel).append(")。");
        system.append("玩家刚说:").append(spoken.isEmpty() ? "（无）" : spoken).append('。');
        system.append("若玩家拿某物来换：只在你会真的想要、且符合职业与好感时写入take。不想要则want=false且take为空。");
        system.append("好感高更愿收对方提出的东西、give可含好东西。");
        system.append("好感低：可收玩家提出的1件，并可再加要1件；give只给不值钱余粮，禁止武器弹药。");
        if (survivalPath) {
            system.append("今日缺食物").append(foodNeed).append("缺取暖").append(heatNeed)
                    .append("。take必须含食物/木材/燃料以补缺口；玩家若主动给这些可按缺口收，其它想要的东西可另加。give只能从surplus里选。");
        } else {
            system.append("今日能活。give从surplus选；take优先玩家刚提出的东西，没有想要的就不要硬凑。");
        }
        system.append("只输出JSON:{\"want\":true或false,\"give\":[{\"t\":\"item|weapon|ammo|material\",\"id\":整数,\"q\":正整数}],")
                .append("\"take\":[{\"t\":\"item|weapon|ammo|material\",\"id\":整数,\"q\":正整数}],")
                .append("\"remark\":\"一句中文，只提这笔交换\"}");

        StringBuilder user = new StringBuilder(400);
        user.append("背包:").append(clip(inventoryBrief, 400))
                .append(" surplus:").append(clip(surplusBrief, 280))
                .append(" 目录:").append(clip(catalogBrief, 500));

        try {
            String raw = callDeepSeekApi(system.toString(), user.toString(), 220);
            return parseTradeProposalPayload(raw);
        } catch (WebClientResponseException e) {
            noteBillingFailure(e.getStatusCode().value(), e.getResponseBodyAsString());
            logger.error("AI trade HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("AI trade proposal failed: {}", e.getMessage());
        }
        return null;
    }

    TradeProposalPayload parseTradeProposalPayload(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        try {
            JsonObject json = gson.fromJson(text.substring(start, end + 1), JsonObject.class);
            List<Map<String, Object>> give = readLineArray(json, "give");
            List<Map<String, Object>> take = readLineArray(json, "take");
            String remark = "";
            if (json.has("remark") && !json.get("remark").isJsonNull()) {
                remark = json.get("remark").getAsString().trim();
            }
            Boolean want = null;
            if (json.has("want") && !json.get("want").isJsonNull()) {
                try {
                    want = json.get("want").getAsBoolean();
                } catch (Exception ignored) {
                    want = null;
                }
            }
            return new TradeProposalPayload(give, take, remark, want);
        } catch (Exception e) {
            logger.warn("Failed to parse trade proposal JSON: {}", e.getMessage());
            return null;
        }
    }

    private List<Map<String, Object>> readLineArray(JsonObject json, String key) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (json == null || !json.has(key) || !json.get(key).isJsonArray()) {
            return out;
        }
        JsonArray arr = json.getAsJsonArray(key);
        for (JsonElement el : arr) {
            if (el == null || !el.isJsonObject()) {
                continue;
            }
            JsonObject row = el.getAsJsonObject();
            Map<String, Object> map = new HashMap<>();
            if (row.has("t") && !row.get("t").isJsonNull()) {
                map.put("t", row.get("t").getAsString());
            }
            if (row.has("id") && !row.get("id").isJsonNull()) {
                map.put("id", row.get("id").getAsInt());
            }
            if (row.has("q") && !row.get("q").isJsonNull()) {
                map.put("q", row.get("q").getAsInt());
            }
            if (!map.containsKey("t") || !map.containsKey("id")) {
                continue;
            }
            out.add(map);
        }
        return out;
    }

    private String callDeepSeekApi(String systemPrompt, String userPrompt) {
        return callDeepSeekApi(systemPrompt, userPrompt, maxTokens > 0 ? maxTokens : 80);
    }

    NpcTurn parseNpcTurnPayload(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            try {
                JsonObject json = gson.fromJson(text.substring(start, end + 1), JsonObject.class);
                String reply = json.has("r") && !json.get("r").isJsonNull()
                        ? json.get("r").getAsString().trim() : "";
                int favor = 0;
                if (json.has("f") && !json.get("f").isJsonNull()) {
                    favor = json.get("f").getAsInt();
                }
                favor = clampFavorChange(favor);
                if (!reply.isEmpty()) {
                    return new NpcTurn(reply, favor);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse NPC JSON turn: {}", e.getMessage());
            }
        }
        return new NpcTurn(text, clampFavorChange(0));
    }

    private NpcTurn fallbackTurn(String name, String job, String playerMessage, int favorValue) {
        return new NpcTurn(
                generateFallbackReply(name, job, playerMessage, favorValue),
                clampFavorChange(calculateFallbackFavorChange(job, playerMessage))
        );
    }

    private int clampFavorChange(int change) {
        if (change > 5) change = 5;
        if (change < -5) change = -5;
        if (change == 0) change = 1;
        return change;
    }

    private int calculateFallbackFavorChange(String npcJob, String message) {
        String lowerMsg = message == null ? "" : message.toLowerCase();

        if (lowerMsg.contains("谢谢") || lowerMsg.contains("感谢") || lowerMsg.contains("帮助")) {
            return 3;
        }
        if (lowerMsg.contains("你好") || lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return 2;
        }
        if (lowerMsg.contains("道歉") || lowerMsg.contains("对不起")) {
            return 2;
        }
        if (lowerMsg.contains("资源") || lowerMsg.contains("交易") || lowerMsg.contains("钱")) {
            if (npcJob != null && npcJob.contains("商人")) {
                return 2;
            }
        }
        if (lowerMsg.contains("信仰") || lowerMsg.contains("主") || lowerMsg.contains("神")) {
            if (npcJob != null && (npcJob.contains("神父") || npcJob.contains("牧师"))) {
                return 3;
            }
        }
        if (lowerMsg.contains("武器") || lowerMsg.contains("战斗") || lowerMsg.contains("危险")) {
            if (npcJob != null && (npcJob.contains("猎人") || npcJob.contains("铁匠"))) {
                return 2;
            }
        }
        if (lowerMsg.contains("医疗") || lowerMsg.contains("药") || lowerMsg.contains("受伤")) {
            if (npcJob != null && (npcJob.contains("医") || npcJob.contains("护士"))) {
                return 3;
            }
        }
        if (lowerMsg.contains("再见") || lowerMsg.contains("保重")) {
            return 1;
        }
        if (lowerMsg.contains("你是谁") || lowerMsg.contains("介绍")) {
            return 2;
        }
        if (lowerMsg.contains("滚") || lowerMsg.contains("去死") || lowerMsg.contains("混蛋")) {
            return -5;
        }
        if (lowerMsg.contains("骗") || lowerMsg.contains("骗子") || lowerMsg.contains("撒谎")) {
            return -3;
        }
        return 1;
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.length() <= max) {
            return trimmed;
        }
        return trimmed.substring(0, max);
    }

    private boolean canCallRemote() {
        return apiKey != null && !apiKey.trim().isEmpty()
                && System.currentTimeMillis() >= billingBlockedUntil.get();
    }

    private void noteBillingFailure(int status, String body) {
        String lower = body != null ? body.toLowerCase() : "";
        boolean billing = status == 401 || status == 402 || status == 403
                || lower.contains("insufficient balance")
                || lower.contains("invalid_api_key")
                || lower.contains("invalidapikey")
                || lower.contains("authentication")
                || (body != null && (body.contains("欠费") || body.contains("overdue-payment")));
        if (!billing) {
            return;
        }
        billingBlockedUntil.set(System.currentTimeMillis() + BILLING_COOLDOWN_MS);
        logger.error("DeepSeek billing/auth failed (HTTP {}). NPC chat uses local replies for 10 minutes.", status);
    }

    private String callDeepSeekApi(String systemPrompt, String userPrompt, int tokenLimit) {
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        Map<String, String> systemMessage = new HashMap<String, String>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        Map<String, String> userMessage = new HashMap<String, String>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);

        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.6);
        requestBody.put("max_tokens", tokenLimit > 0 ? tokenLimit : 80);
        requestBody.put("stream", false);
        Map<String, String> thinking = new HashMap<String, String>();
        thinking.put("type", "disabled");
        requestBody.put("thinking", thinking);
        Map<String, String> responseFormat = new HashMap<String, String>();
        responseFormat.put("type", "json_object");
        requestBody.put("response_format", responseFormat);

        String response = webClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Accept", "application/json;charset=UTF-8")
            .bodyValue(gson.toJson(requestBody))
            .retrieve()
            .bodyToMono(String.class)
            .timeout(java.time.Duration.ofSeconds(20))
            .block();

        return parseResponse(response);
    }

    String parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        try {
            JsonObject json = gson.fromJson(response, JsonObject.class);

            if (json.has("error") && json.get("error").isJsonObject()) {
                JsonObject err = json.getAsJsonObject("error");
                String message = err.has("message") && !err.get("message").isJsonNull()
                        ? err.get("message").getAsString() : "unknown error";
                throw new IllegalStateException("AI " + message);
            }

            if (json.has("choices") && json.get("choices").isJsonArray()) {
                JsonArray choices = json.getAsJsonArray("choices");
                if (choices.size() > 0) {
                    JsonObject choice = choices.get(0).getAsJsonObject();
                    if (choice.has("message") && choice.get("message").isJsonObject()) {
                        JsonObject message = choice.getAsJsonObject("message");
                        if (message.has("content") && !message.get("content").isJsonNull()) {
                            return message.get("content").getAsString().trim();
                        }
                    }
                    if (choice.has("text") && !choice.get("text").isJsonNull()) {
                        return choice.get("text").getAsString().trim();
                    }
                }
            }

            if (json.has("code") && !json.get("code").isJsonNull()) {
                String code = json.get("code").getAsString();
                if (code != null && !code.isEmpty() && !"Success".equalsIgnoreCase(code)) {
                    String message = json.has("message") && !json.get("message").isJsonNull()
                            ? json.get("message").getAsString() : "";
                    throw new IllegalStateException("DashScope " + code + ": " + message);
                }
            }

            if (json.has("output")) {
                JsonElement outputElement = json.get("output");

                if (outputElement.isJsonObject()) {
                    JsonObject output = outputElement.getAsJsonObject();

                    if (output.has("text")) {
                        return output.get("text").getAsString().trim();
                    }

                    if (output.has("choices")) {
                        JsonArray choices = output.getAsJsonArray("choices");
                        if (choices.size() > 0) {
                            JsonObject choice = choices.get(0).getAsJsonObject();
                            if (choice.has("message")) {
                                return choice.getAsJsonObject("message").get("content").getAsString().trim();
                            }
                            if (choice.has("text")) {
                                return choice.get("text").getAsString().trim();
                            }
                        }
                    }
                } else if (outputElement.isJsonPrimitive()) {
                    return outputElement.getAsString().trim();
                }
            }

            if (json.has("result")) {
                JsonObject result = json.getAsJsonObject("result");
                if (result.has("text")) {
                    return result.get("text").getAsString().trim();
                }
            }

            if (json.has("text")) {
                return json.get("text").getAsString().trim();
            }

        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to parse AI response: {}", e.getMessage());
        }

        return null;
    }

    private String getFavorLevel(int favorValue) {
        if (favorValue <= -60) return "敌视";
        if (favorValue <= -20) return "冷漠";
        if (favorValue <= 20) return "中立";
        if (favorValue <= 60) return "友善";
        return "亲近";
    }

    String generateFallbackReply(String npcName, String npcJob, String message, int favorValue) {
        String tone = "";
        if (favorValue <= -60) tone = "（冷漠地）";
        else if (favorValue <= -20) tone = "（冷淡地）";
        else if (favorValue <= 20) tone = "";
        else if (favorValue <= 60) tone = "（友善地）";
        else tone = "（热情地）";

        String msg = message == null ? "" : message.toLowerCase();
        String job = (npcJob == null || npcJob.trim().isEmpty()) ? "居民" : npcJob;
        String name = (npcName == null || npcName.trim().isEmpty()) ? "我" : npcName;

        if (msg.contains("你好") || msg.contains("hello") || msg.contains("hi")) {
            return tone + "你好，我是" + name + "。有什么事吗？";
        }
        if (msg.contains("再见") || msg.contains("拜拜")) {
            return tone + "再见，保重。";
        }
        if (msg.contains("谢谢") || msg.contains("感谢")) {
            return tone + "不客气。";
        }
        if (msg.contains("你是谁") || msg.contains("介绍")) {
            return tone + "我叫" + name + "，是这里的" + job + "。";
        }
        if (msg.contains("交易") || msg.contains("换") || msg.contains("买卖") || msg.contains("需要什么")) {
            return tone + "我这里有些东西可以交换，你可以看看交易面板。";
        }
        if (msg.contains("最近") || msg.contains("怎么样") || msg.contains("天气") || msg.contains("冷")) {
            return tone + "这座岛越来越冷了。能活着就不错，别的以后再说。";
        }
        if (msg.contains("任务") || msg.contains("帮忙") || msg.contains("帮我")) {
            return tone + "眼下自顾不暇。真要帮忙，先把交易谈清楚。";
        }
        if (msg.contains("食物") || msg.contains("吃") || msg.contains("燃料") || msg.contains("木头")) {
            return tone + "吃的和燃料都紧。有富余再拿出来换，别指望白拿。";
        }

        return tone + "眼下这座岛不好过。我是" + name + "，是这里的" + job + "。有事直说就行。";
    }
}
