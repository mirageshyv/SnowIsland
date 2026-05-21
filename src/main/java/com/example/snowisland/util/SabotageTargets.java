package com.example.snowisland.util;

import java.util.*;

public final class SabotageTargets {

    private static final List<int[]> ALLOWED = Arrays.asList(
            new int[] { 2, 1 },
            new int[] { 4, 2 },
            new int[] { 5, 3 },
            new int[] { 10, 12 },
            new int[] { 11, 15 },
            new int[] { 12, 15 },
            new int[] { 13, 15 },
            new int[] { 15, 18 }
    );

    private SabotageTargets() {}

    public static boolean isAllowed(Integer facilityId, Integer locationId) {
        if (facilityId == null || locationId == null) return false;
        for (int[] pair : ALLOWED) {
            if (pair[0] == facilityId && pair[1] == locationId) return true;
        }
        return false;
    }

    public static String labelFor(Integer facilityId) {
        if (facilityId == null) return "未知设施";
        switch (facilityId) {
            case 2: return "发电机（警察局）";
            case 4: return "发电机（镇长厅）";
            case 5: return "电报机（邮局）";
            case 10: return "烘焙炉（面包店）";
            case 11: return "木板蒸汽箱（伐木营地）";
            case 12: return "拖拉机（伐木营地）";
            case 13: return "发电机（伐木营地）";
            case 15: return "切石机（矿场）";
            default: return "设施#" + facilityId;
        }
    }
}
