-- 샘플 데이터 삽입

-- 사용자 데이터
INSERT INTO users (email, nickname, provider, created_at, updated_at)
VALUES 
('test@example.com', '테스트 사용자', 'LOCAL', NOW(), NOW()),
('user1@example.com', '사용자1', 'LOCAL', NOW(), NOW()),
('user2@example.com', '사용자2', 'LOCAL', NOW(), NOW());

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
INSERT INTO chat_session_messages (chat_session_id, message_id, message_order, created_at)
VALUES 
(1, 1, 1, NOW()),
(1, 2, 2, NOW()),
(1, 3, 3, NOW()),
(1, 4, 4, NOW()),
(2, 5, 1, NOW()),
(2, 6, 2, NOW());

-- 콘텐츠 데이터 (유튜브 쇼츠 위주)
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at)
VALUES 
('귀여운 고양이 쇼츠', '스트레스 해소에 딱 좋은 귀여운 고양이 영상 모음', 'YOUTUBE_SHORTS', '힐링', 'Cat Videos', 2024, 'https://example.com/images/cat-shorts.jpg', 'https://www.youtube.com/shorts/8QYHF8I3v9U', 'relaxing,healing,happy', 4.9, NOW(), NOW()),
('웃음 가득한 개그 쇼츠', '하루의 스트레스를 날려버릴 웃픈 개그 영상', 'YOUTUBE_SHORTS', '코미디', 'Comedy Shorts', 2024, 'https://example.com/images/comedy-shorts.jpg', 'https://www.youtube.com/shorts/dQw4w9WgXcQ', 'happy,funny,exciting', 4.7, NOW(), NOW()),
('힐링 음악 쇼츠', '마음이 편안해지는 잔잔한 음악과 풍경', 'YOUTUBE_SHORTS', '음악', 'Healing Music', 2024, 'https://example.com/images/healing-music.jpg', 'https://www.youtube.com/shorts/8QYHF8I3v9U', 'relaxing,calm,healing', 4.8, NOW(), NOW()),
('동기부여 영상 쇼츠', '힘이 나는 동기부여 명언과 성공 스토리', 'YOUTUBE_SHORTS', '동기부여', 'Motivation Daily', 2024, 'https://example.com/images/motivation-shorts.jpg', 'https://www.youtube.com/shorts/dQw4w9WgXcQ', 'inspiring,motivating,positive', 4.6, NOW(), NOW()),
('요가 명상 쇼츠', '집에서 쉽게 따라할 수 있는 요가와 명상', 'YOUTUBE_SHORTS', '웰빙', 'Yoga Life', 2024, 'https://example.com/images/yoga-shorts.jpg', 'https://www.youtube.com/shorts/8QYHF8I3v9U', 'relaxing,healing,calm', 4.5, NOW(), NOW());

-- 문화 콘텐츠 샘플 데이터 (기존 data.sql 파일 내용 기반)
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('해리 포터와 마법사의 돌', '11살 생일에 자신이 마법사라는 사실을 알게 된 해리 포터의 마법 세계 모험', 'BOOK', '판타지', 'J.K. 롤링', 1997, 'https://example.com/images/harry-potter.jpg', 'https://www.amazon.com/Harry-Potter-Sorcerers-Stone-Rowling/dp/059035342X', 'exciting,adventure,magical', 4.8, NOW(), NOW()),
('아몬드', '감정을 느끼지 못하는 소년 선우와 폭력적인 소년 곤이의 특별한 우정에 관한 이야기', 'BOOK', '소설', '손원평', 2017, 'https://example.com/images/almond.jpg', 'https://www.amazon.com/Almond-Novel-Won-pyung-Sohn/dp/0062961373', 'healing,emotional,sad', 4.6, NOW(), NOW()),
('사피엔스', '인류의 역사와 문명의 발전을 다룬 역사서', 'BOOK', '역사', '유발 하라리', 2011, 'https://example.com/images/sapiens.jpg', 'https://www.amazon.com/Sapiens-Humankind-Yuval-Noah-Harari/dp/0062316095', 'thoughtful,educational,inspiring', 4.7, NOW(), NOW()),
('인셉션', '꿈 속의 꿈을 탐험하는 도둑들의 이야기', 'MOVIE', 'SF', '크리스토퍼 놀란', 2010, 'https://example.com/images/inception.jpg', 'https://www.netflix.com/title/70131314', 'exciting,thoughtful,mind-bending', 4.8, NOW(), NOW()),
('인터스텔라', '지구 멸망 위기 속에서 새로운 행성을 찾아 떠나는 우주 탐험', 'MOVIE', 'SF', '크리스토퍼 놀란', 2014, 'https://example.com/images/interstellar.jpg', 'https://www.amazon.com/Interstellar-Matthew-McConaughey/dp/B00TU9UFTS', 'emotional,inspiring,mind-bending', 4.7, NOW(), NOW()),
('Dynamite', '밝고 경쾌한 디스코 팝 곡', 'MUSIC', '팝', 'BTS', 2020, 'https://example.com/images/dynamite.jpg', 'https://www.youtube.com/watch?v=gdZLi9oWNZg', 'happy,upbeat,energetic', 4.8, NOW(), NOW()),
('Blueming', '사랑에 빠진 순간을 표현한 밝은 팝 록 곡', 'MUSIC', '팝', '아이유', 2019, 'https://example.com/images/blueming.jpg', 'https://www.youtube.com/watch?v=D1PvIWdJ8xo', 'happy,romantic,upbeat', 4.7, NOW(), NOW()),
('책들의 주인', '작가와의 대화를 통해 책과 글쓰기에 대해 이야기하는 팟캐스트', 'PODCAST', '문화', '김하나', 2019, 'https://example.com/images/book-owner.jpg', 'https://www.podbbang.com/channels/1771443', 'thoughtful,relaxing,educational', 4.5, NOW(), NOW());
