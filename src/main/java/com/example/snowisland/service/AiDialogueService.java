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

@Service
public class AiDialogueService {

    private static final Logger logger = LoggerFactory.getLogger(AiDialogueService.class);

    @Value("${ai.dashscope.api-key}")
    private String apiKey;

    @Value("${ai.dashscope.api-url}")
    private String apiUrl;

    @Value("${ai.dashscope.model}")
    private String model;

    private final WebClient webClient;
    private final Gson gson;

    public AiDialogueService() {
        this.webClient = WebClient.create();
        this.gson = new Gson();
    }

    public String generateNpcReply(String npcName, String npcJob, String npcPersonality,
                                   String npcIntroduction, String playerMessage, int favorValue) {
        String favorLevel = getFavorLevel(favorValue);
        String tone = getToneByFavor(favorValue);

        String systemPrompt = String.format(
            "你是游戏《雪岛》中的NPC角色【%s】。\n" +
            "身份：%s\n" +
            "性格：%s\n" +
            "背景介绍：%s\n" +
            "当前与玩家的好感度：%d（%s）\n" +
            "\n" +
            "请根据以下要求进行回答：\n" +
            "1. 保持角色身份，用【%s】的口吻说话\n" +
            "2. 回答要符合你的性格特点\n" +
            "3. 好感度会影响你的态度：%s\n" +
            "4. 回答要简短自然，符合末世生存游戏的氛围\n" +
            "5. 不要使用OOC（脱离角色）的语言\n" +
            "6. 不要太长，通常1-3句话即可\n" +
            "7. 如果玩家问交易相关的问题，适当回应但不要直接给出交易信息\n" +
            "\n" +
            "现在，请以【%s】的身份回复玩家。",
            npcName, npcJob, npcPersonality != null ? npcPersonality : "普通",
            npcIntroduction != null ? npcIntroduction : "没有特别的背景故事",
            favorValue, favorLevel, npcName, tone, npcName
        );

        String userPrompt = String.format("玩家说：%s\n请以【%s】的身份回复：", playerMessage, npcName);

        try {
            return callDashScopeApi(systemPrompt, userPrompt);
        } catch (WebClientResponseException e) {
            System.err.println("AI API调用错误: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return generateFallbackReply(npcName, npcJob, playerMessage, favorValue);
        } catch (Exception e) {
            System.err.println("AI调用异常: " + e.getMessage());
            e.printStackTrace();
            return generateFallbackReply(npcName, npcJob, playerMessage, favorValue);
        }
    }

    /**
     * AI计算好感度变化（单次增加不超过10，降低不超过5）
     */
    public int calculateFavorChangeWithAI(String npcName, String npcJob, String npcPersonality,
                                          String playerMessage, int currentFavor) {
        String favorLevel = getFavorLevel(currentFavor);

        String systemPrompt = String.format(
            "你是游戏《雪岛》中的NPC角色【%s】的情感分析系统。\n" +
            "NPC身份：%s\n" +
            "NPC性格：%s\n" +
            "当前好感度：%d（%s）\n" +
            "\n" +
            "请分析玩家的消息，并决定好感度变化。\n" +
            "规则：\n" +
            "1. 好感度变化必须是整数\n" +
            "2. 单次增加不能超过+10\n" +
            "3. 单次降低不能超过-5\n" +
            "4. 正常交流通常变化在-3到+5之间\n" +
            "5. 友好、感谢、帮助等积极行为可以增加好感度\n" +
            "6. 粗鲁、威胁、欺骗等消极行为会降低好感度\n" +
            "7. 根据NPC身份和性格判断：商人喜欢交易话题，医生喜欢医疗话题等\n" +
            "\n" +
            "请严格按照JSON格式输出：\n" +
            "{\"favorChange\": 数字, \"reason\": \"原因描述\"}",
            npcName, npcJob, npcPersonality != null ? npcPersonality : "普通",
            currentFavor, favorLevel
        );

        String userPrompt = "玩家消息：" + playerMessage;

        try {
            String response = callDashScopeApi(systemPrompt, userPrompt);
            return parseFavorChangeResponse(response);
        } catch (Exception e) {
            logger.error("AI好感度计算失败: {}", e.getMessage());
            return calculateFallbackFavorChange(npcJob, playerMessage);
        }
    }

    private int parseFavorChangeResponse(String response) {
        if (response == null || response.isEmpty()) {
            return 0;
        }

        try {
            JsonObject json = gson.fromJson(response, JsonObject.class);
            if (json.has("favorChange")) {
                int change = json.get("favorChange").getAsInt();
                return clampFavorChange(change);
            }

            String cleanResponse = response.replaceAll("[```json|```]", "").trim();
            json = gson.fromJson(cleanResponse, JsonObject.class);
            if (json.has("favorChange")) {
                int change = json.get("favorChange").getAsInt();
                return clampFavorChange(change);
            }

        } catch (Exception e) {
            logger.warn("解析AI好感度响应失败: {}", e.getMessage());
        }

        return 0;
    }

    private int clampFavorChange(int change) {
        if (change > 10) change = 10;
        if (change < -5) change = -5;
        return change;
    }

    private int calculateFallbackFavorChange(String npcJob, String message) {
        String lowerMsg = message.toLowerCase();

        if (lowerMsg.contains("谢谢") || lowerMsg.contains("感谢") || lowerMsg.contains("帮助")) {
            return 5;
        }
        if (lowerMsg.contains("你好") || lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return 2;
        }
        if (lowerMsg.contains("道歉") || lowerMsg.contains("对不起")) {
            return 3;
        }
        if (lowerMsg.contains("资源") || lowerMsg.contains("交易") || lowerMsg.contains("钱")) {
            if (npcJob != null && npcJob.contains("商人")) {
                return 3;
            }
            return 0;
        }
        if (lowerMsg.contains("信仰") || lowerMsg.contains("主") || lowerMsg.contains("神")) {
            if (npcJob != null && (npcJob.contains("神父") || npcJob.contains("牧师"))) {
                return 5;
            }
            return 0;
        }
        if (lowerMsg.contains("武器") || lowerMsg.contains("战斗") || lowerMsg.contains("危险")) {
            if (npcJob != null && (npcJob.contains("猎人") || npcJob.contains("铁匠"))) {
                return 3;
            }
            return 0;
        }
        if (lowerMsg.contains("医疗") || lowerMsg.contains("药") || lowerMsg.contains("受伤")) {
            if (npcJob != null && (npcJob.contains("医") || npcJob.contains("护士"))) {
                return 5;
            }
            return 0;
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
        return 0;
    }

    private String callDashScopeApi(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new HashMap<String, Object>();
        requestBody.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<Map<String, String>>();
        
        Map<String, String> systemMessage = new HashMap<String, String>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        Map<String, String> userMessage = new HashMap<String, String>();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.add(userMessage);
        
        Map<String, Object> input = new HashMap<String, Object>();
        input.put("messages", messages);
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("temperature", 0.7);
        parameters.put("top_p", 0.9);
        parameters.put("max_tokens", 200);
        requestBody.put("parameters", parameters);

        String jsonBody = gson.toJson(requestBody);
        System.out.println("AI请求体: " + jsonBody);
        
        String response = webClient.post()
            .uri(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Accept", "application/json;charset=UTF-8")
            .bodyValue(jsonBody)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(java.time.Duration.ofSeconds(30))
            .block();

        System.out.println("AI响应: " + response);
        
        return parseResponse(response);
    }

    private String parseResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }
        
        try {
            JsonObject json = gson.fromJson(response, JsonObject.class);
            
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
            
        } catch (Exception e) {
            System.err.println("响应解析失败: " + e.getMessage());
            e.printStackTrace();
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

    private String getToneByFavor(int favorValue) {
        if (favorValue <= -60) return "非常敌视，说话充满敌意，可能拒绝交流";
        if (favorValue <= -20) return "冷淡，说话简短，不太愿意交流";
        if (favorValue <= 20) return "中立，礼貌但保持距离";
        if (favorValue <= 60) return "友善，愿意帮助，说话温和";
        return "亲近，热情友好，像朋友一样交流";
    }

    public String generateClueReply(String npcName, String npcJob, String npcPersonality,
                                   String npcIntroduction, String playerMessage, String clueContent, int favorValue) {
        String favorLevel = getFavorLevel(favorValue);
        String tone = getToneByFavor(favorValue);

        String systemPrompt = String.format(
            "你是游戏《雪岛》中的NPC角色【%s】。\n" +
            "身份：%s\n" +
            "性格：%s\n" +
            "背景介绍：%s\n" +
            "当前与玩家的好感度：%d（%s）\n" +
            "\n" +
            "玩家刚刚触发了一个特殊线索！请根据以下线索内容，以自然、符合角色设定的方式回应玩家。\n" +
            "\n" +
            "线索内容：%s\n" +
            "\n" +
            "要求：\n" +
            "1. 保持角色身份，用【%s】的口吻说话\n" +
            "2. 回答要符合你的性格特点\n" +
            "3. 好感度会影响你的态度：%s\n" +
            "4. 不要直接复述线索内容，要转化为自然的口语化表达\n" +
            "5. 回答要简短自然，符合末世生存游戏的氛围\n" +
            "6. 不要使用OOC（脱离角色）的语言\n" +
            "7. 不要太长，通常1-3句话即可\n" +
            "\n" +
            "现在，请以【%s】的身份回复玩家。",
            npcName, npcJob, npcPersonality != null ? npcPersonality : "普通",
            npcIntroduction != null ? npcIntroduction : "没有特别的背景故事",
            favorValue, favorLevel, clueContent,
            npcName, tone, npcName
        );

        String userPrompt = String.format("玩家说：%s\n请以【%s】的身份回复（基于线索内容）：", playerMessage, npcName);

        try {
            return callDashScopeApi(systemPrompt, userPrompt);
        } catch (Exception e) {
            System.err.println("AI线索回复生成失败: " + e.getMessage());
            return generateClueFallbackReply(npcName, npcJob, clueContent, favorValue);
        }
    }

    private String generateClueFallbackReply(String npcName, String npcJob, String clueContent, int favorValue) {
        String tone = "";
        if (favorValue <= -60) tone = "（冷漠地）";
        else if (favorValue <= -20) tone = "（冷淡地）";
        else if (favorValue <= 20) tone = "";
        else if (favorValue <= 60) tone = "（若有所思地）";
        else tone = "（神秘地）";

        return tone + clueContent;
    }

    private String generateFallbackReply(String npcName, String npcJob, String message, int favorValue) {
        String tone = "";
        if (favorValue <= -60) tone = "（冷漠地）";
        else if (favorValue <= -20) tone = "（冷淡地）";
        else if (favorValue <= 20) tone = "";
        else if (favorValue <= 60) tone = "（友善地）";
        else tone = "（热情地）";

        String lowerMsg = message.toLowerCase();
        
        if (lowerMsg.contains("你好") || lowerMsg.contains("hello") || lowerMsg.contains("hi")) {
            return tone + "你好，我是" + npcName + "。有什么事吗？";
        }
        
        if (lowerMsg.contains("再见") || lowerMsg.contains("拜拜")) {
            return tone + "再见，保重。";
        }
        
        if (lowerMsg.contains("谢谢") || lowerMsg.contains("感谢")) {
            return tone + "不客气。";
        }
        
        if (lowerMsg.contains("你是谁") || lowerMsg.contains("介绍")) {
            return tone + "我叫" + npcName + "，是这里的" + npcJob + "。";
        }
        
        if (lowerMsg.contains("交易") || lowerMsg.contains("换") || lowerMsg.contains("买卖")) {
            return tone + "我这里有些东西可以交换，你可以看看交易面板。";
        }
        
        return tone + "嗯...我不太明白你的意思。";
    }
}