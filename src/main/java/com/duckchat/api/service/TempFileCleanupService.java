package com.duckchat.api.service;

import com.duckchat.api.config.OpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TempFileCleanupService {

    private final OpenAIConfig openAIConfig;

    // 10분마다 실행
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void cleanup() {
        try {
            File dir = new File(openAIConfig.getUploadDir());
            if (!dir.exists() || !dir.isDirectory()) return;
            File[] files = dir.listFiles();
            if (files == null) return;
            Instant cutoff = Instant.now().minusSeconds(60 * 60); // 1시간
            for (File f : files) {
                if (f.isFile()) {
                    Instant modified = Instant.ofEpochMilli(f.lastModified());
                    if (modified.isBefore(cutoff)) {
                        if (f.delete()) {
                            log.info("Deleted temp upload file: {}", f.getAbsolutePath());
                        } else {
                            log.warn("Failed to delete temp upload file: {}", f.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("TempFileCleanupService error: {}", e.getMessage());
        }
    }
}
