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
                .content("너는 덕키야! 귀여운 오리 같은 친구 같은 AI야. 사용자의 감정을 잘 이해하고 공감해줘. 이전 대화도 기억하면서 재미있고 귀엽게 응답해줘. 상품 추천도 해줄게~")
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