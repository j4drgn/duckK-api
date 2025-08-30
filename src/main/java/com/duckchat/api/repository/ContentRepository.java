package com.duckchat.api.repository;

import com.duckchat.api.entity.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // 제목 키워드 검색
    List<Content> findByTitleContaining(String keyword);

    // 감정 태그로 유튜브 쇼츠 검색
    List<Content> findByEmotionTagsContaining(String emotionTag);

    // 최신 유튜브 쇼츠 조회
    @Query("SELECT c FROM Content c ORDER BY c.createdAt DESC")
    List<Content> findLatestContents();
}
