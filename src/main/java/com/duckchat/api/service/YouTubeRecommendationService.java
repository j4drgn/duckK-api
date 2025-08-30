package com.duckchat.api.service;

import com.duckchat.api.dto.EmotionAnalysisResult;
import com.duckchat.api.dto.YouTubeRecommendation;
import com.duckchat.api.dto.openai.ChatCompletionRequest;
import com.duckchat.api.dto.openai.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YouTubeRecommendationService {

    private final OpenAIService openAIService;

    // GPT에게 추천 3개를 요청하고 간단 DTO로 변환
    public List<YouTubeRecommendation> recommend(EmotionAnalysisResult analysis) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();
        messages.add(ChatCompletionRequest.Message.builder()
                .role("system")
                .content("당신은 유튜브 쇼츠 추천 전문가입니다. 주어진 감정 및 상황 기반으로 3개의 쇼츠 아이디어를 JSON 배열로 반환하세요. 각 아이템은 {title, description, query} 형태입니다. 다른 텍스트는 출력하지 마세요.")
                .build());

        String userContent = "Emotion: " + analysis.getPrimaryEmotion() + "\nSituation: " + analysis.getSituationLabel();
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userContent)
                .build());

        ChatCompletionResponse resp = openAIService.createChatCompletion(messages);
        List<YouTubeRecommendation> out = new ArrayList<>();
        if (resp != null && resp.getChoices() != null && !resp.getChoices().isEmpty()) {
            String content = resp.getChoices().get(0).getMessage().getContent();
            // 간단 파싱: content에서 JSON 객체를 찾아 Array로 변환
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                var root = om.readTree(content);
                if (root.isArray()) {
                    for (var item : root) {
                        YouTubeRecommendation rec = new YouTubeRecommendation();
                        rec.setTitle(item.has("title") ? item.get("title").asText() : null);
                        rec.setDescription(item.has("description") ? item.get("description").asText() : null);
                        rec.setQuery(item.has("query") ? item.get("query").asText() : (rec.getTitle() != null ? rec.getTitle() : null));
                        out.add(rec);
                    }
                }
            } catch (Exception ex) {
                // fallback: treat content lines as queries
                String[] lines = content.split("\\n");
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String q = line.trim();
                    YouTubeRecommendation rec = new YouTubeRecommendation();
                    rec.setTitle(q);
                    rec.setQuery(q);
                    out.add(rec);
                }
            }
        }
        return out;
    }
}
