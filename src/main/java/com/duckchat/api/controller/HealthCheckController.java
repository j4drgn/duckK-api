package com.duckchat.api.controller;

import com.duckchat.api.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Duck Chat API 서버가 정상적으로 동작 중입니다.");
        
        return ResponseEntity.ok(new ApiResponse<>(true, "서버 상태 확인 완료", status));
    }
}