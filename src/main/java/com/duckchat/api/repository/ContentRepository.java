package com.duckchat.api.repository;

import com.duckchat.api.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {
    
    // 콘텐츠 유형별 조회
    List<Content> findByType(String type);
    
    // 장르별 콘텐츠 조회
    List<Content> findByGenre(String genre);
    
    // 제작자별 콘텐츠 조회
    List<Content> findByCreator(String creator);
    
    // 제목 키워드 검색
    List<Content> findByTitleContaining(String keyword);
    
    // 감정 태그로 콘텐츠 검색
    List<Content> findByEmotionTagsContaining(String emotionTag);
    
    // 평점 기준 상위 콘텐츠 조회
    @Query("SELECT c FROM Content c WHERE c.type = :type ORDER BY c.rating DESC")
    List<Content> findTopRatedByType(@Param("type") String type);
    
    // 특정 감정과 장르에 맞는 콘텐츠 추천
    @Query("SELECT c FROM Content c WHERE c.emotionTags LIKE %:emotion% AND c.genre = :genre ORDER BY c.rating DESC")
    List<Content> findRecommendationsByEmotionAndGenre(
            @Param("emotion") String emotion, 
            @Param("genre") String genre);
    
    // 최신 콘텐츠 조회
    @Query("SELECT c FROM Content c ORDER BY c.releaseYear DESC")
    List<Content> findLatestContents();
}
