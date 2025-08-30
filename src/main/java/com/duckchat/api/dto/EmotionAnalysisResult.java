package com.duckchat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmotionAnalysisResult {
    private String primaryEmotion;
    private Map<String, Double> emotionScores;
    private String situationLabel;
    private Double confidence;
    private List<String> recommendationKeywords;
    private String rawJson; // 원시 GPT 응답(파싱 실패 시 보관)
}
