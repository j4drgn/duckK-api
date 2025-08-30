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
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {
    
    private final ContentService contentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getAllContents() {
        List<Content> contents = contentService.getAllContents();
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "유튜브 쇼츠 목록을 성공적으로 가져왔습니다.", responses));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentById(@PathVariable Long id) {
        return contentService.getContentById(id)
                .map(content -> {
                    ContentResponse response = ContentResponse.fromEntity(content);
                    return ResponseEntity.ok(new ApiResponse<>(true, "유튜브 쇼츠를 성공적으로 가져왔습니다.", response));
                })
                .orElse(ResponseEntity.notFound().build());
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
                String.format("%s 감정에 맞는 유튜브 쇼츠 목록을 성공적으로 가져왔습니다.", emotionTag), 
                responses));
    }
    
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getRecommendationsByEmotion(
            @RequestParam String emotion) {
        
        List<Content> contents = contentService.getRecommendationsByEmotion(emotion);
        
        // 추천을 5개로 제한
        List<Content> limitedContents = contents.stream().limit(5).toList();
        
        List<ContentResponse> responses = limitedContents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, 
                "감정 상태에 맞는 유튜브 쇼츠 추천 목록을 성공적으로 가져왔습니다.", 
                responses));
    }
    
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getLatestContents() {
        List<Content> contents = contentService.getLatestContents();
        List<ContentResponse> responses = contents.stream()
                .map(ContentResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "최신 유튜브 쇼츠 목록을 성공적으로 가져왔습니다.", responses));
    }
}
