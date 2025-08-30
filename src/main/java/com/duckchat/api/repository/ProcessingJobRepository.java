package com.duckchat.api.repository;

import com.duckchat.api.entity.ProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingJobRepository extends JpaRepository<ProcessingJob, String> {
}
