package com.example.snowisland.util;

/**
 * Shared survival math for NPC daily consume, reserved trade stock, and status.
 * Heat: 1 kg wood = 1, 1 kg fuel = 15 (same as players).
 */
public final class NpcSurvivalMath {

    public static final String STATUS_NORMAL = "正常";
    public static final String STATUS_WEAK = "虚弱";
    public static final String STATUS_DEAD = "死亡";
    public static final String STATUS_MISSING = "失踪";
    public static final String STATUS_ARRESTED = "被捕";

    private NpcSurvivalMath() {
    }

    public static boolean isGone(String status) {
        return STATUS_DEAD.equals(status)
                || STATUS_MISSING.equals(status)
                || STATUS_ARRESTED.equals(status);
    }

    public static boolean participatesInDailySurvival(String status) {
        return !isGone(status);
    }

    public static int sellableFood(int foodKg, int requiredFood) {
        return Math.max(0, foodKg - Math.max(0, requiredFood));
    }

    public static int sellableWood(int woodKg, int fuelKg, int requiredHeat) {
        int reservedWood = Math.max(0, requiredHeat - fuelKg * PlayerConsumptionHeat.FUEL_HEAT_PER_KG);
        return Math.max(0, woodKg - reservedWood);
    }

    public static int sellableFuel(int woodKg, int fuelKg, int requiredHeat) {
        int reservedFuel;
        if (woodKg >= requiredHeat) {
            reservedFuel = 0;
        } else {
            int need = requiredHeat - woodKg;
            reservedFuel = (need + PlayerConsumptionHeat.FUEL_HEAT_PER_KG - 1)
                    / PlayerConsumptionHeat.FUEL_HEAT_PER_KG;
        }
        return Math.max(0, fuelKg - reservedFuel);
    }

    public static int currentHeat(int woodKg, int fuelKg) {
        return woodKg * PlayerConsumptionHeat.WOOD_HEAT_PER_KG
                + fuelKg * PlayerConsumptionHeat.FUEL_HEAT_PER_KG;
    }

    public static boolean reserveHolds(int foodKg, int woodKg, int fuelKg, int requiredFood, int requiredHeat) {
        return foodKg >= requiredFood && currentHeat(woodKg, fuelKg) >= requiredHeat;
    }

    /**
     * After a missed/met day: gone statuses stay; miss while weak → death;
     * miss otherwise → weak; meet while weak → normal.
     */
    public static String nextStatusAfterDay(String status, boolean requirementsMet) {
        if (isGone(status)) {
            return status == null ? STATUS_NORMAL : status;
        }
        if (requirementsMet) {
            if (STATUS_WEAK.equals(status)) {
                return STATUS_NORMAL;
            }
            return status == null || status.isEmpty() ? STATUS_NORMAL : status;
        }
        if (STATUS_WEAK.equals(status)) {
            return STATUS_DEAD;
        }
        return STATUS_WEAK;
    }

    public static HeatSpend spendHeat(int woodKg, int fuelKg, int requiredHeat) {
        HeatSpend spend = new HeatSpend();
        int need = Math.max(0, requiredHeat);
        int woodUse = Math.min(woodKg, need);
        spend.woodUsed = woodUse;
        need -= woodUse * PlayerConsumptionHeat.WOOD_HEAT_PER_KG;
        if (need > 0) {
            int fuelNeed = (need + PlayerConsumptionHeat.FUEL_HEAT_PER_KG - 1)
                    / PlayerConsumptionHeat.FUEL_HEAT_PER_KG;
            spend.fuelUsed = Math.min(fuelKg, fuelNeed);
            need -= spend.fuelUsed * PlayerConsumptionHeat.FUEL_HEAT_PER_KG;
        }
        spend.heatGained = woodUse * PlayerConsumptionHeat.WOOD_HEAT_PER_KG
                + spend.fuelUsed * PlayerConsumptionHeat.FUEL_HEAT_PER_KG;
        spend.shortfall = Math.max(0, requiredHeat - spend.heatGained);
        return spend;
    }

    public static final class HeatSpend {
        public int woodUsed;
        public int fuelUsed;
        public int heatGained;
        public int shortfall;
    }

    /** Local copy of player heat constants so this util has no service import. */
    private static final class PlayerConsumptionHeat {
        static final int WOOD_HEAT_PER_KG = 1;
        static final int FUEL_HEAT_PER_KG = 15;
    }
}
