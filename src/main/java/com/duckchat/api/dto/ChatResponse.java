package com.duckchat.api.dto;

import com.duckchat.api.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private Long id;
    private String content;
    private ChatMessage.MessageType type;
    private LocalDateTime timestamp;
    private Long chatSessionId;
}
