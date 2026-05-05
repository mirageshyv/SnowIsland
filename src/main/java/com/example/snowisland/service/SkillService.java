package com.example.snowisland.service;

import com.example.snowisland.entity.Skill;
import com.example.snowisland.entity.Skill.Faction;
import com.example.snowisland.repository.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getAllSkills() {
        return skillRepository.findAllByOrderByFactionAscNameAsc();
    }

    public Optional<Skill> getSkillById(Integer id) {
        return skillRepository.findById(id);
    }

    public Optional<Skill> getSkillByName(String name) {
        return skillRepository.findByName(name);
    }

    public List<Skill> getSkillsByFaction(Faction faction) {
        return skillRepository.findByFactionOrderByName(faction);
    }

    public Skill saveSkill(Skill skill) {
        return skillRepository.save(skill);
    }

    public void deleteSkill(Integer id) {
        skillRepository.deleteById(id);
    }

    public Map<String, Object> getSkillsGroupedByFaction() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        Map<String, List<Skill>> skillsByFaction = new HashMap<>();
        for (Faction faction : Faction.values()) {
            List<Skill> skills = skillRepository.findByFactionOrderByName(faction);
            skillsByFaction.put(faction.name(), skills);
        }
        result.put("skillsByFaction", skillsByFaction);
        return result;
    }

    public Map<String, Object> getSkillsByFactionName(String factionName) {
        Map<String, Object> result = new HashMap<>();
        try {
            Faction faction = Faction.valueOf(factionName);
            List<Skill> skills = skillRepository.findByFactionOrderByName(faction);
            result.put("success", true);
            result.put("faction", factionName);
            result.put("skills", skills);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", "无效的阵营名称: " + factionName);
        }
        return result;
    }
}