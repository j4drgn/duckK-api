package com.duckchat.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    // 콘텐츠 유형: BOOK, MOVIE, MUSIC, PODCAST 등
    @Column(nullable = false)
    private String type;
    
    // 장르: 로맨스, SF, 액션, 클래식, 힙합 등
    @Column
    private String genre;
    
    // 콘텐츠 제작자: 저자, 감독, 아티스트 등
    @Column(name = "creator")
    private String creator;
    
    // 발매/출시 연도
    @Column(name = "release_year")
    private Integer releaseYear;
    
    // 콘텐츠 이미지 URL
    @Column(name = "image_url")
    private String imageUrl;
    
    // 콘텐츠 외부 링크 (스트리밍 서비스, 구매 링크 등)
    @Column(name = "external_link")
    private String externalLink;
    
    // 감정 태그: HAPPY, SAD, RELAXED, EXCITED 등
    @Column(name = "emotion_tags")
    private String emotionTags;
    
    // 평점 (5점 만점)
    @Column
    private Double rating;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
