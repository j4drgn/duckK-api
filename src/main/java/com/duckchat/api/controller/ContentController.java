package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import com.duckchat.api.dto.ContentResponse;
import com.duckchat.api.entity.Content;
import com.duckchat.api.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class ContentController {
    
    private final ContentService contentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getAllContents() {
        List<Content> contents = contentService.getAllContents();
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "콘텐츠 목록을 성공적으로 가져왔습니다.", responses));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentById(@PathVariable Long id) {
        return contentService.getContentById(id)
                .map(content -> {
                    ContentResponse response = ContentResponse.fromEntity(content);
                    return ResponseEntity.ok(new ApiResponse<>(true, "콘텐츠를 성공적으로 가져왔습니다.", response));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getContentsByType(@PathVariable String type) {
        List<Content> contents = contentService.getContentsByType(type);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 유형의 콘텐츠 목록을 성공적으로 가져왔습니다.", type), 
                responses));
    }
    
    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getContentsByGenre(@PathVariable String genre) {
        List<Content> contents = contentService.getContentsByGenre(genre);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 장르의 콘텐츠 목록을 성공적으로 가져왔습니다.", genre), 
                responses));
    }
    
    @GetMapping("/creator/{creator}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getContentsByCreator(@PathVariable String creator) {
        List<Content> contents = contentService.getContentsByCreator(creator);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s의 콘텐츠 목록을 성공적으로 가져왔습니다.", creator), 
                responses));
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> searchContentsByTitle(@RequestParam String keyword) {
        List<Content> contents = contentService.searchContentsByTitle(keyword);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("'%s' 검색 결과를 성공적으로 가져왔습니다.", keyword), 
                responses));
    }
    
    @GetMapping("/emotion/{emotionTag}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getContentsByEmotionTag(@PathVariable String emotionTag) {
        List<Content> contents = contentService.getContentsByEmotionTag(emotionTag);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 감정에 맞는 콘텐츠 목록을 성공적으로 가져왔습니다.", emotionTag), 
                responses));
    }
    
    @GetMapping("/top-rated/{type}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getTopRatedContentsByType(@PathVariable String type) {
        List<Content> contents = contentService.getTopRatedContentsByType(type);
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 유형의 인기 콘텐츠 목록을 성공적으로 가져왔습니다.", type), 
                responses));
    }
    
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getRecommendationsByEmotionAndGenre(
            @RequestParam String emotion, 
            @RequestParam(required = false) String genre) {
        
        List<Content> contents;
        if (genre != null && !genre.isEmpty()) {
            contents = contentService.getRecommendationsByEmotionAndGenre(emotion, genre);
        } else {
            contents = contentService.getRecommendationsByEmotion(emotion);
        }
        
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                "감정 상태에 맞는 콘텐츠 추천 목록을 성공적으로 가져왔습니다.", 
                responses));
    }
    
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getLatestContents() {
        List<Content> contents = contentService.getLatestContents();
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "최신 콘텐츠 목록을 성공적으로 가져왔습니다.", responses));
    }
}
