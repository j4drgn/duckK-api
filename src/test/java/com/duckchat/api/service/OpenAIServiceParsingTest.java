package com.duckchat.api.service;

import com.duckchat.api.dto.EmotionAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class OpenAIServiceParsingTest {

    @Test
    public void parseEmotionAnalysisJson() throws Exception {
        String json = "{\"primaryEmotion\":\"sad\",\"confidence\":0.85,\"situationLabel\":\"alone at home\",\"recommendationKeywords\":[\"comfort\",\"music\"],\"emotionScores\":{\"sad\":0.85,\"neutral\":0.15}}";
        ObjectMapper om = new ObjectMapper();
        var node = om.readTree(json);
        EmotionAnalysisResult r = new EmotionAnalysisResult();
        r.setRawJson(json);
        r.setPrimaryEmotion(node.get("primaryEmotion").asText());
        r.setConfidence(node.get("confidence").asDouble());
        r.setSituationLabel(node.get("situationLabel").asText());
        r.setRecommendationKeywords(om.convertValue(node.get("recommendationKeywords"), java.util.List.class));
        Map<String, Double> map = om.convertValue(node.get("emotionScores"), java.util.Map.class);
        r.setEmotionScores(map);

        Assertions.assertEquals("sad", r.getPrimaryEmotion());
        Assertions.assertEquals(0.85, r.getConfidence());
        Assertions.assertTrue(r.getEmotionScores().containsKey("sad"));
    }
}
