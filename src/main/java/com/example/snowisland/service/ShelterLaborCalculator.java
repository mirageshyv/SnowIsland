package com.example.snowisland.service;

import com.example.snowisland.entity.Job;
import com.example.snowisland.entity.Player;

import java.util.*;

/**
 * 避难所劳工建造值：基础 4；生产职业 +1；压榨 +3。逃役者贡献 0。
 */
public final class ShelterLaborCalculator {

    public static final int BASE_BUILD_VALUE = 4;
    public static final int PRODUCTION_JOB_BONUS = 1;
    public static final int EXPLOIT_BONUS = 3;

    private static final Set<String> PRODUCTION_JOB_NAMES = new HashSet<>(Arrays.asList(
            "渔民", "农户", "伐木工", "矿工", "猎户"
    ));

    private static final String[] PRODUCTION_SKILL_KEYWORDS = {
            "食物生产", "伐木", "挖掘", "炼铁"
    };

    private ShelterLaborCalculator() {
    }

    public static boolean isProductionLaborJobByName(String jobName) {
        if (jobName == null || jobName.trim().isEmpty()) {
            return false;
        }
        return PRODUCTION_JOB_NAMES.contains(jobName.trim());
    }

    public static boolean isProductionLaborJobByNameAndSkills(String jobName, String jobSkills) {
        if (isProductionLaborJobByName(jobName)) {
            return true;
        }
        if (jobSkills == null || jobSkills.isEmpty()) {
            return false;
        }
        for (String kw : PRODUCTION_SKILL_KEYWORDS) {
            if (jobSkills.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isProductionLaborJob(Player player, Map<Integer, Job> jobsById) {
        if (player == null || player.getJobId() == null || jobsById == null) {
            return false;
        }
        Job job = jobsById.get(player.getJobId());
        if (job == null) {
            return false;
        }
        if (job.getName() != null && PRODUCTION_JOB_NAMES.contains(job.getName().trim())) {
            return true;
        }
        String skills = job.getSkills();
        if (skills == null || skills.isEmpty()) {
            return false;
        }
        for (String kw : PRODUCTION_SKILL_KEYWORDS) {
            if (skills.contains(kw)) {
                return true;
            }
        }
        return false;
    }

    public static int calculateBuildValue(boolean productionJob, boolean exploited, boolean escaped) {
        if (escaped) {
            return 0;
        }
        int value = BASE_BUILD_VALUE;
        if (productionJob) {
            value += PRODUCTION_JOB_BONUS;
        }
        if (exploited) {
            value += EXPLOIT_BONUS;
        }
        return value;
    }

    public static int calculateBuildValue(Player player, Map<Integer, Job> jobsById, boolean exploited, boolean escaped) {
        boolean production = isProductionLaborJob(player, jobsById);
        return calculateBuildValue(production, exploited, escaped);
    }

    public static String laborTypeLabel(boolean productionJob, boolean exploited) {
        if (exploited && productionJob) {
            return "职业（压榨）";
        }
        if (exploited) {
            return "普通（压榨）";
        }
        if (productionJob) {
            return "职业劳工";
        }
        return "普通劳工";
    }

    public static String buildValueBreakdown(boolean productionJob, boolean exploited, boolean escaped) {
        if (escaped) {
            return "逃役（0）";
        }
        List<String> parts = new ArrayList<>();
        parts.add("基础" + BASE_BUILD_VALUE);
        if (productionJob) {
            parts.add("职业+" + PRODUCTION_JOB_BONUS);
        }
        if (exploited) {
            parts.add("压榨+" + EXPLOIT_BONUS);
        }
        return String.join(" + ", parts);
    }
}
