package com.example.snowisland.controller;

import com.example.snowisland.entity.Job;
import com.example.snowisland.entity.JobInitialItems;
import com.example.snowisland.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobService jobService;

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        List<Job> jobs = jobService.getAllJobs();
        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable Integer id) {
        Optional<Job> job = jobService.getJobById(id);
        if (job.isPresent()) {
            return ResponseEntity.ok(job.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> getJobWithInitialItems(@PathVariable Integer id) {
        Map<String, Object> result = jobService.getJobWithInitialItems(id);
        if ((Boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/all-with-items")
    public ResponseEntity<List<Map<String, Object>>> getAllJobsWithInitialItems() {
        List<Map<String, Object>> jobs = jobService.getAllJobsWithInitialItems();
        return ResponseEntity.ok(jobs);
    }

    @PostMapping
    public ResponseEntity<Job> createJob(@RequestBody Job job) {
        Job savedJob = jobService.saveJob(job);
        return ResponseEntity.ok(savedJob);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Integer id, @RequestBody Job job) {
        Optional<Job> existingJob = jobService.getJobById(id);
        if (existingJob.isPresent()) {
            job.setId(id);
            Job updatedJob = jobService.saveJob(job);
            return ResponseEntity.ok(updatedJob);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Integer id) {
        Optional<Job> job = jobService.getJobById(id);
        if (job.isPresent()) {
            jobService.deleteJob(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> saveInitialItems(@PathVariable Integer id, @RequestBody List<JobInitialItems> items) {
        Optional<Job> job = jobService.getJobById(id);
        if (job.isPresent()) {
            jobService.saveInitialItems(id, items);
            return ResponseEntity.ok(jobService.getJobWithInitialItems(id));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/initial-items")
    public ResponseEntity<List<JobInitialItems>> getInitialItems(@PathVariable Integer id) {
        List<JobInitialItems> items = jobService.getInitialItemsByJobId(id);
        return ResponseEntity.ok(items);
    }
}