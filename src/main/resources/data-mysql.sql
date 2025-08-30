-- 샘플 데이터 삽입

-- 사용자 데이터
INSERT INTO users (email, nickname, password, provider, created_at, updated_at)
VALUES 
('test@example.com', '테스트 사용자', '$2a$10$eDIjO4uYXXSRxNzXUULDH.dHzOWBCNzCnPpkUGtHQEM0uYI5yvpVi', 'LOCAL', NOW(), NOW()),
('user1@example.com', '사용자1', '$2a$10$eDIjO4uYXXSRxNzXUULDH.dHzOWBCNzCnPpkUGtHQEM0uYI5yvpVi', 'LOCAL', NOW(), NOW()),
('user2@example.com', '사용자2', '$2a$10$eDIjO4uYXXSRxNzXUULDH.dHzOWBCNzCnPpkUGtHQEM0uYI5yvpVi', 'LOCAL', NOW(), NOW());

-- 채팅 세션 데이터
INSERT INTO chat_sessions (user_id, title, is_active, created_at, updated_at)
VALUES 
(1, '첫 번째 대화', TRUE, NOW(), NOW()),
(1, '두 번째 대화', TRUE, NOW(), NOW()),
(2, '사용자2의 대화', TRUE, NOW(), NOW());

-- 채팅 메시지 데이터
INSERT INTO chat_messages (user_id, type, content, emotion_type, emotion_score, created_at)
VALUES 
(1, 'USER', '안녕하세요!', 'HAPPY', 0.8, NOW()),
(1, 'ASSISTANT', '안녕하세요! 무엇을 도와드릴까요?', NULL, NULL, NOW()),
(1, 'USER', '오늘 기분이 좋아요', 'HAPPY', 0.9, NOW()),
(1, 'ASSISTANT', '기분이 좋으시다니 저도 기쁩니다! 무슨 좋은 일이 있으셨나요?', NULL, NULL, NOW()),
(2, 'USER', '요즘 추천할 만한 책이 있을까요?', 'NEUTRAL', 0.5, NOW()),
(2, 'ASSISTANT', '최근에 출간된 책 중에서 "아몬드"라는 소설이 인기가 많습니다. 감정을 느끼지 못하는 소년의 이야기로, 많은 독자들에게 감동을 주고 있어요.', NULL, NULL, NOW());

-- 채팅 세션-메시지 연결 데이터
INSERT INTO chat_session_messages (chat_session_id, message_id, order_num, created_at)
VALUES 
(1, 1, 1, NOW()),
(1, 2, 2, NOW()),
(1, 3, 3, NOW()),
(1, 4, 4, NOW()),
(2, 5, 1, NOW()),
(2, 6, 2, NOW());

-- 콘텐츠 데이터
INSERT INTO contents (user_id, chat_session_id, title, description, content_type, content_url, thumbnail_url, is_viewed, created_at, updated_at)
VALUES 
(1, 1, '해리 포터와 마법사의 돌', '11살 생일에 자신이 마법사라는 사실을 알게 된 해리 포터의 마법 세계 모험', 'BOOK', 'https://example.com/books/harry-potter', 'https://example.com/images/harry-potter.jpg', FALSE, NOW(), NOW()),
(1, 1, '인셉션', '꿈 속의 꿈을 탐험하는 도둑들의 이야기', 'MOVIE', 'https://example.com/movies/inception', 'https://example.com/images/inception.jpg', TRUE, NOW(), NOW()),
(2, 2, 'Dynamite', '밝고 경쾌한 디스코 팝 곡', 'MUSIC', 'https://example.com/music/dynamite', 'https://example.com/images/dynamite.jpg', FALSE, NOW(), NOW());

-- 문화 콘텐츠 샘플 데이터 (기존 data.sql 파일 내용 기반)
INSERT INTO cultural_contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('해리 포터와 마법사의 돌', '11살 생일에 자신이 마법사라는 사실을 알게 된 해리 포터의 마법 세계 모험', 'BOOK', '판타지', 'J.K. 롤링', 1997, 'https://example.com/images/harry-potter.jpg', 'https://www.amazon.com/Harry-Potter-Sorcerers-Stone-Rowling/dp/059035342X', 'exciting,adventure,magical', 4.8, NOW(), NOW()),
('아몬드', '감정을 느끼지 못하는 소년 선우와 폭력적인 소년 곤이의 특별한 우정에 관한 이야기', 'BOOK', '소설', '손원평', 2017, 'https://example.com/images/almond.jpg', 'https://www.amazon.com/Almond-Novel-Won-pyung-Sohn/dp/0062961373', 'healing,emotional,sad', 4.6, NOW(), NOW()),
('사피엔스', '인류의 역사와 문명의 발전을 다룬 역사서', 'BOOK', '역사', '유발 하라리', 2011, 'https://example.com/images/sapiens.jpg', 'https://www.amazon.com/Sapiens-Humankind-Yuval-Noah-Harari/dp/0062316095', 'thoughtful,educational,inspiring', 4.7, NOW(), NOW()),
('인셉션', '꿈 속의 꿈을 탐험하는 도둑들의 이야기', 'MOVIE', 'SF', '크리스토퍼 놀란', 2010, 'https://example.com/images/inception.jpg', 'https://www.netflix.com/title/70131314', 'exciting,thoughtful,mind-bending', 4.8, NOW(), NOW()),
('인터스텔라', '지구 멸망 위기 속에서 새로운 행성을 찾아 떠나는 우주 탐험', 'MOVIE', 'SF', '크리스토퍼 놀란', 2014, 'https://example.com/images/interstellar.jpg', 'https://www.amazon.com/Interstellar-Matthew-McConaughey/dp/B00TU9UFTS', 'emotional,inspiring,mind-bending', 4.7, NOW(), NOW()),
('Dynamite', '밝고 경쾌한 디스코 팝 곡', 'MUSIC', '팝', 'BTS', 2020, 'https://example.com/images/dynamite.jpg', 'https://www.youtube.com/watch?v=gdZLi9oWNZg', 'happy,upbeat,energetic', 4.8, NOW(), NOW()),
('Blueming', '사랑에 빠진 순간을 표현한 밝은 팝 록 곡', 'MUSIC', '팝', '아이유', 2019, 'https://example.com/images/blueming.jpg', 'https://www.youtube.com/watch?v=D1PvIWdJ8xo', 'happy,romantic,upbeat', 4.7, NOW(), NOW()),
('책들의 주인', '작가와의 대화를 통해 책과 글쓰기에 대해 이야기하는 팟캐스트', 'PODCAST', '문화', '김하나', 2019, 'https://example.com/images/book-owner.jpg', 'https://www.podbbang.com/channels/1771443', 'thoughtful,relaxing,educational', 4.5, NOW(), NOW());
