package com.example.snowisland.service;

import com.example.snowisland.entity.GameDaySettings;
import com.example.snowisland.entity.GameState;
import com.example.snowisland.repository.GameStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class GameStateService {

    @Autowired
    private GameStateRepository gameStateRepository;

    @Autowired
    private PlayerConsumptionService playerConsumptionService;

    public Map<String, Object> getGameState() {
        GameState state = ensureState();
        return toMap(state, true);
    }

    public int getCurrentDay() {
        GameState state = ensureState();
        return state.getCurrentDay() != null ? state.getCurrentDay() : 1;
    }

    /** Edit rules for action submit UIs viewing {@code viewDay}. */
    public Map<String, Object> actionEditMeta(Integer viewDay) {
        GameState state = ensureState();
        int current = state.getCurrentDay() != null ? state.getCurrentDay() : 1;
        String phase = state.getCurrentPhase() != null ? state.getCurrentPhase() : "DAY";
        boolean isDay = "DAY".equalsIgnoreCase(phase);
        int day = viewDay != null && viewDay >= 1 ? viewDay : current;

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("currentGameDay", current);
        m.put("currentPhase", phase);
        m.put("viewGameDay", day);
        m.put("daytimeActionsEditable", day == current && isDay);
        m.put("nightActionsEditable", current <= day);
        return m;
    }

    public void enrichActionEditMeta(Map<String, Object> ctx, Integer viewDay) {
        if (ctx == null) {
            return;
        }
        ctx.putAll(actionEditMeta(viewDay));
    }

    public String denyDaytimeSubmit(Integer gameDay) {
        Map<String, Object> meta = actionEditMeta(gameDay);
        if (Boolean.TRUE.equals(meta.get("daytimeActionsEditable"))) {
            return null;
        }
        int current = (Integer) meta.get("currentGameDay");
        int day = gameDay != null ? gameDay : 0;
        if (current > day) {
            return "第 " + day + " 天的白天行动已结束，仅可查看";
        }
        if (current < day) {
            return "尚未到达第 " + day + " 天";
        }
        return "当前为夜晚阶段，无法再提交个人/阵营行动";
    }

    public String denyNightSubmit(Integer gameDay) {
        Map<String, Object> meta = actionEditMeta(gameDay);
        if (Boolean.TRUE.equals(meta.get("nightActionsEditable"))) {
            return null;
        }
        int day = gameDay != null ? gameDay : 0;
        return "第 " + day + " 天的夜晚行动已结束，仅可查看";
    }

    @Transactional
    public Map<String, Object> updateGameState(Map<String, Object> body, String userRole) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!isDm(userRole)) {
            result.put("success", false);
            result.put("message", "只有DM可以修改游戏设置");
            return result;
        }

        GameState state = ensureState();

        if (body.containsKey("currentDay")) {
            Integer day = intOrNull(body.get("currentDay"));
            if (day == null || day < 1 || day > 99) {
                result.put("success", false);
                result.put("message", "当前天数须为 1–99 的整数");
                return result;
            }
            state.setCurrentDay(day);
        }

        if (body.containsKey("currentPhase")) {
            String phase = stringVal(body.get("currentPhase"));
            if (phase == null) {
                result.put("success", false);
                result.put("message", "请指定游戏阶段");
                return result;
            }
            phase = phase.trim().toUpperCase();
            if (!"DAY".equals(phase) && !"NIGHT".equals(phase)) {
                result.put("success", false);
                result.put("message", "阶段须为 DAY 或 NIGHT");
                return result;
            }
            state.setCurrentPhase(phase);
        }

        if (body.containsKey("isGameOver")) {
            state.setIsGameOver(boolVal(body.get("isGameOver")));
        }
        if (body.containsKey("catastropheTriggered")) {
            state.setCatastropheTriggered(boolVal(body.get("catastropheTriggered")));
        }
        if (body.containsKey("extraCardDue")) {
            state.setExtraCardDue(boolVal(body.get("extraCardDue")));
        }

        state = gameStateRepository.save(state);
        int settingsDay = state.getCurrentDay() != null ? state.getCurrentDay() : 1;
        if (body.containsKey("requiredFoodUnits") || body.containsKey("requiredFuelKg")) {
            playerConsumptionService.saveDaySettings(
                    settingsDay,
                    intOrNull(body.get("requiredFoodUnits")),
                    intOrNull(body.get("requiredFuelKg")));
        }

        Map<String, Object> out = toMap(state, true);
        out.put("success", true);
        out.put("message", "游戏设置已保存");
        return out;
    }

    private GameState ensureState() {
        GameState state = gameStateRepository.findFirstByOrderByIdAsc();
        if (state == null) {
            state = new GameState();
            state = gameStateRepository.save(state);
        }
        return state;
    }

    private Map<String, Object> toMap(GameState state, boolean successFlag) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (successFlag) {
            m.put("success", true);
        }
        m.put("currentDay", state.getCurrentDay() != null ? state.getCurrentDay() : 1);
        m.put("currentPhase", state.getCurrentPhase() != null ? state.getCurrentPhase() : "DAY");
        m.put("isGameOver", Boolean.TRUE.equals(state.getIsGameOver()));
        m.put("catastropheTriggered", Boolean.TRUE.equals(state.getCatastropheTriggered()));
        m.put("extraCardDue", Boolean.TRUE.equals(state.getExtraCardDue()));
        int day = state.getCurrentDay() != null ? state.getCurrentDay() : 1;
        GameDaySettings daySettings = playerConsumptionService.getOrCreateDaySettings(day);
        m.put("requiredFoodUnits", daySettings.getRequiredFoodUnits());
        m.put("requiredFuelKg", daySettings.getRequiredFuelKg());
        return m;
    }

    private static boolean isDm(String userRole) {
        return userRole != null && "dm".equalsIgnoreCase(userRole.trim());
    }

    private static String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Integer intOrNull(Object o) {
        if (o == null || "".equals(o)) {
            return null;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean boolVal(Object o) {
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue() != 0;
        }
        String s = String.valueOf(o).trim();
        return "true".equalsIgnoreCase(s) || "1".equals(s);
    }
}
