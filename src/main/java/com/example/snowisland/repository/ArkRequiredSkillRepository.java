package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkRequiredSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArkRequiredSkillRepository extends JpaRepository<ArkRequiredSkill, Integer> {

    Optional<ArkRequiredSkill> findBySkillCode(String skillCode);

    List<ArkRequiredSkill> findAllBySkillTypeOrderByPriorityAsc(String skillType);

    List<ArkRequiredSkill> findByIsRequiredTrue();
}
