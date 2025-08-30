package com.duckchat.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSessionRequest {
    
    @NotBlank(message = "세션 제목은 필수입니다.")
    private String title;
}
