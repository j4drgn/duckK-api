package com.duckchat.api.dto;

import com.duckchat.api.entity.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private String genre;
    private String creator;
    private Integer releaseYear;
    private String imageUrl;
    private String externalLink;
    private String emotionTags;
    private Double rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ContentResponse fromEntity(Content content) {
        return ContentResponse.builder()
                .id(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .type(content.getType())
                .genre(content.getGenre())
                .creator(content.getCreator())
                .releaseYear(content.getReleaseYear())
                .imageUrl(content.getImageUrl())
                .externalLink(content.getExternalLink())
                .emotionTags(content.getEmotionTags())
                .rating(content.getRating())
                .createdAt(content.getCreatedAt())
                .updatedAt(content.getUpdatedAt())
                .build();
    }
}
