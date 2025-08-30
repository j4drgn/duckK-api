package com.duckchat.api.repository;

import com.duckchat.api.entity.ChatMessage;
import com.duckchat.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<ChatMessage> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(User user, LocalDateTime start, LocalDateTime end);
    void deleteByUser(User user);
}
