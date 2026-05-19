package com.example.snowisland.repository;

import com.example.snowisland.entity.GameDaySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameDaySettingsRepository extends JpaRepository<GameDaySettings, Integer> {
}
