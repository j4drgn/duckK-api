package com.duckchat.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuckyResponse {
    private String textResponse;
    private List<String> recommendedVideoIds;
    private String emotion; // Dominant emotion detected
}
