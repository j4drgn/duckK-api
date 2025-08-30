package com.duckchat.api.controller;

import com.duckchat.api.entity.User;
import com.duckchat.api.service.EmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
public class EmotionController {

    private final EmotionService emotionService;

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeEmotion(@RequestParam("audio") MultipartFile audioFile,
                                                 @AuthenticationPrincipal User user) {
        try {
            // 임시 파일로 저장
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
            String fileName = "temp_" + System.currentTimeMillis() + ".wav";
            Path filePath = tempDir.resolve(fileName);
            Files.write(filePath, audioFile.getBytes());

            // 감정 분석 및 추천
            String result = emotionService.analyzeEmotionAndRecommend(filePath.toString(), user);

            // 임시 파일 삭제
            Files.deleteIfExists(filePath);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("오류: " + e.getMessage());
        }
    }
}
