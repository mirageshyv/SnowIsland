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

    public static final String BOUND_DESC =
            "绳索勒进皮肉，四肢再难听从意志。（无法执行自由行动：生产、调查、隐藏、搬运、战斗等；无法执行夜间行动：密谋、暗杀、探索等；无法使用技能：格斗、射击、潜行等）";

    public static final String BOUND_DENY_MESSAGE = "你处于束缚状态，无法执行该行动";

    private PlayerStatusCatalog() {
    }

    /** 仅检查 player.is_bound 列；含标记的完整判定见 TradeRestrictionService.isBoundActive。 */
    public static boolean isBoundFlag(Player player) {
        return player != null && Boolean.TRUE.equals(player.getIsBound());
    }

    private static boolean isDeadActive(Player player) {
        if (Boolean.TRUE.equals(player.getIsDead())) {
            return true;
        }
        Integer injured = player.getIsInjured();
        return injured != null && injured >= 3;
    }

    public static boolean isSeverelyInjuredActive(Player player) {
        if (player == null) {
            return false;
        }
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
        return buildStatusList(player, isBoundFlag(player));
    }

    public static List<Map<String, Object>> buildStatusList(Player player, boolean boundActive) {
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
        if (boundActive) {
            list.add(statusEntry("束缚", 3, BOUND_DESC, "bound"));
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
        return combatMeleeDisabled(player, isBoundFlag(player));
    }

    public static boolean combatMeleeDisabled(Player player, boolean boundActive) {
        if (player == null) {
            return false;
        }
        if (boundActive || isDeadActive(player) || isSeverelyInjuredActive(player)) {
            return true;
        }
        return Boolean.TRUE.equals(player.getIsWeak()) || isInjuredActive(player);
    }

    /** 射击技能在战斗中是否无效 */
    public static boolean combatRangedDisabled(Player player) {
        return combatRangedDisabled(player, isBoundFlag(player));
    }

    public static boolean combatRangedDisabled(Player player, boolean boundActive) {
        if (player == null) {
            return false;
        }
        if (boundActive || isDeadActive(player) || isSeverelyInjuredActive(player)) {
            return true;
        }
        return Boolean.TRUE.equals(player.getIsWeak());
    }

    public static Map<String, Object> combatFlags(Player player) {
        return combatFlags(player, isBoundFlag(player));
    }

    public static Map<String, Object> combatFlags(Player player, boolean boundActive) {
        Map<String, Object> m = new LinkedHashMap<>();
        boolean melee = combatMeleeDisabled(player, boundActive);
        boolean ranged = combatRangedDisabled(player, boundActive);
        m.put("combatMeleeDisabled", melee);
        m.put("combatRangedDisabled", ranged);
        m.put("combatSkillsDisabled", melee && ranged);
        m.put("isDead", isDeadActive(player));
        m.put("isSeverelyInjured", isSeverelyInjuredActive(player));
        m.put("isWeak", Boolean.TRUE.equals(player.getIsWeak()));
        m.put("isInjured", isInjuredActive(player));
        m.put("isOverworked", Boolean.TRUE.equals(player.getIsOverworked()));
        m.put("isBound", boundActive);
        return m;
    }
}
