package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.ChatMessageRequest;
import com.duckchat.api.dto.ChatSessionRequest;
import com.duckchat.api.entity.ChatMessage;
import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.UserRepository;
import com.duckchat.api.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessage>> saveMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatMessageRequest request) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ChatMessage savedMessage = chatService.saveMessage(user, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "메시지가 성공적으로 저장되었습니다.", savedMessage));
    }

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessage>>> getUserMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Page<ChatMessage> messages = chatService.getUserMessages(user, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "사용자 메시지를 성공적으로 가져왔습니다.", messages));
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<ChatSession>> createChatSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatSessionRequest request) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ChatSession session = chatService.createChatSession(user, request.getTitle());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "채팅 세션이 성공적으로 생성되었습니다.", session));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<ChatSession>>> getUserSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        Page<ChatSession> sessions = chatService.getUserSessions(user, pageable);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "사용자 채팅 세션을 성공적으로 가져왔습니다.", sessions));
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<ChatSession>> getChatSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long sessionId) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ChatSession session = chatService.getChatSession(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다."));
        
        return ResponseEntity.ok(new ApiResponse<>(true, "채팅 세션을 성공적으로 가져왔습니다.", session));
    }

    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSessionMessages(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long sessionId) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ChatSession session = chatService.getChatSession(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다."));
        
        List<ChatSessionMessage> sessionMessages = chatService.getSessionMessages(session);
        
        List<Map<String, Object>> messages = sessionMessages.stream().map(sm -> {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", sm.getMessage().getId());
            messageMap.put("content", sm.getMessage().getContent());
            messageMap.put("type", sm.getMessage().getType());
            messageMap.put("emotionType", sm.getMessage().getEmotionType());
            messageMap.put("emotionScore", sm.getMessage().getEmotionScore());
            messageMap.put("createdAt", sm.getMessage().getCreatedAt());
            messageMap.put("order", sm.getOrder());
            return messageMap;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "세션 메시지를 성공적으로 가져왔습니다.", messages));
    }

    @PutMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<ChatSession>> updateSessionTitle(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long sessionId,
            @Valid @RequestBody ChatSessionRequest request) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        ChatSession updatedSession = chatService.updateSessionTitle(sessionId, user, request.getTitle());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "채팅 세션 제목이 성공적으로 업데이트되었습니다.", updatedSession));
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long sessionId) {
        
        if (userDetails == null) {
            throw new IllegalArgumentException("인증이 필요합니다.");
        }
        
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        chatService.deleteSession(sessionId, user);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "채팅 세션이 성공적으로 삭제되었습니다.", null));
    }
}
