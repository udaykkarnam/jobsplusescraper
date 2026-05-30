package com.JobPulse_Scraper.repository;

import com.JobPulse_Scraper.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    boolean existsByJobUrl(String jobUrl);

    List<Job> findByCreatedAtAfter(LocalDateTime time);
}
