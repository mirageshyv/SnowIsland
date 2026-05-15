package com.example.snowisland.repository;

import com.example.snowisland.entity.DrawnCards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrawnCardsRepository extends JpaRepository<DrawnCards, Integer> {
    List<DrawnCards> findByDrawRound(Integer drawRound);
    List<DrawnCards> findByDrawRoundOrderByPositionAsc(Integer drawRound);
    @Query("SELECT MAX(d.drawRound) FROM DrawnCards d")
    Integer findMaxDrawRound();
}