package com.duckchat.api.entity;

import com.duckchat.api.dto.EmotionAnalysisResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processing_job")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingJob {
    @Id
    private String id;

    private String status; // PENDING, RUNNING, DONE, FAILED

    @Lob
    private String transcript;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String analysisJson; // EmotionAnalysisResult raw json

    @Lob
    private String assistantResponse;

    private String errorMessage;

    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
