package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.ChatRequest;
import com.duckchat.api.dto.ChatResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chatgpt")
@RequiredArgsConstructor
public class ChatGPTController {

    private final OpenAIService openAIService;
    private final ChatService chatService;
    private final UserRepository userRepository;

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
                .content("당신은 덕키(Ducky)라는 친근한 AI 어시스턴트입니다. 사용자의 감정을 이해하고 공감하며, " +
                        "이전 대화 기록을 항상 고려하여 일관된 답변을 제공하세요. 사용자의 과거 발언을 기억하고, " +
                        "맥락에 맞게 응답하세요. 필요에 따라 적절한 상품을 추천해 주세요. 답변은 친근하고 간결하게 해주세요.")
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
    
    private com.duckchat.api.dto.ChatMessageRequest buildChatMessageRequest(
            ChatRequest request, ChatMessage.MessageType type) {
        return com.duckchat.api.dto.ChatMessageRequest.builder()
                .content(request.getMessage())
                .type(type)
                .emotionType(request.getEmotionType())
                .emotionScore(request.getEmotionScore())
                .chatSessionId(request.getChatSessionId())
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
}