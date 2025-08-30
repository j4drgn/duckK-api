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
                        "적절한 문화 콘텐츠(책, 영화, 음악 등)를 추천해 주세요. 답변은 친근하고 간결하게 해주세요.")
                .build());
        
        // 사용자 메시지 추가
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build());

<<<<<<< HEAD
        ChatCompletionResponse response = createChatCompletion(messages);
        
        if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
            return response.getChoices().get(0).getMessage().getContent();
        } else {
            return "죄송합니다. 응답을 생성하는 중에 오류가 발생했습니다.";
=======
        try {
            ChatCompletionResponse response = createChatCompletion(messages);
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                log.warn("OpenAI API 응답이 비어 있습니다.");
                return getDefaultResponse(userMessage);
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return getDefaultResponse(userMessage);
        }
    }
    
    private String getDefaultResponse(String userMessage) {
        // API 호출 실패 시 기본 응답 제공
        if (userMessage.toLowerCase().contains("슬픔") || userMessage.toLowerCase().contains("우울") || 
            userMessage.toLowerCase().contains("슬퍼") || userMessage.toLowerCase().contains("힘들")) {
            return "기분이 안 좋으신가 보네요. 위로가 필요하실 때는 잔잔한 음악이나 따뜻한 영화가 도움이 될 수 있어요. '어바웃 타임'이나 아이유의 '밤편지' 같은 작품은 어떨까요?";
        } else if (userMessage.toLowerCase().contains("행복") || userMessage.toLowerCase().contains("기쁨") || 
                  userMessage.toLowerCase().contains("좋아") || userMessage.toLowerCase().contains("신나")) {
            return "기분이 좋으시군요! 그런 기분에는 밝고 경쾌한 음악이 어울릴 것 같아요. BTS의 'Dynamite'나 '버터'같은 곡을 추천해 드려요!";
        } else if (userMessage.toLowerCase().contains("책") || userMessage.toLowerCase().contains("독서")) {
            return "책을 찾고 계시는군요! '사피엔스'나 '달러구트 꿈 백화점' 같은 책이 많은 분들에게 사랑받고 있어요. 어떤 장르를 선호하시나요?";
        } else if (userMessage.toLowerCase().contains("영화") || userMessage.toLowerCase().contains("보고싶")) {
            return "영화 추천을 원하시는군요! '인터스텔라'나 '기생충' 같은 작품은 어떠세요? 스릴러, 로맨스, SF 중에 어떤 장르를 좋아하시나요?";
        } else if (userMessage.toLowerCase().contains("음악") || userMessage.toLowerCase().contains("노래")) {
            return "음악 추천이군요! 요즘 많은 분들이 좋아하시는 곡으로는 아이유의 'Blueming'이나 BTS의 '봄날' 등이 있어요. 어떤 분위기의 음악을 찾으시나요?";
        } else {
            return "안녕하세요! 오늘은 어떤 문화 콘텐츠를 추천해 드릴까요? 책, 영화, 음악 중에서 어떤 것에 관심이 있으신가요?";
        }
    }
    }
    
    private String getDefaultResponse(String userMessage) {
        // API 호출 실패 시 기본 응답 제공
        if (userMessage.toLowerCase().contains("슬픔") || userMessage.toLowerCase().contains("우울") || 
            userMessage.toLowerCase().contains("슬퍼") || userMessage.toLowerCase().contains("힘들")) {
            return "기분이 안 좋으신가 보네요. 위로가 필요하실 때는 잔잔한 음악이나 따뜻한 영화가 도움이 될 수 있어요. '어바웃 타임'이나 아이유의 '밤편지' 같은 작품은 어떨까요?";
        } else if (userMessage.toLowerCase().contains("행복") || userMessage.toLowerCase().contains("기쁨") || 
                  userMessage.toLowerCase().contains("좋아") || userMessage.toLowerCase().contains("신나")) {
            return "기분이 좋으시군요! 그런 기분에는 밝고 경쾌한 음악이 어울릴 것 같아요. BTS의 'Dynamite'나 '버터'같은 곡을 추천해 드려요!";
        } else if (userMessage.toLowerCase().contains("책") || userMessage.toLowerCase().contains("독서")) {
            return "책을 찾고 계시는군요! '사피엔스'나 '달러구트 꿈 백화점' 같은 책이 많은 분들에게 사랑받고 있어요. 어떤 장르를 선호하시나요?";
        } else if (userMessage.toLowerCase().contains("영화") || userMessage.toLowerCase().contains("보고싶")) {
            return "영화 추천을 원하시는군요! '인터스텔라'나 '기생충' 같은 작품은 어떠세요? 스릴러, 로맨스, SF 중에 어떤 장르를 좋아하시나요?";
        } else if (userMessage.toLowerCase().contains("음악") || userMessage.toLowerCase().contains("노래")) {
            return "음악 추천이군요! 요즘 많은 분들이 좋아하시는 곡으로는 아이유의 'Blueming'이나 BTS의 '봄날' 등이 있어요. 어떤 분위기의 음악을 찾으시나요?";
        } else {
            return "안녕하세요! 오늘은 어떤 문화 콘텐츠를 추천해 드릴까요? 책, 영화, 음악 중에서 어떤 것에 관심이 있으신가요?";
>>>>>>> 15da2fa63d8e724bef125a826768dcd95c8d33e2
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
