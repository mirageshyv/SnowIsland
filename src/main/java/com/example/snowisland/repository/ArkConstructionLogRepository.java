package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkConstructionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ArkConstructionLogRepository extends JpaRepository<ArkConstructionLog, Integer> {

    List<ArkConstructionLog> findAllByOrderByGameDayDescCreatedAtDesc();

    List<ArkConstructionLog> findByPlayerIdOrderByGameDayDescCreatedAtDesc(Integer playerId);

    List<ArkConstructionLog> findByGameDayOrderByCreatedAtDesc(Integer gameDay);

    @Query("SELECT COALESCE(SUM(a.woodChange), 0) FROM ArkConstructionLog a WHERE a.gameDay = :gameDay")
    BigDecimal sumTodayWood(Integer gameDay);

    @Query("SELECT COALESCE(SUM(a.metalChange), 0) FROM ArkConstructionLog a WHERE a.gameDay = :gameDay")
    BigDecimal sumTodayMetal(Integer gameDay);

    @Query("SELECT COALESCE(SUM(a.asphaltChange), 0) FROM ArkConstructionLog a WHERE a.gameDay = :gameDay")
    BigDecimal sumTodayAsphalt(Integer gameDay);

    @Query("SELECT COALESCE(SUM(a.workUnits), 0) FROM ArkConstructionLog a WHERE a.gameDay = :gameDay")
    Integer sumTodayWorkUnits(Integer gameDay);
}
