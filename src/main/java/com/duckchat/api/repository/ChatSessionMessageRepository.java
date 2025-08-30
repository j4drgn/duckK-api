package com.duckchat.api.repository;

import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.ChatSessionMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatSessionMessageRepository extends JpaRepository<ChatSessionMessage, Long> {
    @Query("SELECT csm FROM ChatSessionMessage csm JOIN FETCH csm.message WHERE csm.chatSession = :chatSession ORDER BY csm.order ASC")
    List<ChatSessionMessage> findByChatSessionOrderByOrderAsc(@Param("chatSession") ChatSession chatSession);
    void deleteByChatSession(ChatSession chatSession);
}
