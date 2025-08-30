package com.duckchat.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

@Data
public class DuckyChatRequest {
    @NotEmpty
    private String message;

    @NotEmpty
    private String characterProfile; // "F형" or "T형"

    private String extractedLabelsJson; // Can be null or a JSON string
    
    private List<Map<String, Object>> conversationHistory; // 최근 대화 히스토리
}
