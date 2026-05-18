package com.example.snowisland.repository;

import com.example.snowisland.entity.ShelterDailyLabor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterDailyLaborRepository extends JpaRepository<ShelterDailyLabor, Integer> {

    List<ShelterDailyLabor> findByGameDayOrderByPlayerIdAsc(Integer gameDay);

    List<ShelterDailyLabor> findAllByOrderByGameDayDescPlayerIdAsc();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM ShelterDailyLabor l WHERE l.gameDay = :gameDay")
    void deleteByGameDay(@Param("gameDay") Integer gameDay);

    @Query("SELECT COALESCE(SUM(l.buildValue), 0) FROM ShelterDailyLabor l " +
            "WHERE l.gameDay IN (SELECT d.gameDay FROM ShelterLaborDay d WHERE d.verified = true)")
    int sumVerifiedBuildValue();
}
