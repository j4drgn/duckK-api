package com.duckchat.api.service;

import com.duckchat.api.dto.EmotionAnalysisResult;
import com.duckchat.api.entity.ProcessingJob;
import com.duckchat.api.repository.ProcessingJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.Future;
import java.util.UUID;

@Service
public class AsyncProcessingService {

    private final ProcessingJobRepository jobRepository;

    @Autowired
    public AsyncProcessingService(ProcessingJobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public ProcessingJob createJob() {
        ProcessingJob j = new ProcessingJob();
        j.setId(UUID.randomUUID().toString());
        j.setStatus("PENDING");
        jobRepository.save(j);
        return j;
    }

    public ProcessingJob getJob(String id) {
        Optional<ProcessingJob> opt = jobRepository.findById(id);
        return opt.orElse(null);
    }

    @Async("taskExecutor")
    public Future<ProcessingJob> runTranscriptionAndAnalysis(String jobId, String filePath, String language, OpenAIService openAIService) {
        ProcessingJob j = jobRepository.findById(jobId).orElse(null);
        if (j == null) return new AsyncResult<>(null);
        j.setStatus("RUNNING");
        jobRepository.save(j);
        try {
            String transcript = openAIService.transcribeAudioFile(filePath, language);
            j.setTranscript(transcript);
            EmotionAnalysisResult analysis = openAIService.analyzeTranscriptEmotion(transcript, null);
            if (analysis != null) {
                j.setAnalysisJson(analysis.getRawJson());
            }
            String assistant = openAIService.generateResponseWithVoice(transcript, null);
            j.setAssistantResponse(assistant);
            j.setStatus("DONE");
            jobRepository.save(j);
        } catch (Exception e) {
            j.setStatus("FAILED");
            j.setErrorMessage(e.getMessage());
            jobRepository.save(j);
        }
        return new AsyncResult<>(j);
    }
}
