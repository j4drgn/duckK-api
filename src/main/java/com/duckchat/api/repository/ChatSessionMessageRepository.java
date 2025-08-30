package com.duckchat.api.repository;

import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionMessageRepository extends JpaRepository<ChatSessionMessage, Long> {
    List<ChatSessionMessage> findByChatSessionOrderByOrderAsc(ChatSession chatSession);
    void deleteByChatSession(ChatSession chatSession);
}
