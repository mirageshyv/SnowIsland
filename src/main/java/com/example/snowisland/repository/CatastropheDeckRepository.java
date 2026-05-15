package com.example.snowisland.repository;

import com.example.snowisland.entity.CatastropheDeck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatastropheDeckRepository extends JpaRepository<CatastropheDeck, Integer> {
    List<CatastropheDeck> findByIsDrawnFalseAndIsUsedFalse();
    List<CatastropheDeck> findByIsDrawnTrueAndIsUsedFalse();
    List<CatastropheDeck> findByCardId(Integer cardId);
    @Query(value = "SELECT d FROM CatastropheDeck d WHERE d.isDrawn = false AND d.isUsed = false ORDER BY RAND()", nativeQuery = true)
    List<CatastropheDeck> findRandomUndrawnCards();
}