package com.example.snowisland.controller;

import com.example.snowisland.entity.Skill;
import com.example.snowisland.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skills")
@CrossOrigin(origins = "*")
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills() {
        List<Skill> skills = skillService.getAllSkills();
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSkillById(@PathVariable Integer id) {
        Optional<Skill> skill = skillService.getSkillById(id);
        if (skill.isPresent()) {
            return ResponseEntity.ok(skill.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Skill> createSkill(@RequestBody Skill skill) {
        Skill savedSkill = skillService.saveSkill(skill);
        return ResponseEntity.ok(savedSkill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable Integer id, @RequestBody Skill skill) {
        Optional<Skill> existingSkill = skillService.getSkillById(id);
        if (existingSkill.isPresent()) {
            skill.setId(id);
            Skill updatedSkill = skillService.saveSkill(skill);
            return ResponseEntity.ok(updatedSkill);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable Integer id) {
        Optional<Skill> skill = skillService.getSkillById(id);
        if (skill.isPresent()) {
            skillService.deleteSkill(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}