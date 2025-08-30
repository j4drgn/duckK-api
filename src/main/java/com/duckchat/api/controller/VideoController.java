package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {
    
    private static final String ARCHIVE_PATH = "/Users/ryugi62/Desktop/해커톤/아카이브";
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getVideoList() {
        try {
            Path archivePath = Paths.get(ARCHIVE_PATH);
            
            if (!Files.exists(archivePath)) {
                return ResponseEntity.ok(new ApiResponse<>(false, "아카이브 폴더를 찾을 수 없습니다.", null));
            }
            
            List<Map<String, String>> videos;
            try (Stream<Path> stream = Files.list(archivePath)) {
                videos = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp4"))
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
                        return Map.of(
                            "id", String.valueOf(Math.abs(fileName.hashCode())),
                            "title", fileNameWithoutExtension,
                            "filename", fileName,
                            "url", "/api/videos/stream/" + fileName
                        );
                    })
                    .limit(5)
                    .toList();
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "로컬 동영상 목록을 성공적으로 가져왔습니다.", videos));
            
        } catch (IOException e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "동영상 목록을 가져오는데 실패했습니다: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/stream/{filename}")
    public ResponseEntity<Resource> streamVideo(@PathVariable String filename) {
        try {
            Path videoPath = Paths.get(ARCHIVE_PATH, filename);
            
            if (!Files.exists(videoPath)) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(videoPath);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/recommend/{emotion}")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRecommendedVideos(
            @PathVariable String emotion,
            @RequestParam(required = false) String excludeIds,
            @RequestParam(required = false) String chatContext,
            @RequestParam(defaultValue = "3") int limit) {
        try {
            Path archivePath = Paths.get(ARCHIVE_PATH);
            
            if (!Files.exists(archivePath)) {
                return ResponseEntity.ok(new ApiResponse<>(false, "아카이브 폴더를 찾을 수 없습니다.", null));
            }
            
            // 제외할 ID들 파싱
            final Set<String> excludeIdSet;
            if (excludeIds != null && !excludeIds.trim().isEmpty()) {
                excludeIdSet = Set.of(excludeIds.split(","));
            } else {
                excludeIdSet = new HashSet<>();
            }
            
            List<Map<String, String>> videos;
            try (Stream<Path> stream = Files.list(archivePath)) {
                videos = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp4"))
                    .map(path -> {
                        String fileName = path.getFileName().toString();
                        String fileNameWithoutExtension = fileName.replaceFirst("[.][^.]+$", "");
                        String videoId = String.valueOf(Math.abs(fileName.hashCode()));
                        
                        return Map.of(
                            "id", videoId,
                            "title", fileNameWithoutExtension,
                            "filename", fileName,
                            "url", "/api/videos/stream/" + fileName,
                            "type", "local",
                            "emotion", emotion,
                            "chatContext", chatContext != null ? chatContext : "",
                            "recommendationScore", calculateRecommendationScore(fileNameWithoutExtension, emotion, chatContext)
                        );
                    })
                    .filter(video -> !excludeIdSet.contains(video.get("id"))) // 이미 본 동영상 제외
                    .sorted((v1, v2) -> Double.compare(
                        Double.parseDouble(v2.get("recommendationScore")), 
                        Double.parseDouble(v1.get("recommendationScore"))
                    )) // 추천 점수 기준 정렬
                    .limit(limit)
                    .toList();
            }
            
            String message = videos.isEmpty() 
                ? "새로운 추천 동영상이 없습니다. 모든 동영상을 시청하셨거나 조건에 맞는 동영상이 없습니다."
                : String.format("%s 감정 기반으로 %d개의 개인화된 동영상을 추천합니다.", emotion, videos.size());
            
            return ResponseEntity.ok(new ApiResponse<>(true, message, videos));
            
        } catch (IOException e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "추천 동영상을 가져오는데 실패했습니다: " + e.getMessage(), null));
        }
    }
    
    private String calculateRecommendationScore(String title, String emotion, String chatContext) {
        double score = 0.5; // 기본 점수
        
        // 1. 감정 기반 점수 계산
        score += calculateEmotionScore(title, emotion) * 0.4; // 40% 가중치
        
        // 2. 채팅 컨텍스트 기반 점수
        if (chatContext != null && !chatContext.trim().isEmpty()) {
            score += calculateContextScore(title, chatContext) * 0.3; // 30% 가중치
        }
        
        // 3. 다양성을 위한 랜덤 점수
        score += Math.random() * 0.3; // 30% 가중치로 다양성 확보
        
        return String.format("%.2f", Math.max(0.0, Math.min(1.0, score)));
    }
    
    private double calculateEmotionScore(String title, String emotion) {
        Map<String, List<String>> emotionKeywords = Map.of(
            "기쁨", List.of("웃음", "코미디", "재미", "유머", "즐거운", "happy", "funny", "comedy"),
            "슬픔", List.of("감동", "눈물", "드라마", "슬픈", "sad", "drama", "emotional"),
            "화남", List.of("액션", "전쟁", "격투", "스릴러", "action", "fight", "war"),
            "놀람", List.of("서프라이즈", "마술", "놀라운", "신기한", "amazing", "magic"),
            "두려움", List.of("공포", "호러", "무서운", "scary", "horror", "thriller"),
            "혐오", List.of("다큐", "현실", "documentary", "reality"),
            "관심", List.of("교육", "학습", "정보", "educational", "learning", "how-to")
        );
        
        List<String> keywords = emotionKeywords.getOrDefault(emotion, List.of());
        String lowerTitle = title.toLowerCase();
        
        long matchCount = keywords.stream()
            .mapToLong(keyword -> lowerTitle.contains(keyword.toLowerCase()) ? 1 : 0)
            .sum();
            
        return matchCount > 0 ? Math.min(1.0, matchCount * 0.3) : 0.0;
    }
    
    private double calculateContextScore(String title, String chatContext) {
        if (chatContext == null || chatContext.trim().isEmpty()) {
            return 0.0;
        }
        
        String lowerTitle = title.toLowerCase();
        String lowerContext = chatContext.toLowerCase();
        
        // 간단한 키워드 매칭
        String[] contextWords = lowerContext.split("\\s+");
        long matchCount = Arrays.stream(contextWords)
            .filter(word -> word.length() > 1) // 1글자 단어 제외
            .mapToLong(word -> lowerTitle.contains(word) ? 1 : 0)
            .sum();
            
        return matchCount > 0 ? Math.min(1.0, matchCount * 0.2) : 0.0;
    }
}