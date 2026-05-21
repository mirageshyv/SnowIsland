package com.example.snowisland.repository;

import com.example.snowisland.entity.LorePlayerGrant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LorePlayerGrantRepository extends JpaRepository<LorePlayerGrant, Long> {
    List<LorePlayerGrant> findAllByOrderByGrantedAtAsc();

    List<LorePlayerGrant> findByLoreSlugOrderByGrantedAtAsc(String loreSlug);

    List<LorePlayerGrant> findByPlayerIdOrderByGrantedAtAsc(Integer playerId);

    boolean existsByPlayerIdAndLoreSlug(Integer playerId, String loreSlug);

    Optional<LorePlayerGrant> findByPlayerIdAndLoreSlug(Integer playerId, String loreSlug);

    void deleteByPlayerIdAndLoreSlug(Integer playerId, String loreSlug);
}
