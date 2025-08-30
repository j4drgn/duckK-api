-- 데이터베이스 스키마 정의

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(255),
    provider VARCHAR(20) NOT NULL,
    mbti_type VARCHAR(10),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 채팅 세션 테이블
CREATE TABLE IF NOT EXISTS chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    emotion_type VARCHAR(50),
    emotion_score DOUBLE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 채팅 세션-메시지 연결 테이블
CREATE TABLE IF NOT EXISTS chat_session_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_session_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    order_num INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (chat_session_id) REFERENCES chat_sessions(id),
    FOREIGN KEY (message_id) REFERENCES chat_messages(id)
);

-- 콘텐츠 테이블
CREATE TABLE IF NOT EXISTS contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    chat_session_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    content_type VARCHAR(50) NOT NULL,
    content_url TEXT,
    thumbnail_url TEXT,
    metadata TEXT,
    is_viewed BOOLEAN DEFAULT FALSE,
    viewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (chat_session_id) REFERENCES chat_sessions(id)
);

-- 문화 콘텐츠 테이블 (기존 data.sql 파일에서 사용하는 테이블)
CREATE TABLE IF NOT EXISTS cultural_contents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    genre VARCHAR(50),
    creator VARCHAR(255),
    release_year INT,
    image_url VARCHAR(255),
    external_link VARCHAR(255),
    emotion_tags VARCHAR(255),
    rating DOUBLE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
