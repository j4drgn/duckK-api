package com.duckchat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoiceMetadata {
    private Double pitch;        // 음성 피치 (높낮이)
    private Double volume;       // 볼륨
    private Double speed;        // 말하는 속도
    private Double confidence;   // 음성 인식 신뢰도
    private Boolean isQuestion;  // 의문문 여부
    private String audioUrl;     // 녹음된 오디오 파일 URL (선택적)
    private String detectedEmotions; // 감지된 감정들 (JSON 문자열)
}
