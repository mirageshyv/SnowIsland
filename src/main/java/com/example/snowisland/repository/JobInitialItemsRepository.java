package com.example.snowisland.repository;

import com.example.snowisland.entity.JobInitialItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobInitialItemsRepository extends JpaRepository<JobInitialItems, Integer> {

    List<JobInitialItems> findByJobId(Integer jobId);

    List<JobInitialItems> findByJobIdOrderByItemType(Integer jobId);

    void deleteByJobId(Integer jobId);
}