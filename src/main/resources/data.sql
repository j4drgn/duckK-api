-- 문화 콘텐츠 샘플 데이터

-- 도서 데이터
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('해리 포터와 마법사의 돌', '11살 생일에 자신이 마법사라는 사실을 알게 된 해리 포터의 마법 세계 모험', 'BOOK', '판타지', 'J.K. 롤링', 1997, 'https://example.com/images/harry-potter.jpg', 'https://www.amazon.com/Harry-Potter-Sorcerers-Stone-Rowling/dp/059035342X', 'exciting,adventure,magical', 4.8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('아몬드', '감정을 느끼지 못하는 소년 선우와 폭력적인 소년 곤이의 특별한 우정에 관한 이야기', 'BOOK', '소설', '손원평', 2017, 'https://example.com/images/almond.jpg', 'https://www.amazon.com/Almond-Novel-Won-pyung-Sohn/dp/0062961373', 'healing,emotional,sad', 4.6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('사피엔스', '인류의 역사와 문명의 발전을 다룬 역사서', 'BOOK', '역사', '유발 하라리', 2011, 'https://example.com/images/sapiens.jpg', 'https://www.amazon.com/Sapiens-Humankind-Yuval-Noah-Harari/dp/0062316095', 'thoughtful,educational,inspiring', 4.7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('달러구트 꿈 백화점', '잠들어야만 입장할 수 있는 신비로운 꿈 백화점에서 일어나는 이야기', 'BOOK', '소설', '이미예', 2020, 'https://example.com/images/dollargut.jpg', 'https://www.aladin.co.kr/shop/wproduct.aspx?ItemId=249294215', 'healing,dreamy,relaxing', 4.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('어린 왕자', '어른들의 세계를 순수한 아이의 눈으로 바라보는 고전 소설', 'BOOK', '소설', '앙투안 드 생텍쥐페리', 1943, 'https://example.com/images/little-prince.jpg', 'https://www.amazon.com/Little-Prince-Antoine-Saint-Exup%C3%A9ry/dp/0156012197', 'thoughtful,healing,philosophical', 4.9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 영화 데이터
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('인셉션', '꿈 속의 꿈을 탐험하는 도둑들의 이야기', 'MOVIE', 'SF', '크리스토퍼 놀란', 2010, 'https://example.com/images/inception.jpg', 'https://www.netflix.com/title/70131314', 'exciting,thoughtful,mind-bending', 4.8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('인터스텔라', '지구 멸망 위기 속에서 새로운 행성을 찾아 떠나는 우주 탐험', 'MOVIE', 'SF', '크리스토퍼 놀란', 2014, 'https://example.com/images/interstellar.jpg', 'https://www.amazon.com/Interstellar-Matthew-McConaughey/dp/B00TU9UFTS', 'emotional,inspiring,mind-bending', 4.7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('어바웃 타임', '시간을 되돌릴 수 있는 능력을 가진 남자의 사랑과 인생 이야기', 'MOVIE', '로맨스', '리처드 커티스', 2013, 'https://example.com/images/about-time.jpg', 'https://www.netflix.com/title/70261674', 'healing,romantic,emotional', 4.6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('기생충', '부자와 가난한 두 가족의 만남으로 벌어지는 예상치 못한 사건들', 'MOVIE', '드라마', '봉준호', 2019, 'https://example.com/images/parasite.jpg', 'https://www.amazon.com/Parasite-English-Subtitled-Kang-Song/dp/B07YM14FRG', 'thought-provoking,dark,surprising', 4.9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('라라랜드', '꿈을 좇는 재즈 피아니스트와 배우 지망생의 사랑 이야기', 'MOVIE', '뮤지컬', '데미언 셔젤', 2016, 'https://example.com/images/lalaland.jpg', 'https://www.amazon.com/Land-Ryan-Gosling/dp/B01MZGJCDY', 'romantic,inspiring,musical', 4.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 음악 데이터
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('Dynamite', '밝고 경쾌한 디스코 팝 곡', 'MUSIC', '팝', 'BTS', 2020, 'https://example.com/images/dynamite.jpg', 'https://www.youtube.com/watch?v=gdZLi9oWNZg', 'happy,upbeat,energetic', 4.8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Blueming', '사랑에 빠진 순간을 표현한 밝은 팝 록 곡', 'MUSIC', '팝', '아이유', 2019, 'https://example.com/images/blueming.jpg', 'https://www.youtube.com/watch?v=D1PvIWdJ8xo', 'happy,romantic,upbeat', 4.7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('에필로그', '이별 후의 감정을 담은 감성적인 발라드', 'MUSIC', '발라드', '아이유', 2021, 'https://example.com/images/epilogue.jpg', 'https://www.youtube.com/watch?v=J_XDH_Z7Vi8', 'sad,emotional,healing', 4.9, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('봄날', '이별과 그리움, 그리고 희망을 담은 팝 발라드', 'MUSIC', '발라드', 'BTS', 2017, 'https://example.com/images/spring-day.jpg', 'https://www.youtube.com/watch?v=xEeFrLSkMm8', 'emotional,healing,hopeful', 4.8, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('Butter', '중독성 있는 리듬과 밝은 분위기의 팝 곡', 'MUSIC', '팝', 'BTS', 2021, 'https://example.com/images/butter.jpg', 'https://www.youtube.com/watch?v=WMweEpGlu_U', 'happy,upbeat,energetic', 4.6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

-- 팟캐스트 데이터
INSERT INTO contents (title, description, type, genre, creator, release_year, image_url, external_link, emotion_tags, rating, created_at, updated_at) VALUES
('책들의 주인', '작가와의 대화를 통해 책과 글쓰기에 대해 이야기하는 팟캐스트', 'PODCAST', '문화', '김하나', 2019, 'https://example.com/images/book-owner.jpg', 'https://www.podbbang.com/channels/1771443', 'thoughtful,relaxing,educational', 4.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('빠른 귀가', '일상 속 소소한 이야기와 음악을 들려주는 팟캐스트', 'PODCAST', '라이프스타일', '이동진', 2018, 'https://example.com/images/fast-return.jpg', 'https://www.podbbang.com/channels/1771876', 'relaxing,healing,comforting', 4.7, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP()),
('ASMR 수면 명상', '편안한 수면을 위한 ASMR과 명상 가이드', 'PODCAST', '건강', '김명상', 2020, 'https://example.com/images/asmr-meditation.jpg', 'https://www.podbbang.com/channels/1780123', 'relaxing,healing,calming', 4.6, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
