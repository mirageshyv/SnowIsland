package com.example.snowisland.repository;

import com.example.snowisland.entity.PlayerMarker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PlayerMarkerRepository extends JpaRepository<PlayerMarker, Integer> {

    List<PlayerMarker> findByPlayerIdOrderByIdAsc(Integer playerId);

    List<PlayerMarker> findByPlayerIdInOrderByIdAsc(Collection<Integer> playerIds);

    List<PlayerMarker> findByPlayerIdAndVisibleToPlayerTrueOrderByIdAsc(Integer playerId);
}
