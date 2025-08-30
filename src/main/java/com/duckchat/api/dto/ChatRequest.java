package com.duckchat.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {
    
    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;
    
    private String emotionType;
    
    private Double emotionScore;
    
    private Long chatSessionId;
    
    // 음성 메타데이터 (음성 입력 시 사용)
    private VoiceMetadata voiceMetadata;
    
    // 음성 입력 여부
    private Boolean isVoiceInput = false;
}
