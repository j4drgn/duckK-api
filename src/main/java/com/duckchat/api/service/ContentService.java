package com.duckchat.api.service;

import com.duckchat.api.entity.Content;
import com.duckchat.api.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentRepository contentRepository;

    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    public Optional<Content> getContentById(Long id) {
        return contentRepository.findById(id);
    }

    public List<Content> searchContentsByTitle(String keyword) {
        return contentRepository.findByTitleContaining(keyword);
    }

    public List<Content> getContentsByEmotionTag(String emotionTag) {
        return contentRepository.findByEmotionTagsContaining(emotionTag);
    }

    public List<Content> getLatestContents() {
        return contentRepository.findLatestContents();
    }

    /**
     * 사용자의 감정 상태에 따라 유튜브 쇼츠를 추천합니다.
     *
     * @param emotionType 감정 타입 (happy, sad, relaxed, excited 등)
     * @return 추천 유튜브 쇼츠 목록
     */
    public List<Content> getRecommendationsByEmotion(String emotionType) {
        // 감정에 따른 유튜브 쇼츠 추천 로직
        switch (emotionType.toLowerCase()) {
            case "happy":
                // 행복한 감정일 때는 재미있는 쇼츠 추천
                return contentRepository.findByEmotionTagsContaining("happy");
            case "sad":
                // 슬픈 감정일 때는 위로가 되는 따뜻한 쇼츠 추천
                return contentRepository.findByEmotionTagsContaining("healing");
            case "relaxed":
                // 편안한 감정일 때는 가벼운 힐링 쇼츠 추천
                return contentRepository.findByEmotionTagsContaining("relaxing");
            case "excited":
                // 흥분된 감정일 때는 에너지 넘치는 쇼츠 추천
                return contentRepository.findByEmotionTagsContaining("exciting");
            default:
                // 기본적으로는 최신 쇼츠 추천
                return contentRepository.findLatestContents();
        }
    }
}
