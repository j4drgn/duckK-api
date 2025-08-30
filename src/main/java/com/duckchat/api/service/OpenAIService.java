package com.duckchat.api.service;

import com.duckchat.api.config.OpenAIConfig;
import com.duckchat.api.dto.VoiceMetadata;
import com.duckchat.api.dto.EmotionAnalysisResult;
import com.duckchat.api.dto.openai.ChatCompletionRequest;
import com.duckchat.api.dto.openai.ChatCompletionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    private final RestTemplate restTemplate;
    private final OpenAIConfig openAIConfig;
    private final ObjectMapper objectMapper;

    public ChatCompletionResponse createChatCompletion(List<ChatCompletionRequest.Message> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.getOpenaiApiKey());

    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .model("gpt-3.5-turbo-instant") // turbo-instant로 변경
        .messages(messages)
        .temperature(0.5) // 더 빠른 응답을 위해 낮춤
        .max_tokens(256) // 응답 길이 제한
        .top_p(0.8) // 다양성 제한
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

    // 시스템 메시지 추가 - 프롬프트 단순화
    messages.add(ChatCompletionRequest.Message.builder()
        .role("system")
        .content("너는 덕키야! 귀엽고 친근한 AI 친구야. 사용자의 감정을 공감하며, 짧고 빠르게 답변해줘.")
        .build());

        // 사용자 메시지 추가
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build());

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

    // 음성 메타데이터를 활용한 응답 생성
    public String generateResponseWithVoice(String userMessage, VoiceMetadata voiceMetadata) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();

        // 음성 메타데이터를 고려한 시스템 메시지 생성
    String systemMessage = buildSystemMessageWithVoiceMetadata(voiceMetadata);
    // 프롬프트 단순화: 감정/상황만 간단히 반영
    messages.add(ChatCompletionRequest.Message.builder()
        .role("system")
        .content(systemMessage + " 답변은 짧고 빠르게 해줘.")
        .build());

        // 사용자 메시지 추가 (음성 입력임을 표시)
        String voiceIndicator = voiceMetadata != null ? " [음성 입력] " : "";
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(voiceIndicator + userMessage)
                .build());

        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            java.util.concurrent.Future<String> future = executor.submit(() -> {
                ChatCompletionResponse response = createChatCompletion(messages);
                if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                    String content = response.getChoices().get(0).getMessage().getContent();
                    log.info("OpenAI API response with voice metadata: {}", content);
                    return content;
                } else {
                    log.warn("OpenAI API 응답이 비어 있습니다.");
                    return getDefaultResponse(userMessage);
                }
            });
            return future.get(7, java.util.concurrent.TimeUnit.SECONDS); // 7초 제한
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("OpenAI 응답 생성 타임아웃, 기본 응답 반환");
            return getDefaultResponse(userMessage);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return getDefaultResponse(userMessage);
        } finally {
            executor.shutdown();
        }
    }

    private String buildSystemMessageWithVoiceMetadata(VoiceMetadata metadata) {
        StringBuilder systemMessage = new StringBuilder();
        systemMessage.append("너는 덕키야! 귀여운 오리 같은 친구 같은 AI야. ");

        if (metadata != null) {
            boolean hasValidMetadata = false;

            // 음성 특성에 따른 응답 스타일 조정
            if (metadata.getPitch() != null && metadata.getPitch() != 0.0) {
                hasValidMetadata = true;
                if (metadata.getPitch() > 1.2) {
                    systemMessage.append("사용자가 높은 톤으로 말하고 있어요. 더 밝고 에너지 넘치는 응답을 해주세요. ");
                } else if (metadata.getPitch() < 0.8) {
                    systemMessage.append("사용자가 낮은 톤으로 말하고 있어요. 더 차분하고 진지한 응답을 해주세요. ");
                }
            }

            if (metadata.getSpeed() != null && metadata.getSpeed() != 0.0) {
                hasValidMetadata = true;
                if (metadata.getSpeed() > 1.3) {
                    systemMessage.append("사용자가 빠르게 말하고 있어요. 간결하고 빠른 응답을 해주세요. ");
                } else if (metadata.getSpeed() < 0.7) {
                    systemMessage.append("사용자가 천천히 말하고 있어요. 더 자세하고 공감하는 응답을 해주세요. ");
                }
            }

            if (metadata.getVolume() != null && metadata.getVolume() != 0.0) {
                hasValidMetadata = true;
                if (metadata.getVolume() > 1.5) {
                    systemMessage.append("사용자가 큰 소리로 말하고 있어요. 더 강한 공감을 표현해주세요. ");
                } else if (metadata.getVolume() < 0.5) {
                    systemMessage.append("사용자가 작은 소리로 말하고 있어요. 더 부드럽고 섬세한 응답을 해주세요. ");
                }
            }

            if (metadata.getDuration() != null && metadata.getDuration() != 0.0) {
                hasValidMetadata = true;
                if (metadata.getDuration() > 30.0) {
                    systemMessage.append("사용자가 긴 시간 동안 말했어요. 더 자세한 설명을 해주세요. ");
                } else if (metadata.getDuration() < 5.0) {
                    systemMessage.append("사용자가 짧게 말했어요. 간결한 응답을 해주세요. ");
                }
            }

            if (metadata.getConfidence() != null && metadata.getConfidence() != 0.0) {
                hasValidMetadata = true;
                if (metadata.getConfidence() < 0.7) {
                    systemMessage.append("음성 인식이 불확실해요. 더 명확한 질문을 해주세요. ");
                }
            }

            if (metadata.getIsQuestion() != null && metadata.getIsQuestion()) {
                hasValidMetadata = true;
                systemMessage.append("사용자가 의문문으로 말했어요. 질문에 대한 명확한 답변을 해주세요. ");
            }

            if (metadata.getDetectedEmotions() != null && !metadata.getDetectedEmotions().isEmpty()) {
                hasValidMetadata = true;
                systemMessage.append("감지된 감정 정보: ").append(metadata.getDetectedEmotions()).append(" ");
            }

            // 유효한 메타데이터가 없는 경우 기본 메시지
            if (!hasValidMetadata) {
                systemMessage.append("사용자의 음성 입력을 받고 있어요. 더 자연스럽고 친근한 응답을 해주세요. ");
            }
        } else {
            systemMessage.append("사용자의 음성 입력을 받고 있어요. 더 자연스럽고 친근한 응답을 해주세요. ");
        }

        systemMessage.append("사용자의 감정을 잘 이해하고 공감해줘. 재미있고 귀엽게 응답해줘. 문화 콘텐츠(책, 영화, 음악 등) 추천도 해줄게~");

        return systemMessage.toString();
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

    public String generateResponseWithHistory(List<ChatCompletionRequest.Message> messageHistory, String userMessage) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>(messageHistory);

        // 새로운 사용자 메시지 추가
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(userMessage)
                .build());

        try {
            ChatCompletionResponse response = createChatCompletion(messages);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API response: {}", content);
                return content;
            } else {
                log.warn("OpenAI API 응답이 비어 있습니다.");
                return getDefaultResponseWithHistory(messageHistory, userMessage);
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return getDefaultResponseWithHistory(messageHistory, userMessage);
        }
    }

    // 음성 메타데이터를 활용한 대화 히스토리 기반 응답 생성
    public String generateResponseWithHistoryAndVoice(List<ChatCompletionRequest.Message> messageHistory, String userMessage, VoiceMetadata voiceMetadata) {
        List<ChatCompletionRequest.Message> messages = new ArrayList<>();

        // 음성 메타데이터를 고려한 시스템 메시지 추가
        String systemMessage = buildSystemMessageWithVoiceMetadata(voiceMetadata);
        messages.add(ChatCompletionRequest.Message.builder()
                .role("system")
                .content(systemMessage)
                .build());

        // 이전 메시지 히스토리 추가 (시스템 메시지 제외)
        for (ChatCompletionRequest.Message msg : messageHistory) {
            if (!"system".equals(msg.getRole())) {
                messages.add(msg);
            }
        }

        // 새로운 사용자 메시지 추가 (음성 입력임을 표시)
        String voiceIndicator = voiceMetadata != null ? " [음성 입력] " : "";
        messages.add(ChatCompletionRequest.Message.builder()
                .role("user")
                .content(voiceIndicator + userMessage)
                .build());

        try {
            ChatCompletionResponse response = createChatCompletion(messages);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String content = response.getChoices().get(0).getMessage().getContent();
                log.info("OpenAI API response with voice metadata: {}", content);
                return content;
            } else {
                log.warn("OpenAI API 응답이 비어 있습니다.");
                return getDefaultResponseWithHistory(messageHistory, userMessage);
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            return getDefaultResponseWithHistory(messageHistory, userMessage);
        }
    }

    // 오디오 파일을 전사(Whisper) 호출
    // filePath는 서버에 임시 저장된 오디오 파일 경로
    public String transcribeAudioFile(String filePath, String language) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(openAIConfig.getOpenaiApiKey());
            // multipart/form-data
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new FileSystemResource(filePath));
            if (openAIConfig.getOpenaiTranscriptionModel() != null) {
                builder.part("model", openAIConfig.getOpenaiTranscriptionModel());
            }
            if (language != null) {
                builder.part("language", language);
            }

            MultiValueMap<String, HttpEntity<?>> multipart = builder.build();

            HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity = new HttpEntity(multipart, headers);

            // OpenAI의 transcription endpoint에 POST
            String response = restTemplate.postForObject(openAIConfig.getOpenaiTranscriptionUrl(), requestEntity, String.class);
            if (response == null) return null;
            // OpenAI transcription 응답에는 일반적으로 JSON { "text": "..." } 형태가 돌아옵니다.
            try {
                JsonNode node = objectMapper.readTree(response);
                if (node.has("text")) {
                    return node.get("text").asText();
                }
                // 일부 구현체는 최상위 문자열을 반환할 수 있으므로 그대로 반환
                return response;
            } catch (Exception ex) {
                log.warn("transcription response parsing failed, returning raw response");
                return response;
            }
        } catch (Exception e) {
            log.error("transcribeAudioFile error: {}", e.getMessage());
            return null;
        }
    }

    // 전사 텍스트와 옵션의 VoiceMetadata를 사용해 감정/상황 분석을 수행
    public EmotionAnalysisResult analyzeTranscriptEmotion(String transcript, VoiceMetadata metadata) {
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            java.util.concurrent.Future<EmotionAnalysisResult> future = executor.submit(() -> {
                try {
                    // 프롬프트를 더 강하게: 반드시 JSON만, 예시 포함, 추가 텍스트 금지
                    List<ChatCompletionRequest.Message> messages = new ArrayList<>();
                    messages.add(ChatCompletionRequest.Message.builder()
                            .role("system")
                            .content("당신은 감정 분석가입니다. 아래 사용자의 전사 텍스트를 분석하여 반드시 JSON만 반환하세요. 다음 필드를 포함해야 합니다: primaryEmotion (string), emotionScores (map string->float), situationLabel (string), confidence (0.0-1.0), recommendationKeywords (list). 예시: {\"primaryEmotion\":\"분노\",\"emotionScores\":{\"분노\":0.8,\"슬픔\":0.1},\"situationLabel\":\"불쾌한 상황\",\"confidence\":0.92,\"recommendationKeywords\":[\"진정\",\"휴식\"]}. JSON 외 텍스트, 설명, 인사말, 마크다운, 코드블록, 따옴표 등은 절대 포함하지 마세요.")
                            .build());

                    String userContent = "Transcript: " + transcript;
                    if (metadata != null) {
                        userContent += "\nVoiceMetadata: " + metadata.toString();
                    }

                    messages.add(ChatCompletionRequest.Message.builder()
                            .role("user")
                            .content(userContent)
                            .build());

                    ChatCompletionResponse response = createChatCompletion(messages);
                    if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                        String content = response.getChoices().get(0).getMessage().getContent();
                        EmotionAnalysisResult result = new EmotionAnalysisResult();
                        result.setRawJson(content);
                        // 반드시 JSON만 반환하도록 프롬프트를 강화했지만, 혹시 모를 예외 처리
                        try {
                            int start = content.indexOf('{');
                            int end = content.lastIndexOf('}');
                            String jsonPart = content;
                            if (start >= 0 && end > start) {
                                jsonPart = content.substring(start, end + 1);
                            }
                            JsonNode root = objectMapper.readTree(jsonPart);
                            if (root.has("primaryEmotion")) {
                                result.setPrimaryEmotion(root.get("primaryEmotion").asText());
                            }
                            if (root.has("confidence")) {
                                result.setConfidence(root.get("confidence").asDouble());
                            }
                            if (root.has("situationLabel")) {
                                result.setSituationLabel(root.get("situationLabel").asText());
                            }
                            if (root.has("recommendationKeywords") && root.get("recommendationKeywords").isArray()) {
                                List<String> keywords = new ArrayList<>();
                                for (JsonNode kn : root.get("recommendationKeywords")) {
                                    keywords.add(kn.asText());
                                }
                                result.setRecommendationKeywords(keywords);
                            }
                            if (root.has("emotionScores") && root.get("emotionScores").isObject()) {
                                var map = objectMapper.convertValue(root.get("emotionScores"), java.util.Map.class);
                                result.setEmotionScores(map);
                            }
                        } catch (Exception ex) {
                            log.warn("Failed to parse GPT analysis JSON: {}", ex.getMessage());
                        }
                        return result;
                    }
                } catch (Exception e) {
                    log.error("analyzeTranscriptEmotion error: {}", e.getMessage());
                }
                return null;
            });
            return future.get(7, java.util.concurrent.TimeUnit.SECONDS); // 7초 제한
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("감정분석 타임아웃, null 반환");
            return null;
        } catch (Exception e) {
            log.error("analyzeTranscriptEmotion error: {}", e.getMessage());
            return null;
        } finally {
            executor.shutdown();
        }
    }

    private String getDefaultResponseWithHistory(List<ChatCompletionRequest.Message> messageHistory, String userMessage) {
        // 히스토리에서 마지막 사용자 메시지 추출
        String lastUserMessage = "";
        for (int i = messageHistory.size() - 1; i >= 0; i--) {
            if ("user".equals(messageHistory.get(i).getRole())) {
                lastUserMessage = messageHistory.get(i).getContent();
                break;
            }
        }

        // 히스토리 요약 기반 기본 응답
        if (lastUserMessage.toLowerCase().contains("알바") || lastUserMessage.toLowerCase().contains("일") ||
            lastUserMessage.toLowerCase().contains("사람")) {
            return "이전 대화에서 알바나 사람 관련 이야기를 했었네요. 그 일에 대해 더 자세히 이야기해 주시면 공감하고 도와드릴게요! 😊";
        } else {
            return "이전 대화 내용을 기억하고 있어요. 더 자세한 이야기를 들려주시면 함께 고민해 보아요!";
        }
    }
}
