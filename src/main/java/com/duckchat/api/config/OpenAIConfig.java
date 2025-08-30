package com.duckchat.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key:dummy-api-key}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.transcription.url:https://api.openai.com/v1/audio/transcriptions}")
    private String openaiTranscriptionUrl;

    @Value("${openai.transcription.model:whisper-1}")
    private String openaiTranscriptionModel;

    @Value("${app.upload.dir:/tmp/duckchat-uploads}")
    private String uploadDir;

    @Value("${youtube.api.key:}")
    private String youtubeApiKey;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public String getOpenaiApiKey() {
        return openaiApiKey;
    }

    public String getOpenaiApiUrl() {
        return openaiApiUrl;
    }

    public String getOpenaiTranscriptionUrl() {
        return openaiTranscriptionUrl;
    }

    public String getOpenaiTranscriptionModel() {
        return openaiTranscriptionModel;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }
}