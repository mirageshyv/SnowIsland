package com.example.snowisland.util;

import com.example.snowisland.entity.Player;

import java.util.*;

/**
 * 玩家负面状态文案与规则标记（供 API / 战斗辅助使用）。
 */
public final class PlayerStatusCatalog {

    public static final String WEAK_DESC =
            "你的血肉在低语，诉说着某种迟缓的终结。生产的仪式已非你所能负担，更不必说与阴影中的敌手角力。（无法生产，格斗射击技能无效，可喝酒消除）";

    public static final String OVERWORKED_DESC =
            "骨骼在重复的磨损中发出暗哑的呻吟。你知晓那矿洞深处——那该死的避难所残骸——不可再踏足。否则，另一种结局会比夜幕更先降临。（无法执行生产行动，调查玩家和隐匿。在当天夜晚行动和第二天进行需要行动点的生产行动时，投1d6骰子，判定为1则死亡，可使用5瓶朗姆酒消除过劳）";

    public static final String INJURED_DESC =
            "一道痕迹。它尚未决心吞噬你的命数——至少此刻，它还在犹豫。（你已经受伤了，再次受伤会恶化，无法生产，格斗技能无效）";

    public static final String SEVERE_DESC =
            "你的沙漏已近枯竭。你不知今夜是否便是最后一页。某个披着白袍的影或许能挽留你——又或许，还有另一个……更幽暗的。（无法行动，每夜阶段结束时若不进行急救，则死亡，急救消耗5医疗资源，可将重伤转为受伤）";

    public static final String DEAD_DESC =
            "天灾的舌锋舔舐过这具躯壳。所有的门扉都已阖上，再无应答。（无额外效果）";

    private PlayerStatusCatalog() {
    }

    private static boolean isDeadActive(Player player) {
        if (Boolean.TRUE.equals(player.getIsDead())) {
            return true;
        }
        Integer injured = player.getIsInjured();
        return injured != null && injured >= 3;
    }

    private static boolean isSeverelyInjuredActive(Player player) {
        if (Boolean.TRUE.equals(player.getIsSeverelyInjured())) {
            return true;
        }
        Integer injured = player.getIsInjured();
        return injured != null && injured >= 2;
    }

    private static boolean isInjuredActive(Player player) {
        Integer injured = player.getIsInjured();
        return injured != null && injured >= 1;
    }

    public static List<Map<String, Object>> buildStatusList(Player player) {
        if (player == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        if (isDeadActive(player)) {
            list.add(statusEntry("死亡", 5, DEAD_DESC, "dead"));
        }
        if (isSeverelyInjuredActive(player) && !isDeadActive(player)) {
            list.add(statusEntry("重伤", 4, SEVERE_DESC, "severely_injured"));
        }
        if (isInjuredActive(player) && !isSeverelyInjuredActive(player) && !isDeadActive(player)) {
            list.add(statusEntry("受伤", 3, INJURED_DESC, "injured"));
        }
        if (Boolean.TRUE.equals(player.getIsOverworked())) {
            list.add(statusEntry("过劳", 2, OVERWORKED_DESC, "overworked"));
        }
        if (Boolean.TRUE.equals(player.getIsWeak())) {
            list.add(statusEntry("虚弱", 1, WEAK_DESC, "weak"));
        }
        return list;
    }

    private static Map<String, Object> statusEntry(String name, int severity, String description, String key) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", name);
        m.put("severity", severity);
        m.put("description", description);
        m.put("key", key);
        return m;
    }

    /** 格斗技能在战斗中是否无效 */
    public static boolean combatMeleeDisabled(Player player) {
        if (player == null) {
            return false;
        }
        if (isDeadActive(player) || isSeverelyInjuredActive(player)) {
            return true;
        }
        return Boolean.TRUE.equals(player.getIsWeak()) || isInjuredActive(player);
    }

    /** 射击技能在战斗中是否无效 */
    public static boolean combatRangedDisabled(Player player) {
        if (player == null) {
            return false;
        }
        if (isDeadActive(player) || isSeverelyInjuredActive(player)) {
            return true;
        }
        return Boolean.TRUE.equals(player.getIsWeak());
    }

    public static Map<String, Object> combatFlags(Player player) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("combatMeleeDisabled", combatMeleeDisabled(player));
        m.put("combatRangedDisabled", combatRangedDisabled(player));
        m.put("combatSkillsDisabled", combatMeleeDisabled(player) && combatRangedDisabled(player));
        m.put("isDead", isDeadActive(player));
        m.put("isSeverelyInjured", isSeverelyInjuredActive(player));
        m.put("isWeak", Boolean.TRUE.equals(player.getIsWeak()));
        m.put("isInjured", isInjuredActive(player));
        m.put("isOverworked", Boolean.TRUE.equals(player.getIsOverworked()));
        return m;
    }
}
