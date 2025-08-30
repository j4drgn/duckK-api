-- 유튜브 쇼츠 샘플 데이터

-- 기존 데이터 삭제 (외래 키 제약 조건 고려)
DELETE FROM chat_session_messages;
DELETE FROM chat_sessions;
DELETE FROM chat_messages;
DELETE FROM contents;
DELETE FROM users;

-- 샘플 사용자 데이터
INSERT INTO users (email, nickname, password, provider, created_at, updated_at) VALUES
('uniqueuser@test.com', '테스트유저', '$2a$10$example.hash.for.test', 'LOCAL', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 유튜브 쇼츠 데이터

-- 유튜브 쇼츠 데이터
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('귀여운 고양이 쇼츠', '귀여운 고양이들의 일상 모습을 담은 쇼츠 모음', 'YOUTUBE_SHORTS', '동물', 'CatLover', 2024, 'https://example.com/images/cat-shorts.jpg', 'https://youtube.com/shorts/cat123', 'happy,relaxing,cute', 4.8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('웃음 가득한 개그 쇼츠', '하루를 웃음으로 시작하는 개그 영상 모음', 'YOUTUBE_SHORTS', '코미디', 'FunnyGuy', 2024, 'https://example.com/images/comedy-shorts.jpg', 'https://youtube.com/shorts/comedy456', 'happy,funny,energetic', 4.7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('힐링 자연 풍경 쇼츠', '마음의 안정을 주는 아름다운 자연 풍경', 'YOUTUBE_SHORTS', '자연', 'NatureHealer', 2024, 'https://example.com/images/nature-shorts.jpg', 'https://youtube.com/shorts/nature789', 'relaxing,healing,peaceful', 4.9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('감동적인 일상 이야기 쇼츠', '일상 속 작은 감동을 담은 쇼츠', 'YOUTUBE_SHORTS', '감동', 'DailyStory', 2024, 'https://example.com/images/touching-shorts.jpg', 'https://youtube.com/shorts/touch101', 'emotional,healing,inspiring', 4.6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('에너지 넘치는 댄스 쇼츠', '신나는 음악과 춤으로 가득한 쇼츠', 'YOUTUBE_SHORTS', '댄스', 'DanceMaster', 2024, 'https://example.com/images/dance-shorts.jpg', 'https://youtube.com/shorts/dance202', 'excited,energetic,fun', 4.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
