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
        System.out.println("🔄 [AsyncProcessing] 작업 시작 - jobId: " + jobId + ", filePath: " + filePath);
        
        ProcessingJob j = jobRepository.findById(jobId).orElse(null);
        if (j == null) {
            System.out.println("❌ [AsyncProcessing] Job을 찾을 수 없음: " + jobId);
            return new AsyncResult<>(null);
        }
        
        System.out.println("🟡 [AsyncProcessing] Job 상태를 RUNNING으로 변경: " + jobId);
        j.setStatus("RUNNING");
        jobRepository.save(j);
        
        try {
            System.out.println("🎤 [AsyncProcessing] Whisper 전사 시작...");
            String transcript = openAIService.transcribeAudioFile(filePath, language);
            System.out.println("📝 [AsyncProcessing] 전사 완료: " + (transcript != null ? transcript.substring(0, Math.min(50, transcript.length())) + "..." : "null"));
            j.setTranscript(transcript);
            
            System.out.println("🧠 [AsyncProcessing] 감정 분석 시작...");
            EmotionAnalysisResult analysis = openAIService.analyzeTranscriptEmotion(transcript, null);
            if (analysis != null) {
                System.out.println("💭 [AsyncProcessing] 감정 분석 완료: " + analysis.getRawJson());
                j.setAnalysisJson(analysis.getRawJson());
            } else {
                System.out.println("⚠️ [AsyncProcessing] 감정 분석 결과 없음");
            }
            
            System.out.println("🤖 [AsyncProcessing] AI 응답 생성 시작...");
            String assistant = openAIService.generateResponseWithVoice(transcript, null);
            System.out.println("💬 [AsyncProcessing] AI 응답 완료: " + (assistant != null ? assistant.substring(0, Math.min(50, assistant.length())) + "..." : "null"));
            j.setAssistantResponse(assistant);
            
            j.setStatus("DONE");
            jobRepository.save(j);
            System.out.println("✅ [AsyncProcessing] 작업 완료: " + jobId);
            
        } catch (Exception e) {
            System.out.println("❌ [AsyncProcessing] 작업 실패 - jobId: " + jobId + ", 오류: " + e.getMessage());
            e.printStackTrace();
            j.setStatus("FAILED");
            j.setErrorMessage(e.getMessage());
            jobRepository.save(j);
        }
        return new AsyncResult<>(j);
    }
}
