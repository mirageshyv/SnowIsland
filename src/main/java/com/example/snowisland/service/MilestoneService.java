package com.example.snowisland.service;

import com.example.snowisland.entity.Milestone;
import com.example.snowisland.entity.Player;
import com.example.snowisland.repository.MilestoneRepository;
import com.example.snowisland.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private PlayerRepository playerRepository;

    public List<Milestone> getAllMilestones() {
        return milestoneRepository.findAllByOrderByOrderNumberAsc();
    }

    public Map<String, Object> getMilestoneProgress() {
        long completed = milestoneRepository.countCompleted();
        long total = milestoneRepository.countTotal();
        int requiredForFull = 3;
        double percentage = completed >= requiredForFull ? 100.0 : (completed * 100.0 / requiredForFull);

        Map<String, Object> progress = new HashMap<>();
        progress.put("completed", completed);
        progress.put("total", total);
        progress.put("percentage", Math.round(percentage * 100.0) / 100.0);

        return progress;
    }

    @Transactional
    public Milestone completeMilestone(Integer milestoneId) {
        Optional<Milestone> optionalMilestone = milestoneRepository.findById(milestoneId);
        if (!optionalMilestone.isPresent()) {
            throw new RuntimeException("里程碑不存在");
        }

        Milestone milestone = optionalMilestone.get();
        if (!milestone.getIsCompleted()) {
            milestone.setIsCompleted(true);
            milestone.setCompletedAt(LocalDateTime.now());
            return milestoneRepository.save(milestone);
        }

        return milestone;
    }

    @Transactional
    public Milestone toggleMilestone(Integer milestoneId) {
        Optional<Milestone> optionalMilestone = milestoneRepository.findById(milestoneId);
        if (!optionalMilestone.isPresent()) {
            throw new RuntimeException("里程碑不存在");
        }

        Milestone milestone = optionalMilestone.get();
        if (milestone.getIsCompleted()) {
            milestone.setIsCompleted(false);
            milestone.setCompletedAt(null);
        } else {
            milestone.setIsCompleted(true);
            milestone.setCompletedAt(LocalDateTime.now());
        }

        return milestoneRepository.save(milestone);
    }

    public boolean isPlayerRebel(Integer playerId) {
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        if (!optionalPlayer.isPresent()) {
            return false;
        }

        Player player = optionalPlayer.get();
        Player.Faction faction = player.getFaction();
        return faction != null && faction == Player.Faction.反叛者;
    }

    public boolean hasAccess(Integer playerId, String userRole) {
        if ("dm".equalsIgnoreCase(userRole)) {
            return true;
        }
        return isPlayerRebel(playerId);
    }
}