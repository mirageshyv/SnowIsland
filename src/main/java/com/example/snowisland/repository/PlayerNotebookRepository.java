package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerNotebook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerNotebookRepository extends JpaRepository<PlayerNotebook, Integer> {

    List<PlayerNotebook> findByPlayerIdOrderBySortOrderAscIdAsc(Integer playerId);

    long countByPlayerId(Integer playerId);

    void deleteByPlayerId(Integer playerId);

    Optional<PlayerNotebook> findByIdAndPlayerId(Integer id, Integer playerId);

    Optional<PlayerNotebook> findTopByPlayerIdOrderBySortOrderDesc(Integer playerId);
}
