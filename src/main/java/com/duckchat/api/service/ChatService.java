package com.duckchat.api.service;

import com.duckchat.api.dto.ChatMessageRequest;
import com.duckchat.api.entity.ChatMessage;
import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import com.duckchat.api.entity.User;
import com.duckchat.api.repository.ChatMessageRepository;
import com.duckchat.api.repository.ChatSessionMessageRepository;
import com.duckchat.api.repository.ChatSessionRepository;
import com.duckchat.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatSessionMessageRepository chatSessionMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessage saveMessage(User user, ChatMessageRequest request) {
        ChatMessage message = ChatMessage.builder()
                .user(user)
                .content(request.getContent())
                .type(request.getType())
                .emotionType(request.getEmotionType())
                .emotionScore(request.getEmotionScore())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 채팅 세션이 지정된 경우 세션에 메시지 추가
        if (request.getChatSessionId() != null) {
            ChatSession session = chatSessionRepository.findByIdAndUser(request.getChatSessionId(), user)
                    .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다."));

            // 세션의 마지막 메시지 순서 조회
            int lastOrder = chatSessionMessageRepository.findByChatSessionOrderByOrderAsc(session).stream()
                    .mapToInt(ChatSessionMessage::getOrder)
                    .max()
                    .orElse(0);

            ChatSessionMessage sessionMessage = ChatSessionMessage.builder()
                    .chatSession(session)
                    .message(savedMessage)
                    .order(lastOrder + 1)
                    .build();

            chatSessionMessageRepository.save(sessionMessage);
        }

        return savedMessage;
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getUserMessages(User user) {
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Page<ChatMessage> getUserMessages(User user, Pageable pageable) {
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Transactional
    public ChatSession createChatSession(User user, String title) {
        ChatSession session = ChatSession.builder()
                .user(user)
                .title(title)
                .isActive(true)
                .build();

        return chatSessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSession> getUserSessions(User user) {
        return chatSessionRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Page<ChatSession> getUserSessions(User user, Pageable pageable) {
        return chatSessionRepository.findByUserOrderByUpdatedAtDesc(user, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ChatSession> getChatSession(Long sessionId, User user) {
        return chatSessionRepository.findByIdAndUser(sessionId, user);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionMessage> getSessionMessages(ChatSession session) {
        return chatSessionMessageRepository.findByChatSessionOrderByOrderAsc(session);
    }

    @Transactional
    public ChatSession updateSessionTitle(Long sessionId, User user, String title) {
        ChatSession session = chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다."));

        session.updateTitle(title);
        return chatSessionRepository.save(session);
    }

    @Transactional
    public void deleteSession(Long sessionId, User user) {
        ChatSession session = chatSessionRepository.findByIdAndUser(sessionId, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다."));

        chatSessionMessageRepository.deleteByChatSession(session);
        chatSessionRepository.delete(session);
    }
}
