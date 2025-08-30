package com.duckchat.api.service;

import com.duckchat.api.entity.User;
import com.duckchat.api.entity.UserPreference;
import com.duckchat.api.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final OpenSmileService openSmileService;
    private final UserPreferenceRepository userPreferenceRepository;

    // 감정 분석 및 추천
    public String analyzeEmotionAndRecommend(String wavFilePath, User user) {
        // openSMILE config 경로 (emobase.conf 사용)
        String configPath = "/Users/ryugi62/Desktop/해커톤/opensmile/config/emobase/emobase.conf";

        Map<String, String> features = openSmileService.analyzeEmotionWithOpenSmile(wavFilePath, configPath);

        if (features.containsKey("error")) {
            return "감정 분석 실패: " + features.get("error");
        }

        // 간단한 감정 판단 (예시)
        double energy = Double.parseDouble(features.getOrDefault("pcm_RMSenergy_sma", "0"));
        double pitch = Double.parseDouble(features.getOrDefault("F0final_sma", "0"));
        double voiceProb = Double.parseDouble(features.getOrDefault("voicingFinalUnclipped_sma", "0"));

        String emotion;
        if (energy < 0.1 && pitch < 100) {
            emotion = "sad";
        } else if (energy > 0.5 && pitch > 150) {
            emotion = "happy";
        } else {
            emotion = "neutral";
        }

        // 사용자 선호도 기반 추천
        List<UserPreference> preferences = userPreferenceRepository.findByUserAndPreferenceType(user, "animal");
        String recommendation = "기본 추천: 긍정적인 콘텐츠";

        if (emotion.equals("sad") && !preferences.isEmpty()) {
            for (UserPreference pref : preferences) {
                if (pref.getPreferenceValue().equals("cat")) {
                    recommendation = "고양이 쇼츠 추천";
                    break;
                }
            }
        }

        return "감정: " + emotion + ", 추천: " + recommendation;
    }
}
