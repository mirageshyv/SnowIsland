package com.example.snowisland.repository;

import com.example.snowisland.entity.GameActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameActivityLogRepository extends JpaRepository<GameActivityLog, Long> {

    List<GameActivityLog> findTop500ByOrderByCreatedAtDesc();

    List<GameActivityLog> findTop500ByGameDayOrderByCreatedAtDesc(Integer gameDay);
}
