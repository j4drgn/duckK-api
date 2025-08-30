package com.duckchat.api.service;

import com.duckchat.api.dto.EmotionAnalysisResult;
import lombok.Data;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.UUID;

@Service
public class AsyncProcessingService {

    @Data
    public static class Job {
        private String id;
        private String status; // PENDING, RUNNING, DONE, FAILED
        private String transcript;
        private EmotionAnalysisResult analysisResult;
        private String assistantResponse;
        private String errorMessage;
    }

    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    public Job createJob() {
        Job j = new Job();
        j.setId(UUID.randomUUID().toString());
        j.setStatus("PENDING");
        jobs.put(j.getId(), j);
        return j;
    }

    public Job getJob(String id) {
        return jobs.get(id);
    }

    @Async("taskExecutor")
    public Future<Job> runTranscriptionAndAnalysis(String jobId, String filePath, String language, OpenAIService openAIService) {
        Job j = jobs.get(jobId);
        if (j == null) return new AsyncResult<>(null);
        j.setStatus("RUNNING");
        try {
            String transcript = openAIService.transcribeAudioFile(filePath, language);
            j.setTranscript(transcript);
            EmotionAnalysisResult analysis = openAIService.analyzeTranscriptEmotion(transcript, null);
            j.setAnalysisResult(analysis);
            String assistant = openAIService.generateResponseWithVoice(transcript, null);
            j.setAssistantResponse(assistant);
            j.setStatus("DONE");
        } catch (Exception e) {
            j.setStatus("FAILED");
            j.setErrorMessage(e.getMessage());
        }
        return new AsyncResult<>(j);
    }
}
