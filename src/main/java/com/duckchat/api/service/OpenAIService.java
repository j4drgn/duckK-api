package com.duckchat.api.service;

import com.duckchat.api.config.OpenAIConfig;
import com.duckchat.api.dto.openai.ChatCompletionRequest;
import com.duckchat.api.dto.openai.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final OpenAIConfig openAIConfig;

    public ChatCompletionResponse createChatCompletion(List<ChatCompletionRequest.Message> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.getOpenaiApiKey());

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .temperature(0.7)
                .max_tokens(1000)
                .top_p(1.0)
                .frequency_penalty(0.0)
                .presence_penalty(0.0)
                .build();

        HttpEntity<ChatCompletionRequest> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.postForObject(
                    openAIConfig.getOpenaiApiUrl(),
                    entity,
                    ChatCompletionResponse.class
            );
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생", e);
            throw new RuntimeException("OpenAI API 호출 중 오류가 발생했습니다.", e);
        }
    }

    public String generateResponse(String userMessage) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();
        
        // 시스템 메시지 추가 - 챗봇의 성격과 역할 정의
        messages.add(ChatCompletionRequest.Message.builder()
                .role("system")
                .content("당신은 덕키(Ducky)라는 친근한 AI 어시스턴트입니다. 사용자의 감정을 이해하고 공감하며, " +
                        "필요에 따라 적절한 상품을 추천해 주세요. 답변은 친근하고 간결하게 해주세요.")
                .build());
        
        // 사용자 메시지 추가
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build());

        ChatCompletionResponse response = createChatCompletion(messages);
        
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        } else {
            return "죄송합니다. 응답을 생성하는 중에 오류가 발생했습니다.";
        }
    }
    
    public String generateResponseWithHistory(List<ChatCompletionRequest.Message> messageHistory, String userMessage) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>(messageHistory);
        
        // 새로운 사용자 메시지 추가
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build());

        ChatCompletionResponse response = createChatCompletion(messages);
        
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        } else {
            return "죄송합니다. 응답을 생성하는 중에 오류가 발생했습니다.";
        }
    }
}
