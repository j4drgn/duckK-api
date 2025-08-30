package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.ChatRequest;
import com.duckchat.api.dto.ChatResponse;
import com.duckchat.api.dto.DuckyChatRequest;
import com.duckchat.api.dto.openai.ChatCompletionRequest;
import com.duckchat.api.entity.ChatMessage;
import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.UserRepository;
import com.duckchat.api.service.ChatService;
import com.duckchat.api.service.OpenAIService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
@Slf4j
public class ChatGPTController {

    private final OpenAIService openAIService;
    private final ChatService chatService;
    private final UserRepository userRepository;
        private final com.duckchat.api.service.AsyncProcessingService asyncProcessingService;
        private final com.duckchat.api.config.OpenAIConfig openAIConfig;

    // 텍스트 채팅은 음성 채팅만 지원하도록 비활성화
    /*
    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatRequest request) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 사용자 메시지 저장
        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));
        
        // ChatGPT API 호출
        String assistantResponse = openAIService.generateResponse(request.getMessage());
        
        // ChatGPT 응답 저장
        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                assistantResponse, ChatMessage.MessageType.ASSISTANT, request.getChatSessionId()));
        
        // 응답 생성
        ChatResponse response = ChatResponse.builder()
                .id(assistantMessage.getId())
                .content(assistantMessage.getContent())
                .type(assistantMessage.getType())
                .timestamp(assistantMessage.getCreatedAt())
                .chatSessionId(request.getChatSessionId())
                .build();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "메시지가 성공적으로 처리되었습니다.", response));
    }
    */

    // 텍스트 채팅은 음성 채팅만 지원하도록 비활성화
    /*
    @PostMapping("/chat/session/{sessionId}")
    public ResponseEntity<ApiResponse<ChatResponse>> chatWithSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody ChatRequest request) {
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 세션 조회
        Optional<ChatSession> sessionOpt = chatService.getChatSession(sessionId, user);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "채팅 세션을 찾을 수 없습니다.", null));
        }
        
        ChatSession session = sessionOpt.get();
        request.setChatSessionId(sessionId);
        
        // 세션의 이전 메시지 히스토리 조회
        List<ChatSessionMessage> sessionMessages = chatService.getSessionMessages(session);
        List<ChatCompletionRequest.Message> messageHistory = new ArrayList<>();
        
        // 시스템 메시지 추가
        messageHistory.add(ChatCompletionRequest.Message.builder()
                .role("system")
                .content("너는 덕키야! 귀여운 오리 같은 친구 같은 AI야. 사용자의 감정을 잘 이해하고 공감해줘. 이전 대화도 기억하면서 재미있고 귀엽게 응답해줘. 유튜브 쇼츠 추천도 해줄게~")
                .build());
        
        // 이전 메시지 히스토리 추가
        for (ChatSessionMessage sessionMessage : sessionMessages) {
            ChatMessage message = sessionMessage.getMessage();
            String role = message.getType() == ChatMessage.MessageType.USER ? "user" : "assistant";
            
            messageHistory.add(ChatCompletionRequest.Message.builder()
                    .role(role)
                    .content(message.getContent())
                    .build());
        }
        
        // 사용자 메시지 저장
        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));
        
        // ChatGPT API 호출 (대화 히스토리 포함)
        String assistantResponse = openAIService.generateResponseWithHistory(messageHistory, request.getMessage());
        
        // ChatGPT 응답 저장
        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                assistantResponse, ChatMessage.MessageType.ASSISTANT, sessionId));
        
        // 응답 생성
        ChatResponse response = ChatResponse.builder()
                .id(assistantMessage.getId())
                .content(assistantMessage.getContent())
                .type(assistantMessage.getType())
                .timestamp(assistantMessage.getCreatedAt())
                .chatSessionId(sessionId)
                .build();
        
        return ResponseEntity.ok(new ApiResponse<>(true, "메시지가 성공적으로 처리되었습니다.", response));
    }
    */
    
    private com.duckchat.api.dto.ChatMessageRequest buildChatMessageRequest(
            ChatRequest request, ChatMessage.MessageType type) {
        return com.duckchat.api.dto.ChatMessageRequest.builder()
                .content(request.getMessage())
                .type(type)
                .emotionType(request.getEmotionType())
                .emotionScore(request.getEmotionScore())
                .chatSessionId(request.getChatSessionId())
                .voiceMetadata(request.getVoiceMetadata())
                .isVoiceInput(request.getIsVoiceInput())
                .build();
    }
    
