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
    // openSMILE 실행파일 및 config 경로 (macOS 빌드 기준)
    final String openSmileExecPath = "/Users/ryugi62/Desktop/해커톤/opensmile/build/progsrc/smilextract/SMILExtract";
    final String openSmileConfigPath = "/Users/ryugi62/Desktop/해커톤/opensmile/config/is09-13/IS13_ComParE.conf";
    OpenSmileService openSmileService = new OpenSmileService(openSmileExecPath);
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

            // webm → wav 변환 (ffmpeg 필요)
            String wavPath = filePath.replaceAll("\\.webm$", ".wav");
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y", "-i", filePath, "-ar", "16000", "-ac", "1", wavPath
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[ffmpeg] " + line);
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.out.println("❌ [ffmpeg] 변환 실패: " + filePath + " → " + wavPath);
                } else {
                    System.out.println("✅ [ffmpeg] 변환 성공: " + wavPath);
                }
            } catch (Exception e) {
                System.out.println("❌ [ffmpeg] 변환 예외: " + e.getMessage());
            }

            // openSMILE 음성 감정 분석(비언어적 신호) - 변환된 wav 파일 사용
            java.util.concurrent.Future<Map<String, String>> openSmileFuture = executor.submit(() -> openSmileService.analyzeEmotionWithOpenSmile(wavPath, openSmileConfigPath));

            // 감정분석, openSMILE, AI 응답을 동시에 시작
            java.util.concurrent.Future<EmotionAnalysisResult> analysisFuture = executor.submit(() -> openAIService.analyzeTranscriptEmotion(transcript, null));
            java.util.concurrent.Future<String> assistantFuture = executor.submit(() -> openAIService.generateResponseWithVoice(transcript, null));

            EmotionAnalysisResult analysis = null;
            Map<String, String> openSmileResult = null;
            StringBuilder errorBuilder = new StringBuilder();
            try {
                analysis = analysisFuture.get();
            } catch (Exception e) {
                errorBuilder.append("[감정분석 예외] ").append(e.getMessage()).append("; ");
                System.out.println("❌ [AsyncProcessing] 감정분석 예외: " + e.getMessage());
            }
            try {
                openSmileResult = openSmileFuture.get();
            } catch (Exception e) {
                errorBuilder.append("[openSMILE 예외] ").append(e.getMessage()).append("; ");
                System.out.println("❌ [AsyncProcessing] openSMILE 예외: " + e.getMessage());
            }
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
                errorBuilder.append("[감정분석 결과 없음]");
            }
            if (errorBuilder.length() > 0) {
                j.setErrorMessage(errorBuilder.toString());
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
