package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArkConfigRepository extends JpaRepository<ArkConfig, Integer> {
}
