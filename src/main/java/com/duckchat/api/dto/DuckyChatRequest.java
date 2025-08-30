package com.duckchat.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;

@Data
public class DuckyChatRequest {
    @NotEmpty
    private String message;

    @NotEmpty
    private String characterProfile; // "F형" or "T형"

    private String extractedLabelsJson; // Can be null or a JSON string
}
