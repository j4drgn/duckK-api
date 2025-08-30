package com.duckchat.api.service;

import com.duckchat.api.dto.EmotionAnalysisResult;
import com.duckchat.api.entity.ProcessingJob;
import com.duckchat.api.repository.ProcessingJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map;
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
        // openSMILE config 경로(환경에 맞게 수정 필요)
        final String openSmileConfigPath = "/usr/local/opt/opensmile/config/emo/IS13_ComParE.conf";
        OpenSmileService openSmileService = new OpenSmileService();
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
            // Whisper, 감정분석, openSMILE, AI 응답을 병렬로 처리
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(4);
            java.util.concurrent.Future<String> transcriptFuture = executor.submit(() -> openAIService.transcribeAudioFile(filePath, language));
            // transcript가 준비되어야 감정분석/AI 응답이 가능하므로, transcript만 우선 빠르게 처리
            String transcript = transcriptFuture.get();
            System.out.println("📝 [AsyncProcessing] 전사 완료: " + (transcript != null ? transcript.substring(0, Math.min(50, transcript.length())) + "..." : "null"));
            j.setTranscript(transcript);
            jobRepository.save(j);

            // openSMILE 음성 감정 분석(비언어적 신호)
            java.util.concurrent.Future<Map<String, String>> openSmileFuture = executor.submit(() -> openSmileService.analyzeEmotionWithOpenSmile(filePath, openSmileConfigPath));

            // 감정분석, openSMILE, AI 응답을 동시에 시작
            java.util.concurrent.Future<EmotionAnalysisResult> analysisFuture = executor.submit(() -> openAIService.analyzeTranscriptEmotion(transcript, null));
            java.util.concurrent.Future<String> assistantFuture = executor.submit(() -> openAIService.generateResponseWithVoice(transcript, null));

            EmotionAnalysisResult analysis = analysisFuture.get();
            Map<String, String> openSmileResult = openSmileFuture.get();
            if (analysis != null) {
                System.out.println("💭 [AsyncProcessing] 감정 분석 완료: " + analysis.getRawJson());
                // openSMILE 결과를 analysisJson에 함께 저장(필요시 별도 필드 추가 가능)
                String combinedJson = analysis.getRawJson();
                if (openSmileResult != null && !openSmileResult.isEmpty()) {
                    combinedJson = combinedJson.replaceFirst("}$", ", \"openSmile\": " + new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(openSmileResult) + "}");
                }
                j.setAnalysisJson(combinedJson);
            } else {
                System.out.println("⚠️ [AsyncProcessing] 감정 분석 결과 없음");
            }

            String assistant = assistantFuture.get();
            System.out.println("💬 [AsyncProcessing] AI 응답 완료: " + (assistant != null ? assistant.substring(0, Math.min(50, assistant.length())) + "..." : "null"));
            j.setAssistantResponse(assistant);

            j.setStatus("DONE");
            jobRepository.save(j);
            executor.shutdown();
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