    private com.duckchat.api.dto.ChatMessageRequest buildChatMessageRequest(
            String content, ChatMessage.MessageType type, Long chatSessionId) {
        return com.duckchat.api.dto.ChatMessageRequest.builder()
                .content(content)
                .type(type)
                .chatSessionId(chatSessionId)
                .build();
    }
    
    @PostMapping("/chat/voice")
    public ResponseEntity<ApiResponse<ChatResponse>> chatWithVoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatRequest request) {

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 사용자 메시지 저장 (음성 메타데이터 포함)
        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));

        // 음성 메타데이터를 활용한 ChatGPT API 호출
        String assistantResponse;
        try {
            if (request.getVoiceMetadata() != null) {
                assistantResponse = openAIService.generateResponseWithVoice(request.getMessage(), request.getVoiceMetadata());
            } else {
                assistantResponse = openAIService.generateResponse(request.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "ChatGPT API 호출 중 오류가 발생했습니다.", null));
        }

        // ChatGPT 응답 저장
        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                assistantResponse, ChatMessage.MessageType.ASSISTANT, request.getChatSessionId()));

        // 응답 생성
        ChatResponse response = ChatResponse.builder()
                .id(assistantMessage.getId())
                .content(assistantMessage.getContent())
                .type(assistantMessage.getType())
                .timestamp(assistantMessage.getCreatedAt())
                .chatSessionId(request.getChatSessionId())
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "음성 메시지가 성공적으로 처리되었습니다.", response));
    }

                @PostMapping("/chat/voice/file")
                public ResponseEntity<ApiResponse<Object>> chatWithVoiceFile(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestPart("audio") MultipartFile audio,
                        @RequestPart(value = "meta", required = false) String metaJson,
                    @RequestParam(value = "chatSessionId", required = false) Long chatSessionId,
                    @RequestParam(value = "async", required = false, defaultValue = "false") boolean async
        ) {
                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                if (audio == null || audio.isEmpty()) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "오디오 파일이 필요합니다.", null));
                }

                try {
                                        // 업로드 디렉토리에 저장
                                        File uploadDir = new File(openAIConfig.getUploadDir());
                                        if (!uploadDir.exists()) uploadDir.mkdirs();
                                        File tmp = new File(uploadDir, "upload-" + System.currentTimeMillis() + "-" + audio.getOriginalFilename());
                                        try (FileOutputStream fos = new FileOutputStream(tmp)) {
                                                fos.write(audio.getBytes());
                                        }

                                        if (async) {
                                                var job = asyncProcessingService.createJob(user.getId());
                                                asyncProcessingService.runTranscriptionAndAnalysis(job.getId(), tmp.getAbsolutePath(), "ko", chatSessionId, openAIService);
                                                // 클라이언트가 폴링해서 확인하도록 jobId 반환
                                                return ResponseEntity.ok(new ApiResponse<>(true, "작업이 시작되었습니다.", job.getId()));
                                        }

                                        // 동기 처리에서 7초 이상 걸리면 자동으로 비동기 전환
                                        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                                        java.util.concurrent.Future<String> future = executor.submit(() -> openAIService.transcribeAudioFile(tmp.getAbsolutePath(), "ko"));
                                        String transcriptionRaw;
                                        try {
                                                transcriptionRaw = future.get(7, java.util.concurrent.TimeUnit.SECONDS); // 7초 제한
                                        } catch (java.util.concurrent.TimeoutException e) {
                                                // 비동기로 전환
                                                var job = asyncProcessingService.createJob(user.getId());
                                                asyncProcessingService.runTranscriptionAndAnalysis(job.getId(), tmp.getAbsolutePath(), "ko", chatSessionId, openAIService);
                                                return ResponseEntity.ok(new ApiResponse<>(true, "오디오 처리에 시간이 소요되어 비동기 처리로 전환되었습니다. 잠시 후 결과를 확인해주세요.", job.getId()));
                                        } finally {
                                                executor.shutdown();
                                        }

                                        // API 키가 더미일 경우 기본 텍스트 사용
                                        String transcriptText;
                                        if (transcriptionRaw == null || transcriptionRaw.trim().isEmpty()) {
                                                transcriptText = "안녕하세요! 음성 메시지를 받았어요. 어떻게 도와드릴까요?"; // 기본 텍스트
                                        } else {
                                                transcriptText = transcriptionRaw;
                                        }

                                        // 감정/상황 분석
                                        // VoiceMetadata 파싱은 생략(클라이언트 metaJson 사용 가능)
                                        var analysis = openAIService.analyzeTranscriptEmotion(transcriptText, null);

                                        // 기존 저장/대화 흐름 사용: ChatMessage로 사용자 메시지 저장 (전사 텍스트 포함)
                                        ChatRequest request = new ChatRequest();
                                        request.setMessage(transcriptText);
                                        request.setChatSessionId(chatSessionId);

                                        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));

                                        // 챗봇 응답 생성 - 분석 결과 반영 가능
                                        String assistantResponse = openAIService.generateResponseWithVoice(transcriptText, null);

                                        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                                                assistantResponse, ChatMessage.MessageType.ASSISTANT, chatSessionId));

                                        ChatResponse response = ChatResponse.builder()
                                                .id(assistantMessage.getId())
                                                .content(assistantMessage.getContent())
                                                .type(assistantMessage.getType())
                                                .timestamp(assistantMessage.getCreatedAt())
                                                .chatSessionId(chatSessionId)
                                                .build();

                                        // 임시 파일 삭제
                                        tmp.delete();

                                        return ResponseEntity.ok(new ApiResponse<>(true, "오디오 처리 및 응답 생성 완료", response));
                } catch (Exception e) {
                        log.error("오디오 처리 중 오류 발생: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "오디오 처리 중 오류가 발생했습니다: " + e.getMessage(), null));
                }
        }

                @PostMapping("/chat/session/{sessionId}/voice/file")
                public ResponseEntity<ApiResponse<Object>> chatWithSessionAndVoiceFile(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @PathVariable("sessionId") Long sessionId,
                        @RequestPart("audio") MultipartFile audio,
                            @RequestPart(value = "meta", required = false) String metaJson,
                            @RequestParam(value = "async", required = false, defaultValue = "false") boolean async
        ) {
                // 세션 유효성 검사
                if (sessionId == null || sessionId <= 0) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "유효하지 않은 세션 ID입니다.", null));
                }

                User user = userRepository.findByEmail(userDetails.getUsername())
                                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

                Optional<ChatSession> sessionOpt = chatService.getChatSession(sessionId, user);
                if (sessionOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "채팅 세션을 찾을 수 없습니다. 세션 ID: " + sessionId, null));
                }

                if (audio == null || audio.isEmpty()) {
                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "오디오 파일이 필요합니다.", null));
                }

                try {
                            File uploadDir = new File(openAIConfig.getUploadDir());
                            if (!uploadDir.exists()) uploadDir.mkdirs();
                            File tmp = new File(uploadDir, "upload-" + System.currentTimeMillis() + "-" + audio.getOriginalFilename());
                        try (FileOutputStream fos = new FileOutputStream(tmp)) {
                                fos.write(audio.getBytes());
                        }

                                        if (async) {
                                                var job = asyncProcessingService.createJob(user.getId());
                                                asyncProcessingService.runTranscriptionAndAnalysis(job.getId(), tmp.getAbsolutePath(), "ko", sessionId, openAIService);
                                                return ResponseEntity.ok(new ApiResponse<>(true, "작업이 시작되었습니다.", job.getId()));
                                        }

                                        String transcriptionRaw = openAIService.transcribeAudioFile(tmp.getAbsolutePath(), "ko");
                                        String transcriptText;
                                        if (transcriptionRaw == null || transcriptionRaw.trim().isEmpty()) {
                                                transcriptText = "안녕하세요! 음성 메시지를 받았어요. 어떻게 도와드릴까요?"; // 기본 텍스트
                                        } else {
                                                transcriptText = transcriptionRaw;
                                        }

                        var analysis = openAIService.analyzeTranscriptEmotion(transcriptText, null);

                        ChatRequest request = new ChatRequest();
                        request.setMessage(transcriptText);
                        request.setChatSessionId(sessionId);

                        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));

                        String assistantResponse = openAIService.generateResponseWithHistoryAndVoice(new ArrayList<>(), transcriptText, null);

                        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                                        assistantResponse, ChatMessage.MessageType.ASSISTANT, sessionId));

                        ChatResponse response = ChatResponse.builder()
                                        .id(assistantMessage.getId())
                                        .content(assistantMessage.getContent())
                                        .type(assistantMessage.getType())
                                        .timestamp(assistantMessage.getCreatedAt())
                                        .chatSessionId(sessionId)
                                        .build();

                        tmp.delete();
                        return ResponseEntity.ok(new ApiResponse<>(true, "오디오 처리 및 응답 생성 완료", response));
                } catch (Exception e) {
                        log.error("오디오 처리 중 오류 발생: {}", e.getMessage(), e);
                        return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "오디오 처리 중 오류가 발생했습니다: " + e.getMessage(), null));
                }
        }

                        @GetMapping("/chat/voice/task/{jobId}")
                        public ResponseEntity<ApiResponse<Object>> getJobStatus(@PathVariable("jobId") String jobId) {
                                var job = asyncProcessingService.getJob(jobId);
                                if (job == null) {
                                        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "존재하지 않는 작업 ID입니다.", null));
                                }
                                return ResponseEntity.ok(new ApiResponse<>(true, "작업 상태 조회", job));
                        }

        @PostMapping("/chat/session/{sessionId}/voice")
    public ResponseEntity<ApiResponse<ChatResponse>> chatWithSessionAndVoice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("sessionId") Long sessionId,
            @Valid @RequestBody ChatRequest request) {

        // sessionId 유효성 검증
        if (sessionId == null || sessionId <= 0) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "유효하지 않은 세션 ID입니다.", null));
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 세션 조회
        Optional<ChatSession> sessionOpt = chatService.getChatSession(sessionId, user);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(false, "채팅 세션을 찾을 수 없습니다. 세션 ID: " + sessionId, null));
        }

        ChatSession session = sessionOpt.get();
        request.setChatSessionId(sessionId);

        // 세션의 이전 메시지 히스토리 조회
        List<ChatSessionMessage> sessionMessages = chatService.getSessionMessages(session);
        List<ChatCompletionRequest.Message> messageHistory = new ArrayList<>();

        // 시스템 메시지 추가 (기본 시스템 메시지)
        messageHistory.add(ChatCompletionRequest.Message.builder()
                .role("system")
                .content("너는 덕키야! 귀여운 오리 같은 친구 같은 AI야. 사용자의 감정을 잘 이해하고 공감해줘. 이전 대화도 기억하면서 재미있고 귀엽게 응답해줘. 유튜브 쇼츠 추천도 해줄게~")
                .build());

        // 이전 메시지 히스토리 추가
        for (ChatSessionMessage sessionMessage : sessionMessages) {
            ChatMessage message = sessionMessage.getMessage();
            String role = message.getType() == ChatMessage.MessageType.USER ? "user" : "assistant";

            messageHistory.add(ChatCompletionRequest.Message.builder()
                    .role(role)
                    .content(message.getContent())
                    .build());
        }

        // 사용자 메시지 저장
        ChatMessage userMessage = chatService.saveMessage(user, buildChatMessageRequest(request, ChatMessage.MessageType.USER));

        // 음성 메타데이터를 활용한 ChatGPT API 호출
        String assistantResponse;
        try {
            if (request.getVoiceMetadata() != null) {
                assistantResponse = openAIService.generateResponseWithHistoryAndVoice(messageHistory, request.getMessage(), request.getVoiceMetadata());
            } else {
                assistantResponse = openAIService.generateResponseWithHistory(messageHistory, request.getMessage());
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "ChatGPT API 호출 중 오류가 발생했습니다.", null));
        }

        // ChatGPT 응답 저장
        ChatMessage assistantMessage = chatService.saveMessage(user, buildChatMessageRequest(
                assistantResponse, ChatMessage.MessageType.ASSISTANT, sessionId));

        // 응답 생성
        ChatResponse response = ChatResponse.builder()
                .id(assistantMessage.getId())
                .content(assistantMessage.getContent())
                .type(assistantMessage.getType())
                .timestamp(assistantMessage.getCreatedAt())
                .chatSessionId(sessionId)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "음성 메시지가 성공적으로 처리되었습니다.", response));
    }

    @PostMapping("/ducky-chat")
    public ResponseEntity<ApiResponse<String>> duckyChat(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DuckyChatRequest request) {

        // User validation
        userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        try {
            String duckyResponseJson = openAIService.generateDuckyResponse(
                    request.getMessage(),
                    request.getCharacterProfile(),
                    request.getExtractedLabelsJson()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Ducky response generated successfully.", duckyResponseJson));

        } catch (Exception e) {
            log.error("Error during Ducky chat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ApiResponse<>(false, "Error generating Ducky response.", null));
        }
    }
}}
