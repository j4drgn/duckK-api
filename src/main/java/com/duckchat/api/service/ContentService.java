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
    
    public List<Content> getContentsByType(String type) {
        return contentRepository.findByType(type);
    }
    
    public List<Content> getContentsByGenre(String genre) {
        return contentRepository.findByGenre(genre);
    }
    
    public List<Content> getContentsByCreator(String creator) {
        return contentRepository.findByCreator(creator);
    }
    
    public List<Content> searchContentsByTitle(String keyword) {
        return contentRepository.findByTitleContaining(keyword);
    }
    
    public List<Content> getContentsByEmotionTag(String emotionTag) {
        return contentRepository.findByEmotionTagsContaining(emotionTag);
    }
    
    public List<Content> getTopRatedContentsByType(String type) {
        return contentRepository.findTopRatedByType(type);
    }
    
    public List<Content> getRecommendationsByEmotionAndGenre(String emotion, String genre) {
        return contentRepository.findRecommendationsByEmotionAndGenre(emotion, genre);
    }
    
    public List<Content> getLatestContents() {
        return contentRepository.findLatestContents();
    }
    
    /**
     * 사용자의 감정 상태에 따라 콘텐츠를 추천합니다.
     * 
     * @param emotionType 감정 타입 (happy, sad, relaxed, excited 등)
     * @return 추천 콘텐츠 목록
     */
    public List<Content> getRecommendationsByEmotion(String emotionType) {
        // 감정에 따른 콘텐츠 추천 로직
        switch (emotionType.toLowerCase()) {
            case "happy":
                // 행복한 감정일 때는 코미디, 로맨스 등 밝은 콘텐츠 추천
                return contentRepository.findByEmotionTagsContaining("happy");
            case "sad":
                // 슬픈 감정일 때는 위로가 되는 따뜻한 콘텐츠나 힐링 콘텐츠 추천
                return contentRepository.findByEmotionTagsContaining("healing");
            case "relaxed":
                // 편안한 감정일 때는 가벼운 예능이나 다큐멘터리 추천
                return contentRepository.findByEmotionTagsContaining("relaxing");
            case "excited":
                // 흥분된 감정일 때는 액션, 스릴러 등 자극적인 콘텐츠 추천
                return contentRepository.findByEmotionTagsContaining("exciting");
            default:
                // 기본적으로는 평점이 높은 콘텐츠 추천
                return contentRepository.findLatestContents();
        }
    }
}
