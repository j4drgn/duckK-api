package com.duckchat.api.dto.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionRequest {
    private String model;
    private List<Message> messages;
    private Double temperature;
    private Integer max_tokens;
    private Double top_p;
    private Double frequency_penalty;
    private Double presence_penalty;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
