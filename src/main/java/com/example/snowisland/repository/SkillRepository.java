package com.example.snowisland.repository;

import com.example.snowisland.entity.Skill;
import com.example.snowisland.entity.Skill.Faction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {

    Optional<Skill> findByName(String name);

    boolean existsByName(String name);

    List<Skill> findByFaction(Faction faction);

    List<Skill> findByFactionOrderByName(Faction faction);

    List<Skill> findAllByOrderByFactionAscNameAsc();
}