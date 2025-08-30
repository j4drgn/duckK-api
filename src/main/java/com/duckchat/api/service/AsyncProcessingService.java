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
import java.util.List;
import java.util.ArrayList;
import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import com.duckchat.api.entity.ChatMessage;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.UserRepository;
import com.duckchat.api.dto.openai.ChatCompletionRequest;
import com.duckchat.api.dto.ChatMessageRequest;
import com.duckchat.api.entity.ChatMessage.MessageType;

@Service
public class AsyncProcessingService {

    private final ProcessingJobRepository jobRepository;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @Autowired
    public AsyncProcessingService(ProcessingJobRepository jobRepository, ChatService chatService, UserRepository userRepository) {
        this.jobRepository = jobRepository;
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    public ProcessingJob createJob(Long userId) {
        ProcessingJob j = new ProcessingJob();
        j.setId(UUID.randomUUID().toString());
        j.setStatus("PENDING");
        j.setUserId(userId);
        jobRepository.save(j);
        return j;
    }

    public ProcessingJob getJob(String id) {
        Optional<ProcessingJob> opt = jobRepository.findById(id);
        return opt.orElse(null);
    }

    @Async("taskExecutor")
    public Future<ProcessingJob> runTranscriptionAndAnalysis(String jobId, String filePath, String language, Long chatSessionId, OpenAIService openAIService) {
    // openSMILE 실행파일 및 config 경로 (macOS 빌드 기준)
    final String openSmileExecPath = "/Users/ryugi62/Desktop/해커톤/opensmile/build/progsrc/smilextract/SMILExtract";
    final String openSmileConfigPath = "/Users/ryugi62/Desktop/해커톤/opensmile/config/is09-13/IS13_ComParE.conf";
    OpenSmileService openSmileService = new OpenSmileService(openSmileExecPath);
        System.out.println("[AsyncProcessing] 작업 시작 - jobId: " + jobId + ", filePath: " + filePath);

        ProcessingJob j = jobRepository.findById(jobId).orElse(null);
        if (j == null) {
            System.out.println("[AsyncProcessing] Job을 찾을 수 없음: " + jobId);
            return new AsyncResult<>(null);
        }

        // chatSessionId 설정
        j.setChatSessionId(chatSessionId != null ? chatSessionId.toString() : null);
        jobRepository.save(j);

        System.out.println("🟡 [AsyncProcessing] Job 상태를 RUNNING으로 변경: " + jobId);
        j.setStatus("RUNNING");
        jobRepository.save(j);

        try {
            // Whisper, 감정분석, openSMILE, AI 응답을 병렬로 처리
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(4);
            java.util.concurrent.Future<String> transcriptFuture = executor.submit(() -> openAIService.transcribeAudioFile(filePath, language));
            // transcript가 준비되어야 감정분석/AI 응답이 가능하므로, transcript만 우선 빠르게 처리
            String transcript = transcriptFuture.get();
            System.out.println("[AsyncProcessing] 전사 완료: " + (transcript != null ? transcript.substring(0, Math.min(50, transcript.length())) + "..." : "null"));
            j.setTranscript(transcript);
            jobRepository.save(j);

            // 대화 히스토리 구성 (chatSessionId가 있는 경우)
            List<ChatCompletionRequest.Message> messageHistory = new ArrayList<>();
            StringBuilder emotionSummary = new StringBuilder();
            if (chatSessionId != null && j.getUserId() != null) {
                try {
                    User user = userRepository.findById(j.getUserId()).orElse(null);
                    if (user != null) {
                        Optional<ChatSession> sessionOpt = chatService.getChatSession(chatSessionId, user);
                        if (sessionOpt.isPresent()) {
                            ChatSession session = sessionOpt.get();
                            List<ChatSessionMessage> sessionMessages = chatService.getSessionMessages(session);

                            // 시스템 메시지 추가 (히스토리에 추가하지 않음 - generateResponseWithHistoryAndVoice에서 처리)

                            // 이전 메시지 히스토리 추가 (감정 정보 포함)
                            for (ChatSessionMessage sessionMessage : sessionMessages) {
                                ChatMessage message = sessionMessage.getMessage();
                                String role = message.getType() == ChatMessage.MessageType.USER ? "user" : "assistant";
                                String content = message.getContent();

                                // 감정 정보가 있으면 내용에 포함하고 요약에도 추가
                                if (message.getEmotionType() != null && !message.getEmotionType().isEmpty()) {
                                    content += " [감정: " + message.getEmotionType() +
                                              (message.getEmotionScore() != null ? ", 점수: " + String.format("%.2f", message.getEmotionScore()) : "") + "]";

                                    // 최근 3개의 사용자 메시지 감정 요약
                                    if (role.equals("user") && emotionSummary.length() < 200) { // 요약 길이 제한
                                        if (emotionSummary.length() > 0) emotionSummary.append(", ");
                                        emotionSummary.append(message.getEmotionType());
                                    }
                                }

                                messageHistory.add(ChatCompletionRequest.Message.builder()
                                        .role(role)
                                        .content(content)
                                        .build());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("[AsyncProcessing] 대화 히스토리 조회 실패: " + e.getMessage());
                }
            }

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
                    System.out.println("[ffmpeg] 변환 실패: " + filePath + " → " + wavPath);
                } else {
                    System.out.println("[ffmpeg] 변환 성공: " + wavPath);
                }
            } catch (Exception e) {
                System.out.println("[ffmpeg] 변환 예외: " + e.getMessage());
            }

            // openSMILE 음성 감정 분석(비언어적 신호) - 변환된 wav 파일 사용
            java.util.concurrent.Future<Map<String, String>> openSmileFuture = executor.submit(() -> openSmileService.analyzeEmotionWithOpenSmile(wavPath, openSmileConfigPath));

            // 감정분석, openSMILE, AI 응답을 동시에 시작
            java.util.concurrent.Future<EmotionAnalysisResult> analysisFuture = executor.submit(() -> openAIService.analyzeTranscriptEmotion(transcript, null));
            // openSmileResult를 LLM 프롬프트에 반영하기 위해 전달
            // openSmileResult(Map) → VoiceMetadata 변환 (try-catch 블록 이후, openSmileResult가 할당된 뒤)
            EmotionAnalysisResult analysis = null;
            Map<String, String> openSmileResult = null;
            StringBuilder errorBuilder = new StringBuilder();
            com.duckchat.api.dto.VoiceMetadata voiceMetadata = null;
            try {
                analysis = analysisFuture.get();
            } catch (Exception e) {
                errorBuilder.append("[감정분석 예외] ").append(e.getMessage()).append("; ");
                System.out.println("[AsyncProcessing] 감정분석 예외: " + e.getMessage());
            }
            try {
                openSmileResult = openSmileFuture.get();
            } catch (Exception e) {
                errorBuilder.append("[openSMILE 예외] ").append(e.getMessage()).append("; ");
                System.out.println("[AsyncProcessing] openSMILE 예외: " + e.getMessage());
            }
            if (openSmileResult != null && !openSmileResult.isEmpty()) {
                try {
                    Double pitch = openSmileResult.get("F0final_sma") != null ? Double.valueOf(openSmileResult.get("F0final_sma")) : null;
                    Double volume = openSmileResult.get("pcm_RMSenergy_sma") != null ? Double.valueOf(openSmileResult.get("pcm_RMSenergy_sma")) : null;
                    Double confidence = openSmileResult.get("voicingFinalUnclipped_sma") != null ? Double.valueOf(openSmileResult.get("voicingFinalUnclipped_sma")) : null;
                    voiceMetadata = new com.duckchat.api.dto.VoiceMetadata();
                    voiceMetadata.setPitch(pitch);
                    voiceMetadata.setVolume(volume);
                    voiceMetadata.setConfidence(confidence);
                    // 감정 분석 결과를 VoiceMetadata에 추가
                    if (analysis != null) {
                        voiceMetadata.setDetectedEmotions(analysis.getRawJson());
                    }
                } catch (Exception e) {
                    System.out.println("[openSMILE→VoiceMetadata 변환 오류] " + e.getMessage());
                }
            }

            // 감정 요약을 VoiceMetadata에 추가 (VoiceMetadata 초기화 후)
            if (emotionSummary.length() > 0) {
                if (voiceMetadata == null) {
                    voiceMetadata = new com.duckchat.api.dto.VoiceMetadata();
                }
                String currentEmotions = voiceMetadata.getDetectedEmotions();
                String emotionContext = "\"emotionSummary\":\"" + emotionSummary.toString() + "\"";
                if (currentEmotions != null && !currentEmotions.isEmpty()) {
                    // 기존 JSON에 emotionSummary 추가
                    if (currentEmotions.endsWith("}")) {
                        voiceMetadata.setDetectedEmotions(currentEmotions.substring(0, currentEmotions.length() - 1) + "," + emotionContext + "}");
                    } else {
                        voiceMetadata.setDetectedEmotions(currentEmotions + "," + emotionContext);
                    }
                } else {
                    voiceMetadata.setDetectedEmotions("{" + emotionContext + "}");
                }
            }
            final String transcriptFinal = transcript;
            System.out.println("[AsyncProcessing] 대화 히스토리 개수: " + messageHistory.size());
            final com.duckchat.api.dto.VoiceMetadata voiceMetadataFinal = voiceMetadata;
            final List<ChatCompletionRequest.Message> messageHistoryFinal = messageHistory;
            java.util.concurrent.Future<String> assistantFuture = executor.submit(() -> openAIService.generateResponseWithHistoryAndVoice(messageHistoryFinal, transcriptFinal, voiceMetadataFinal));
            if (analysis != null) {
                System.out.println("[AsyncProcessing] 감정 분석 완료: " + analysis.getRawJson());
                // openSMILE 결과를 analysisJson에 함께 저장(필요시 별도 필드 추가 가능)
                String combinedJson = analysis.getRawJson();
                if (openSmileResult != null && !openSmileResult.isEmpty()) {
                    combinedJson = combinedJson.replaceFirst("}$", ", \"openSmile\": " + new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(openSmileResult) + "}");
                }
                j.setAnalysisJson(combinedJson);
            } else {
                System.out.println("[AsyncProcessing] 감정 분석 결과 없음");
                errorBuilder.append("[감정분석 결과 없음]");
            }
            if (errorBuilder.length() > 0) {
                j.setErrorMessage(errorBuilder.toString());
            }

            String assistant = assistantFuture.get();
            System.out.println("[AsyncProcessing] AI 응답 완료: " + (assistant != null ? assistant.substring(0, Math.min(50, assistant.length())) + "..." : "null"));
            j.setAssistantResponse(assistant);

            // 사용자와 AI 메시지를 채팅 히스토리에 저장
            if (chatSessionId != null && j.getUserId() != null) {
                try {
                    User user = userRepository.findById(j.getUserId()).orElse(null);
                    if (user != null) {
                        // 사용자 메시지 저장
                        ChatMessageRequest userMessageRequest = ChatMessageRequest.builder()
                                .content(transcript)
                                .type(MessageType.USER)
                                .chatSessionId(chatSessionId)
                                .emotionType(analysis != null ? analysis.getPrimaryEmotion() : null)
                                .emotionScore(analysis != null && analysis.getConfidence() != null ? analysis.getConfidence() : 0.0)
                                .isVoiceInput(true)
                                .build();
                        chatService.saveMessage(user, userMessageRequest);

                        // AI 응답 저장
                        ChatMessageRequest aiMessageRequest = ChatMessageRequest.builder()
                                .content(assistant)
                                .type(MessageType.ASSISTANT)
                                .chatSessionId(chatSessionId)
                                .isVoiceInput(false)
                                .build();
                        chatService.saveMessage(user, aiMessageRequest);
                    }
                } catch (Exception e) {
                    System.out.println("[AsyncProcessing] 메시지 저장 실패: " + e.getMessage());
                }
            }

            j.setStatus("DONE");
            jobRepository.save(j);
            executor.shutdown();
            System.out.println("[AsyncProcessing] 작업 완료: " + jobId);

        } catch (Exception e) {
            System.out.println("[AsyncProcessing] 작업 실패 - jobId: " + jobId + ", 오류: " + e.getMessage());
            e.printStackTrace();
            j.setStatus("FAILED");
            j.setErrorMessage(e.getMessage());
            jobRepository.save(j);
        }
        return new AsyncResult<>(j);
    }
}
