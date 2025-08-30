# Duck Chat API

Duck Chat 애플리케이션을 위한 Spring Boot 기반 백엔드 API 서버입니다.

## 목차

1. [기술 스택](#기술-스택)
2. [프로젝트 구조](#프로젝트-구조)
3. [주요 기능](#주요-기능)
4. [API 엔드포인트](#api-엔드포인트)
5. [개발 환경 설정](#개발-환경-설정)
6. [실행 방법](#실행-방법)
7. [프론트엔드 연동 방법](#프론트엔드-연동-방법)
8. [환경 변수 설정](#환경-변수-설정)
9. [데이터베이스 스키마](#데이터베이스-스키마)
10. [문제 해결](#문제-해결)

## 기술 스택

- **언어 및 프레임워크**

  - Java 17
  - Spring Boot 3.2.5
  - Spring Security
  - Spring Data JPA

- **인증 및 보안**

  - OAuth2 (카카오 로그인)
  - JWT 인증

- **데이터베이스**

  - H2 Database (개발 환경)
  - PostgreSQL (프로덕션 환경)

- **기타 라이브러리**
  - Lombok
  - Validation
  - Jackson
  - RestTemplate (OpenAI API 연동)

## 프로젝트 구조

```
src/main/java/com/duckchat/api
├── DuckChatApiApplication.java          # 애플리케이션 진입점
├── config                               # 설정 관련 클래스
│   ├── OpenAIConfig.java                # OpenAI API 설정
│   ├── SecurityConfig.java              # Spring Security 설정
│   └── WebConfig.java                   # CORS 및 웹 설정
├── controller                           # API 컨트롤러
│   ├── AuthController.java              # 인증 관련 API
│   ├── ChatController.java              # 채팅 관련 API
│   ├── ChatGPTController.java           # ChatGPT 연동 API
│   ├── HealthCheckController.java       # 헬스체크 API
│   └── UserController.java              # 사용자 관련 API
├── dto                                  # 데이터 전송 객체
│   ├── ApiResponse.java                 # API 응답 포맷
│   ├── ChatMessageRequest.java          # 채팅 메시지 요청 DTO
│   ├── ChatRequest.java                 # ChatGPT 요청 DTO
│   ├── ChatResponse.java                # ChatGPT 응답 DTO
│   ├── ChatSessionRequest.java          # 채팅 세션 요청 DTO
│   ├── TokenRefreshRequest.java         # 토큰 갱신 요청 DTO
│   ├── TokenResponse.java               # 토큰 응답 DTO
│   ├── UserInfoResponse.java            # 사용자 정보 응답 DTO
│   ├── UserUpdateRequest.java           # 사용자 정보 업데이트 요청 DTO
│   └── openai                           # OpenAI 관련 DTO
│       ├── ChatCompletionRequest.java   # OpenAI 요청 DTO
│       └── ChatCompletionResponse.java  # OpenAI 응답 DTO
├── entity                               # 데이터베이스 엔티티
│   ├── AuthProvider.java                # 인증 제공자 열거형
│   ├── ChatMessage.java                 # 채팅 메시지 엔티티
│   ├── ChatSession.java                 # 채팅 세션 엔티티
│   ├── ChatSessionMessage.java          # 채팅 세션-메시지 연결 엔티티
│   └── User.java                        # 사용자 엔티티
├── exception                            # 예외 처리
│   ├── GlobalExceptionHandler.java      # 전역 예외 핸들러
│   └── OAuth2AuthenticationProcessingException.java
├── repository                           # 데이터 접근 계층
│   ├── ChatMessageRepository.java       # 채팅 메시지 리포지토리
│   ├── ChatSessionMessageRepository.java # 채팅 세션-메시지 리포지토리
│   ├── ChatSessionRepository.java       # 채팅 세션 리포지토리
│   └── UserRepository.java              # 사용자 리포지토리
├── security                             # 보안 관련 클래스
│   ├── CustomUserDetailsService.java    # 사용자 상세 서비스
│   ├── JwtAuthenticationFilter.java     # JWT 인증 필터
│   ├── JwtTokenProvider.java            # JWT 토큰 생성 및 검증
│   ├── KakaoOAuth2UserInfo.java         # 카카오 사용자 정보
│   ├── OAuth2AuthenticationSuccessHandler.java # OAuth2 인증 성공 핸들러
│   ├── OAuth2UserInfo.java              # OAuth2 사용자 정보 추상 클래스
│   ├── OAuth2UserInfoFactory.java       # OAuth2 사용자 정보 팩토리
│   └── OAuth2UserService.java           # OAuth2 사용자 서비스
└── service                              # 비즈니스 로직
    ├── ChatService.java                 # 채팅 관련 서비스
    ├── OpenAIService.java               # OpenAI API 연동 서비스
    └── UserService.java                 # 사용자 관련 서비스
```

## 주요 기능

### 1. 카카오 소셜 로그인

- OAuth2 클라이언트를 통한 카카오 로그인 연동
- 로그인 성공 시 JWT 토큰 발급 (액세스 토큰 + 리프레시 토큰)
- 사용자 정보 자동 저장 및 업데이트

### 2. 사용자 관리

- 사용자 정보 조회 및 업데이트
- MBTI 타입 설정 기능
- 프로필 관리

### 3. 채팅 기록 CRUD

- 채팅 세션 생성, 조회, 업데이트, 삭제
- 채팅 메시지 저장 및 조회
- 사용자별 채팅 기록 관리
- 감정 분석 정보 저장

### 4. ChatGPT 연동

- OpenAI API를 통한 ChatGPT 연동
- 사용자 메시지에 대한 AI 응답 생성
- 컨텍스트 유지를 위한 대화 기록 관리
- 감정 정보를 포함한 맞춤형 응답

## API 엔드포인트

### 인증 API

- `GET /api/auth/me`: 현재 인증된 사용자 정보 조회
- `POST /api/auth/refresh`: 액세스 토큰 갱신

### 사용자 API

- `GET /api/users/me`: 현재 사용자 정보 조회
- `PUT /api/users/me`: 사용자 정보 업데이트 (닉네임, MBTI 타입)

### 채팅 API

- `POST /api/chat/sessions`: 새로운 채팅 세션 생성
- `GET /api/chat/sessions`: 사용자의 모든 채팅 세션 조회
- `GET /api/chat/sessions/{id}`: 특정 채팅 세션 조회
- `PUT /api/chat/sessions/{id}`: 채팅 세션 정보 업데이트
- `DELETE /api/chat/sessions/{id}`: 채팅 세션 삭제
- `POST /api/chat/messages`: 새로운 채팅 메시지 저장
- `GET /api/chat/sessions/{id}/messages`: 특정 세션의 모든 메시지 조회

### ChatGPT API

- `POST /api/chatgpt/chat`: 단일 메시지에 대한 ChatGPT 응답 생성
- `POST /api/chatgpt/chat/session/{sessionId}`: 세션 기반 대화 히스토리를 유지하며 ChatGPT 응답 생성

### 기타

- `GET /api/health`: 서버 상태 확인

## 개발 환경 설정

### 필수 요구사항

- JDK 17 이상
- Gradle 7.0 이상
- IDE (IntelliJ IDEA 권장)
- Git

### 개발 환경 설정 단계

1. 프로젝트 클론

   ```bash
   git clone https://github.com/your-username/duck-chat-api.git
   cd duck-chat-api
   ```

2. 필요한 환경 변수 설정 (아래 [환경 변수 설정](#환경-변수-설정) 참조)

3. 프로젝트 빌드
   ```bash
   ./gradlew build
   ```

## 실행 방법

### 로컬 개발 환경에서 실행

```bash
./gradlew bootRun
```

### JAR 파일로 실행

```bash
./gradlew build
java -jar build/libs/duck-chat-api-0.0.1-SNAPSHOT.jar
```

### 개발 프로필로 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 프로덕션 프로필로 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 프론트엔드 연동 방법

### 카카오 로그인 연동

1. 프론트엔드에서 카카오 로그인 버튼 클릭 시 `/oauth2/authorization/kakao` 엔드포인트로 리다이렉트

   ```javascript
   // 예시 코드 (React)
   const handleKakaoLogin = () => {
     window.location.href =
       "http://localhost:8080/api/oauth2/authorization/kakao";
   };
   ```

2. 로그인 성공 시 `http://localhost:5173?accessToken=xxx&refreshToken=yyy` 형태로 리다이렉트됨

3. 프론트엔드에서 URL 파라미터에서 토큰 추출 및 저장

   ```javascript
   // 예시 코드 (React)
   useEffect(() => {
     const params = new URLSearchParams(window.location.search);
     const accessToken = params.get("accessToken");
     const refreshToken = params.get("refreshToken");

     if (accessToken && refreshToken) {
       localStorage.setItem("accessToken", accessToken);
       localStorage.setItem("refreshToken", refreshToken);
       // 토큰 저장 후 메인 페이지로 리다이렉트
       window.location.href = "/";
     }
   }, []);
   ```

4. API 요청 시 Authorization 헤더에 `Bearer {accessToken}` 형태로 포함

   ```javascript
   // 예시 코드 (React)
   const fetchUserData = async () => {
     const response = await fetch("http://localhost:8080/api/users/me", {
       headers: {
         Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
       },
     });
     const data = await response.json();
     // 데이터 처리
   };
   ```

5. 액세스 토큰 만료 시 리프레시 토큰을 사용하여 갱신
   ```javascript
   // 예시 코드 (React)
   const refreshToken = async () => {
     const response = await fetch("http://localhost:8080/api/auth/refresh", {
       method: "POST",
       headers: {
         "Content-Type": "application/json",
       },
       body: JSON.stringify({
         refreshToken: localStorage.getItem("refreshToken"),
       }),
     });
     const data = await response.json();
     if (data.success) {
       localStorage.setItem("accessToken", data.data.accessToken);
     } else {
       // 리프레시 토큰도 만료된 경우 로그인 페이지로 리다이렉트
       window.location.href = "/login";
     }
   };
   ```

### 채팅 기능 연동

1. 채팅 세션 생성하기

   ```javascript
   // 예시 코드 (React)
   const createChatSession = async () => {
     const response = await fetch("http://localhost:8080/api/chat/sessions", {
       method: "POST",
       headers: {
         "Content-Type": "application/json",
         Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
       },
       body: JSON.stringify({
         title: "새로운 대화",
       }),
     });
     const data = await response.json();
     return data.data; // 생성된 세션 정보
   };
   ```

2. 채팅 세션 목록 조회하기

   ```javascript
   // 예시 코드 (React)
   const getChatSessions = async () => {
     const response = await fetch("http://localhost:8080/api/chat/sessions", {
       headers: {
         Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
       },
     });
     const data = await response.json();
     return data.data; // 세션 목록
   };
   ```

3. 특정 세션의 메시지 조회하기
   ```javascript
   // 예시 코드 (React)
   const getSessionMessages = async (sessionId) => {
     const response = await fetch(
       `http://localhost:8080/api/chat/sessions/${sessionId}/messages`,
       {
         headers: {
           Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
         },
       }
     );
     const data = await response.json();
     return data.data; // 메시지 목록
   };
   ```

### ChatGPT 기능 연동

1. 단일 메시지 전송하기

   ```javascript
   // 예시 코드 (React)
   const sendMessage = async (
     message,
     emotionType = null,
     emotionScore = null
   ) => {
     const response = await fetch("http://localhost:8080/api/chatgpt/chat", {
       method: "POST",
       headers: {
         "Content-Type": "application/json",
         Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
       },
       body: JSON.stringify({
         message: message,
         emotionType: emotionType,
         emotionScore: emotionScore,
       }),
     });
     const data = await response.json();
     return data.data; // ChatGPT 응답
   };
   ```

2. 세션 기반 대화하기

   ```javascript
   // 예시 코드 (React)
   const sendMessageWithSession = async (
     sessionId,
     message,
     emotionType = null,
     emotionScore = null
   ) => {
     const response = await fetch(
       `http://localhost:8080/api/chatgpt/chat/session/${sessionId}`,
       {
         method: "POST",
         headers: {
           "Content-Type": "application/json",
           Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
         },
         body: JSON.stringify({
           message: message,
           emotionType: emotionType,
           emotionScore: emotionScore,
         }),
       }
     );
     const data = await response.json();
     return data.data; // ChatGPT 응답
   };
   ```

3. 감정 정보를 포함한 메시지 전송하기
   ```javascript
   // 예시 코드 (React)
   // 예: 사용자가 슬픈 감정으로 메시지를 보낼 때
   const sendEmotionalMessage = async (sessionId, message) => {
     const response = await fetch(
       `http://localhost:8080/api/chatgpt/chat/session/${sessionId}`,
       {
         method: "POST",
         headers: {
           "Content-Type": "application/json",
           Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
         },
         body: JSON.stringify({
           message: message,
           emotionType: "sad",
           emotionScore: 0.8,
         }),
       }
     );
     const data = await response.json();
     return data.data; // ChatGPT 응답
   };
   ```

## 환경 변수 설정

### 필수 환경 변수

- `KAKAO_CLIENT_ID`: 카카오 개발자 콘솔에서 발급받은 클라이언트 ID
- `KAKAO_CLIENT_SECRET`: 카카오 개발자 콘솔에서 발급받은 클라이언트 시크릿
- `JWT_SECRET`: JWT 서명에 사용할 비밀 키 (최소 32자 이상 권장)
- `OPENAI_API_KEY`: OpenAI API 키 (ChatGPT 기능 사용 시 필수)

### 선택적 환경 변수

- `SPRING_PROFILES_ACTIVE`: 활성화할 스프링 프로필 (dev, prod 등)
- `SERVER_PORT`: 서버 포트 (기본값: 8080)
- `DATABASE_URL`: 데이터베이스 URL (PostgreSQL 사용 시)
- `DATABASE_USERNAME`: 데이터베이스 사용자 이름
- `DATABASE_PASSWORD`: 데이터베이스 비밀번호

### 환경 변수 설정 방법

#### 로컬 개발 환경 (.env 파일)

프로젝트 루트에 `.env` 파일 생성:

```
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret
JWT_SECRET=your_jwt_secret_key
OPENAI_API_KEY=your_openai_api_key
```

#### IntelliJ IDEA에서 설정

1. Run/Debug Configurations 열기
2. Environment variables 필드에 다음 입력:
   ```
   KAKAO_CLIENT_ID=your_kakao_client_id;KAKAO_CLIENT_SECRET=your_kakao_client_secret;JWT_SECRET=your_jwt_secret_key;OPENAI_API_KEY=your_openai_api_key
   ```

#### 시스템 환경 변수로 설정

```bash
# Linux/macOS
export KAKAO_CLIENT_ID=your_kakao_client_id
export KAKAO_CLIENT_SECRET=your_kakao_client_secret
export JWT_SECRET=your_jwt_secret_key
export OPENAI_API_KEY=your_openai_api_key

# Windows
set KAKAO_CLIENT_ID=your_kakao_client_id
set KAKAO_CLIENT_SECRET=your_kakao_client_secret
set JWT_SECRET=your_jwt_secret_key
set OPENAI_API_KEY=your_openai_api_key
```

## 데이터베이스 스키마

### 주요 테이블

#### users

| 컬럼명            | 타입         | 설명                           |
| ----------------- | ------------ | ------------------------------ |
| id                | BIGINT       | 기본 키                        |
| email             | VARCHAR(255) | 사용자 이메일 (유니크)         |
| nickname          | VARCHAR(255) | 사용자 닉네임                  |
| profile_image_url | VARCHAR(255) | 프로필 이미지 URL              |
| provider          | VARCHAR(20)  | 인증 제공자 (KAKAO, GOOGLE 등) |
| provider_id       | VARCHAR(255) | 제공자에서의 사용자 ID         |
| mbti_type         | VARCHAR(10)  | MBTI 타입                      |
| created_at        | TIMESTAMP    | 생성 시간                      |
| updated_at        | TIMESTAMP    | 수정 시간                      |

#### chat_messages

| 컬럼명        | 타입        | 설명                          |
| ------------- | ----------- | ----------------------------- |
| id            | BIGINT      | 기본 키                       |
| user_id       | BIGINT      | 사용자 ID (외래 키)           |
| type          | VARCHAR(20) | 메시지 타입 (USER, ASSISTANT) |
| content       | TEXT        | 메시지 내용                   |
| emotion_type  | VARCHAR(50) | 감정 타입                     |
| emotion_score | DOUBLE      | 감정 점수                     |
| created_at    | TIMESTAMP   | 생성 시간                     |

#### chat_sessions

| 컬럼명     | 타입         | 설명                |
| ---------- | ------------ | ------------------- |
| id         | BIGINT       | 기본 키             |
| user_id    | BIGINT       | 사용자 ID (외래 키) |
| title      | VARCHAR(255) | 세션 제목           |
| created_at | TIMESTAMP    | 생성 시간           |
| updated_at | TIMESTAMP    | 수정 시간           |
| is_active  | BOOLEAN      | 활성화 여부         |

#### chat_session_messages

| 컬럼명          | 타입      | 설명                   |
| --------------- | --------- | ---------------------- |
| id              | BIGINT    | 기본 키                |
| chat_session_id | BIGINT    | 채팅 세션 ID (외래 키) |
| message_id      | BIGINT    | 메시지 ID (외래 키)    |
| message_order   | INT       | 메시지 순서            |
| created_at      | TIMESTAMP | 생성 시간              |

## 문제 해결

### 일반적인 문제

#### 카카오 로그인이 작동하지 않는 경우

1. 카카오 개발자 콘솔에서 Redirect URI가 올바르게 설정되었는지 확인
   - `http://localhost:8080/login/oauth2/code/kakao`가 등록되어 있어야 함
2. 환경 변수 `KAKAO_CLIENT_ID`와 `KAKAO_CLIENT_SECRET`이 올바르게 설정되었는지 확인
3. 애플리케이션 로그에서 OAuth2 관련 오류 메시지 확인

#### JWT 토큰 관련 문제

1. 환경 변수 `JWT_SECRET`이 올바르게 설정되었는지 확인
2. 토큰이 만료되었는지 확인하고 필요한 경우 리프레시 토큰을 사용하여 갱신
3. 요청 헤더에 `Authorization: Bearer {token}` 형식으로 토큰이 포함되었는지 확인

#### CORS 오류

1. `application.yml`에서 CORS 설정 확인
2. 프론트엔드 도메인이 허용된 오리진 목록에 포함되어 있는지 확인
3. 필요한 HTTP 메서드와 헤더가 허용되었는지 확인

#### ChatGPT API 호출 오류

1. 환경 변수 `OPENAI_API_KEY`가 올바르게 설정되었는지 확인
2. API 키의 유효성 및 잔액 확인
3. 요청 형식이 올바른지 확인 (모델명, 최대 토큰 수 등)
4. 네트워크 연결 상태 확인

### 로깅 및 디버깅

#### 로그 확인 방법

```bash
# 애플리케이션 로그 확인 (Linux/macOS)
tail -f logs/application.log

# 스프링 부트 로그 레벨 설정 (application.yml)
logging:
  level:
    root: INFO
    com.duckchat.api: DEBUG
    org.springframework.security: DEBUG
    org.springframework.web.client.RestTemplate: DEBUG  # OpenAI API 호출 로그 확인
```

#### 디버그 모드로 실행

```bash
./gradlew bootRun --debug
```

### API 테스트 방법

#### cURL을 사용한 API 테스트

1. 카카오 로그인 리다이렉트 URL 확인

```bash
curl -v http://localhost:8080/api/oauth2/authorization/kakao
```

2. 토큰 갱신 테스트

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "your_refresh_token"}'
```

3. 사용자 정보 조회

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer your_access_token"
```

4. ChatGPT API 테스트

```bash
curl -X POST http://localhost:8080/api/chatgpt/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_access_token" \
  -d '{
    "message": "안녕하세요, 오늘 기분이 어떠세요?",
    "emotionType": "happy",
    "emotionScore": 0.9
  }'
```

#### Postman을 사용한 API 테스트

1. Postman 컬렉션 설정

   - 프로젝트 루트의 `postman` 디렉토리에 Postman 컬렉션 파일이 포함되어 있습니다.
   - Postman에서 해당 컬렉션을 가져와 환경 변수를 설정하세요.

2. 환경 변수 설정

   - `base_url`: API 서버 URL (예: http://localhost:8080/api)
   - `access_token`: 발급받은 액세스 토큰
   - `refresh_token`: 발급받은 리프레시 토큰

3. 테스트 실행
   - 컬렉션 내의 요청을 순서대로 실행하여 API 기능을 테스트할 수 있습니다.

### 지원 및 문의

문제가 계속되면 GitHub 이슈를 통해 문의하거나 개발팀에 직접 연락하세요.
