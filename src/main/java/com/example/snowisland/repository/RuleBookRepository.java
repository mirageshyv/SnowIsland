package com.example.snowisland.repository;

import com.example.snowisland.entity.RuleBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuleBookRepository extends JpaRepository<RuleBook, Integer> {
    List<RuleBook> findBySectionOrderByOrderNum(String section);
    List<RuleBook> findBySection(String section);
    List<RuleBook> findAllByOrderByOrderNum();
}
