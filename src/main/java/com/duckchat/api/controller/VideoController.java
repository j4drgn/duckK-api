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
import java.util.List;
import java.util.Map;
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
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRecommendedVideos(@PathVariable String emotion) {
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
                            "url", "/api/videos/stream/" + fileName,
                            "type", "local"
                        );
                    })
                    .limit(3)
                    .toList();
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, 
                String.format("%s 감정에 맞는 로컬 동영상을 추천합니다.", emotion), videos));
            
        } catch (IOException e) {
            return ResponseEntity.ok(new ApiResponse<>(false, "추천 동영상을 가져오는데 실패했습니다: " + e.getMessage(), null));
        }
    }
}