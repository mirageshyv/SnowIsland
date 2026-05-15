package com.example.snowisland.repository;

import com.example.snowisland.entity.ArkSail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArkSailRepository extends JpaRepository<ArkSail, Integer> {
}
