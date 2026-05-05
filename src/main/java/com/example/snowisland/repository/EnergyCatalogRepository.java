package com.example.snowisland.repository;

import com.example.snowisland.entity.EnergyCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnergyCatalogRepository extends JpaRepository<EnergyCatalog, String> {
    List<EnergyCatalog> findAllByOrderBySortOrderAsc();
}
