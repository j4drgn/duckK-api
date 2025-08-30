package com.duckchat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YouTubeRecommendation {
    private String title;
    private String description;
    private String query; // 검색 키워드
    private String videoId; // optional
}
