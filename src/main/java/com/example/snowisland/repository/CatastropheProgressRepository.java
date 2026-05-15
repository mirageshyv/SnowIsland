package com.example.snowisland.repository;

import com.example.snowisland.entity.CatastropheProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatastropheProgressRepository extends JpaRepository<CatastropheProgress, Integer> {
    CatastropheProgress findFirstByOrderByIdAsc();
}