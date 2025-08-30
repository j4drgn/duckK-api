package com.duckchat.api.repository;

import com.duckchat.api.entity.ChatSession;
import com.duckchat.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);
    Page<ChatSession> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);
    List<ChatSession> findByUserAndIsActiveTrue(User user);
    Optional<ChatSession> findByIdAndUser(Long id, User user);
    void deleteByUser(User user);
}
