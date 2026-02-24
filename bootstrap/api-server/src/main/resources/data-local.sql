-- =============================================================================
-- Giftify Local Development Seed Data (H2)
-- =============================================================================
-- 이 파일은 local 프로파일에서 서버 시작 시 자동 실행됩니다.
-- H2 + ddl-auto:create 환경에서는 매번 테이블이 새로 생성되므로
-- ON CONFLICT 구문이 필요 없습니다.
--
-- Flyway 시드 데이터(V1.2.0~V1.2.4)와 1:1 동기화 상태입니다.
-- PostgreSQL setval() → H2 ALTER TABLE ... RESTART WITH 100 으로 변환.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. MEMBERS (회원) — V1.2.0
-- -----------------------------------------------------------------------------
INSERT INTO members (id, email, nickname, birthday, role, address, phone_num, name, status, auth_sub,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'qa-seller-giftify@team201.dev', '멍청한돼지0009', '1975-11-08', 'SELLER', '서울시 종로구',
        '010-1234-5678', '김주영', 'ACTIVE', 'auth0|6981838d48f8397cae06ddb0', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 'qa-buyer-giftify@team201.dev', '나른한고양이0013', '2003-02-14', 'BUYER', '서울시 송파구',
        '010-5678-1234', '김영주', 'ACTIVE', 'auth0|698183a503a368a7b14ca6ab', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 'qa-seller-giftify@naver.com', '멍청한고양이2013', '2003-02-14', 'SELLER',
        '서울시 송파구', '010-0002-9871', '김영주', 'ACTIVE', 'auth0|6981842c839dce07958f5a37', NOW(), NOW(), 'SYSTEM',
        'SYSTEM'),
       (4, 'admin-giftify@team201.dev', '관리자', '1999-01-01', 'ADMIN', '서울시 강남구',
        '010-0000-0000', 'TEAM201', 'ACTIVE', 'auth0|6981843a226ff0ca1e6a5ae8', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 'qa-buyer2-giftify@team201.dev', '졸린토끼0042', '1998-07-22', 'BUYER', '서울시 마포구',
        '010-3333-4444', '이수현', 'ACTIVE', 'auth0|698184500000000000000001', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 'qa-buyer3-giftify@team201.dev', '배고픈강아지0007', '2001-12-25', 'BUYER', '서울시 서초구',
        '010-5555-6666', '박지민', 'ACTIVE', 'auth0|698184500000000000000002', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE members
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 2. MEMBER_REPLICA (회원 레플리카 - catalog 모듈) — V1.2.0
-- -----------------------------------------------------------------------------
INSERT INTO member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 3. CORE_MEMBER_REPLICA (회원 레플리카 - core 모듈) — V1.0.0
-- -----------------------------------------------------------------------------
INSERT INTO core_member_replica (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 4. WALLET (지갑) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO wallet (id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 50000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 250000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 180000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wallet
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 5. CART (장바구니) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO cart (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE cart
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 6. PRODUCT (상품) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO product (id, seller_id, name, description, price, stock, status, image_key, category,
                     created_at, updated_at, created_by, updated_by)
VALUES
    -- ELECTRONICS (11-30)
    (11, 3, '소니 WH-1000XM5', '압도적인 정적, 최고의 노이즈 캔슬링 헤드폰', 449000, 50, 'ACTIVE',
     'products/11/sony-xm5.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (12, 3, '아이패드 에어 6세대', 'M2 칩 탑재, 프로의 성능을 담은 에어', 899000, 50, 'ACTIVE',
     'products/12/ipad-air-6.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (13, 3, '애플워치 시리즈 9', '가장 앞선 건강 센서와 더 밝아진 디스플레이', 599000, 50, 'ACTIVE',
     'products/13/apple-watch-9.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (14, 3, '로지텍 MX Master 3S', '무소음 클릭과 정교한 매그스피드 휠', 139000, 50, 'ACTIVE',
     'products/14/logitech-mx-3s.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (15, 3, '키크론 Q1 커스텀 키보드', '풀 알루미늄 바디의 묵직한 타건감', 215000, 50, 'ACTIVE',
     'products/15/keychron-q1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (16, 3, '삼성 갤럭시 S24 울트라', 'AI로 완성된 새로운 모바일 경험', 1698000, 50, 'ACTIVE',
     'products/16/galaxy-s24-ultra.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (17, 3, '벨킨 3-in-1 맥세이프 충전기', '아이폰, 애플워치, 에어팟 동시 급속 충전', 179000, 50, 'ACTIVE',
     'products/17/belkin-3in1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (18, 3, '필립스 휴 그라디언트', 'TV 뒷면을 화려하게 채우는 스마트 조명', 289000, 50, 'ACTIVE',
     'products/18/philips-hue.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (19, 3, '킨들 페이퍼화이트 5', '종이 책을 읽는 듯한 편안함, 최고의 이북리더', 189000, 50, 'ACTIVE',
     'products/19/kindle-pw5.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (20, 3, '고프로 히어로 12', '가장 강력한 흔들림 보정 기능의 액션캠', 558000, 50, 'ACTIVE',
     'products/20/gopro-12.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (21, 3, '네스프레소 버츄오 팝', '풍부한 크레마, 버튼 하나로 완성되는 커피', 199000, 50, 'DRAFT',
     'products/21/nespresso-vertuo.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (22, 3, '발뮤다 더 토스터', '죽은 빵도 살려내는 스팀 테크놀로지', 319000, 50, 'DRAFT',
     'products/22/balmuda-toaster.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (23, 3, 'LG 시네빔 PF50KA', '내 방을 영화관으로 만드는 휴대용 빔프로젝터', 450000, 50, 'DRAFT',
     'products/23/lg-cinebeam.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (24, 3, '보스 사운드링크 플렉스', '어떤 환경에서도 강력한 아웃도어 스피커', 179000, 50, 'DRAFT',
     'products/24/bose-flex.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (25, 3, '신지모루 맥세이프 보조배터리', '가볍게 붙여서 사용하는 10000mAh 고속 충전', 329000, 50, 'ACTIVE',
     'products/25/sinjimoru-magsafe.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (26, 3, '오큘러스 퀘스트 3', '혼합 현실(MR)로 즐기는 새로운 차원의 게이밍', 690000, 50, 'ACTIVE',
     'products/26/oculus-quest3.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (27, 3, '드롱기 데디카 커피머신', '홈바리스타를 위한 메탈 바디 에스프레소 머신', 259000, 50, 'ACTIVE',
     'products/27/delonghi-dedica.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (28, 3, '로보락 S8 Pro Ultra', '먼지 비움부터 걸레 세척까지 완전 자동화', 1590000, 50, 'ACTIVE',
     'products/28/roborock-s8.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (29, 3, '플레이스테이션 5 Slim', '더 작아진 크기, 더 강력해진 퍼포먼스', 628000, 50, 'ACTIVE',
     'products/29/ps5-slim.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (30, 3, '뱅앤올룹슨 Beosound A1', '작지만 웅장한 사운드의 프리미엄 스피커', 399000, 50, 'ACTIVE',
     'products/30/bo-a1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- LIVING (31-50)
    (31, 3, '조 말론 런던 캔들', '잉글리쉬 페어 앤 프리지아 홈 캔들', 115000, 50, 'ACTIVE',
     'products/31/jomalone-candle.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (32, 3, '이딸라 가스테헬미 볼', '이슬방울을 머금은 듯한 아름다운 유리 볼', 35000, 50, 'ACTIVE',
     'products/32/iittala-bowl.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (33, 3, '허먼밀러 뉴 에어론 체어', '사무용 의자의 끝판왕, 완벽한 인체공학', 2350000, 50, 'ACTIVE',
     'products/33/hermanmiller-aeron.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (34, 3, '루이스폴센 PH5', '빛을 부드럽게 분산시키는 북유럽 디자인 조명', 1450000, 50, 'ACTIVE',
     'products/34/louispoulsen-ph5.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (35, 3, '딥티크 룸 스프레이 베이', '장미 꽃다발과 블랙커런트 잎의 세련된 향', 95000, 50, 'ACTIVE',
     'products/35/diptyque-baies.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (36, 3, '사브르 파리 커틀러리', '프랑스 감성의 비비드한 컬러 커틀러리 세트', 48000, 50, 'ACTIVE',
     'products/36/sabre-cutlery.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (37, 3, '크로우캐년 법랑 머그', '감각적인 마블 패턴의 핸드메이드 머그', 22000, 50, 'ACTIVE',
     'products/37/crowcanyon-mug.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (38, 3, '킨토 데이오프 텀블러', '지치지 않는 일상을 위한 그립감 좋은 텀블러', 45000, 50, 'ACTIVE',
     'products/38/kinto-tumbler.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (39, 3, '아르테미데 네시노', '버섯 모양의 아이코닉한 디자인 테이블 램프', 280000, 50, 'ACTIVE',
     'products/39/artemide-nessino.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (40, 3, '렉슨 미나 미니 램프', '손바닥 위 작은 버섯, 버섯 모양 무드등', 39000, 50, 'ACTIVE',
     'products/40/lexon-mina.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (41, 3, '템퍼 오리지널 베개', '수면의 질을 바꾸는 메모리폼의 원조', 180000, 50, 'DRAFT',
     'products/41/tempur-pillow.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (42, 3, '까사미아 캄포 쇼파', '구름 위에 앉은 듯한 안락함, 모듈형 소파', 3200000, 50, 'DRAFT',
     'products/42/casamia-campo.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (43, 3, '아쿠아 디 파르마 디퓨저', '이탈리아 태양 아래 오렌지 나무의 활기', 135000, 50, 'ACTIVE',
     'products/43/acquadiparma-diffuser.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (44, 3, '일리 Y3.3 에스프레소 머신', '미니멀한 디자인과 진한 이탈리아 커피향', 139000, 50, 'ACTIVE',
     'products/44/illy-y33.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (45, 3, '르크루제 무쇠 주물 냄비', '요리의 맛을 살리는 클래식한 프랑스 냄비', 299000, 50, 'ACTIVE',
     'products/45/lecreuset-pot.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (46, 3, '오덴세 시네트 식기세트', '간결한 선과 동양적 미학의 2인 세트', 245000, 50, 'ACTIVE',
     'products/46/odense-set.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (47, 3, 'Aesop 아로마틱 핸드 워시', '만다린과 로즈마리 리프의 상쾌한 향', 50000, 50, 'ACTIVE',
     'products/47/aesop-handwash.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (48, 3, '발뮤다 더 팟', '커피 드립에 최적화된 우아한 전기 주전자', 199000, 50, 'ACTIVE',
     'products/48/balmuda-pot.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (49, 3, '몰스킨 노트', '창의력을 자극하는 클래식한 블랙 하드커버', 33000, 50, 'ACTIVE',
   'products/49/moleskine-note.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (50, 3, '프리츠한센 이케바나 화병', '꽃 한 송이의 가치를 높여주는 황동 화병', 240000, 50, 'ACTIVE',
   'products/50/fritzhansen-vase.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

       -- BEAUTY (51-65)
       (51, 3, '샤넬 가브리엘 향수', '네 가지 화이트 플라워의 눈부신 광채', 242000, 50, 'ACTIVE',
   'products/51/chanel-perfume.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (52, 3, '입생로랑 쿠션', '럭셔리한 패키지와 무결점 커버력의 조화', 98000, 50, 'ACTIVE',
     'products/52/ysl-cushion.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (53, 3, '에스티로더 갈색병', '밤 사이 깨어나는 피부의 자생 에너지', 182000, 50, 'ACTIVE',
   'products/53/esteelauder-anr.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (54, 3, '르 라보 상탈 33', '강렬한 중독성의 우디한 스모키 향', 420000, 50, 'ACTIVE',
     'products/54/lelabo-santal33.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (55, 3, '바이레도 블랑쉬', '방금 세탁한 셔츠처럼 깨끗하고 맑은 향', 350000, 50, 'ACTIVE',
     'products/55/byredo-blanche.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (56, 3, '설화수 자음 2종 세트', '부모님께 드리는 최고의 선물, 촉촉한 영양', 125000, 50, 'ACTIVE',
     'products/56/sulwhasoo-set.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (57, 3, '록시땅 핸드크림 세트', '시어버터의 풍부한 보습, 베스트셀러 3종', 45000, 50, 'ACTIVE',
     'products/57/loccitane-handcream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (58, 3, '디올 어딕트 립 글로우', '나만의 피부 톤에 맞춰 발색되는 국민 립밤', 48000, 50, 'ACTIVE',
     'products/58/dior-lipglow.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (59, 3, '키엘 울트라 훼이셜 크림', '24시간 유지되는 강력한 보습의 정석', 49000, 50, 'ACTIVE',
     'products/59/kiehls-cream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (60, 3, '메종 마르지엘라 레이지 선데이', '햇살 비치는 일요일 아침의 포근함', 185000, 50, 'ACTIVE',
     'products/60/margiela-perfume.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (61, 3, '라메르 크렘 드 라 메르', '바다의 생명력을 담은 기적의 크림', 650000, 50, 'DRAFT',
     'products/61/lamer-cream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (62, 3, 'SK-II 피테라 에센스', '피부 근본부터 케어하는 맑고 투영한 피부', 210000, 50, 'DRAFT',
     'products/62/sk2-essence.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (63, 3, '산타마리아노벨라 왁스 타블렛', '옷장 속 기분 좋은 은은한 피렌체의 향', 42000, 50, 'ACTIVE',
     'products/63/smn-wax.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (64, 3, '불리 1803 보디 오일', '수성 베이스의 고급스럽고 우아한 잔향', 85000, 50, 'ACTIVE',
     'products/64/buly-oil.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (65, 3, '라부르켓 리브 밤', '거친 입술을 즉각적으로 진정시키는 천연 립밤', 19000, 50, 'ACTIVE',
   'products/65/labruket-lipbalm.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

       -- TOYS & HOBBY (66-75)
       (66, 3, '레고 테크닉 페라리 Daytona', '실차와 동일한 정교한 디테일의 하이퍼카', 599000, 50, 'ACTIVE',
   'products/66/lego-ferrari.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (67, 3, '포켓몬 카드 스페셜 세트', '희귀한 홀로그램 카드가 포함된 한정판', 120000, 50, 'ACTIVE',
   'products/67/pokemon-card.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (68, 3, '실바니안 패밀리 타운하우스', '꿈꾸던 미니어처 세상, 디럭스 하우스', 159000, 50, 'ACTIVE',
   'products/68/sylvanian-house.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (69, 3, '보드게임 카탄', '전 세계에서 가장 사랑받는 전략 보드게임', 45000, 50, 'ACTIVE', 'products/69/catan.jpg',
   'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (70, 3, '인스탁스 미니 12', '찍는 즉시 현상되는 아날로그 감성 카메라', 119000, 50, 'ACTIVE',
   'products/70/instax-mini.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (71, 3, 'DJI 미니 4 프로', '초보자도 쉽게 날리는 4K 화질 드론', 950000, 50, 'DRAFT', 'products/71/dji-mini.jpg',
   'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (72, 3, '젤리캣 버니 L', '세상에서 가장 부드러운 애착 인형', 58000, 50, 'ACTIVE', 'products/72/jellycat-bunny.jpg',
   'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (73, 3, '건담 PG 언리쉬드 RX-78-2', '건프라 기술의 정점을 보여주는 기념비적 모델', 300000, 50, 'ACTIVE',
   'products/73/gundam-pg.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (74, 3, '모나미 153 ID 볼펜', '금속 바디의 묵직한 필기감, 각인 서비스 포함', 25000, 50, 'ACTIVE',
   'products/74/monami-153id.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (75, 3, '커세어 K70 RGB MK.2', '게이머를 위한 최강의 성능과 화려한 조명', 219000, 50, 'ACTIVE',
   'products/75/corsair-k70.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

       -- OUTDOOR & SPORTS (76-85)
       (76, 3, '헬리녹스 체어원 블랙', '경량 캠핑 의자의 대명사, 컴팩트한 휴대성', 110000, 50, 'ACTIVE',
   'products/76/helinox-chair.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (77, 3, '스노우피크 티타늄 컵 450', '가장 가벼운 캠핑 필수 아이템', 55000, 50, 'ACTIVE',
   'products/77/snowpeak-cup.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (78, 3, '노르디스크 이순 텐트', '감성 캠핑의 끝판왕 면 텐트', 1250000, 50, 'ACTIVE', 'products/78/nordisk-tent.jpg',
   'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (79, 3, '나이키 에어맥스 97', '클래식한 디자인과 편안한 쿠셔닝의 아이콘', 199000, 50, 'ACTIVE',
   'products/79/nike-airmax.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (80, 3, '룰루레몬 얼라인 팬츠', '버터처럼 부드러운 촉감의 요가 레깅스', 138000, 50, 'ACTIVE',
   'products/80/lululemon-align.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (81, 3, '스탠리 워터저그 7.5L', '무더운 여름에도 얼음이 유지되는 보냉력', 69000, 50, 'ACTIVE',
   'products/81/stanley-waterjug.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (82, 3, '파타고니아 레트로 X 자켓', '친환경 소재의 따뜻하고 포근한 플리스', 289000, 50, 'ACTIVE',
   'products/82/patagonia-retro.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (83, 3, '가민 피닉스 7', '탐험가를 위한 최강의 아웃도어 스마트워치', 990000, 50, 'DRAFT',
   'products/83/garmin-fenix7.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (84, 3, '아크테릭스 맨티스 26', '등산부터 일상까지 책임지는 멀티 백팩', 215000, 50, 'ACTIVE',
   'products/84/arcteryx-mantis.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (85, 3, '크레모아 멀티 페이스 L', '밤을 낮으로 만드는 강력한 캠핑 랜턴', 169000, 50, 'ACTIVE',
   'products/85/claymore-lantern.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

       -- PET (86-93)
       (86, 3, '러프웨어 전술 하네스', '어떤 험로도 거뜬한 고성능 아웃도어 하네스', 98000, 50, 'ACTIVE',
   'products/86/ruffwear-harness.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (87, 3, '분고 가죽 리드줄', '이탈리아 베지터블 가죽의 클래식한 무드', 65000, 50, 'ACTIVE',
   'products/87/bungo-leash.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (88, 3, '펫킷 스마트 정수기', '반려동물의 건강한 수분 섭취를 위한 저소음 정수기', 75000, 50, 'ACTIVE',
   'products/88/petkit-fountain.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (89, 3, '숨숨집 캣타워 4단', '인테리어를 해치지 않는 원목 캣타워', 320000, 50, 'ACTIVE', 'products/89/cat-tower.jpg',
   'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (90, 3, '강아지 프리미엄 영양제', '피부와 피모 개선을 위한 60일분 스틱', 42000, 50, 'ACTIVE',
   'products/90/dog-supplement.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (91, 3, '냥이 낚시 놀이 세트', '고양이의 본능을 깨우는 천연 깃털 장난감', 15000, 50, 'ACTIVE',
   'products/91/cat-toy.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (92, 3, '바르크 수제 간식 세트', '무항생제 한우와 연어로 만든 건강한 간식', 35000, 50, 'DRAFT',
   'products/92/bark-snack.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (93, 3, '강아지 비옷 옐로우', '비 오는 날에도 즐거운 산책을 위한 방수 코드', 28000, 50, 'ACTIVE',
   'products/93/dog-raincoat.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

       -- FASHION (94-100)
       (94, 3, '메종 키츠네 카디건', '여우 로고가 돋보이는 부드러운 램스울 소재', 365000, 50, 'ACTIVE',
   'products/94/kitsune-cardigan.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (95, 3, '아미 하트 반팔 티셔츠', '미니멀한 감성의 레드 하트 자수 티셔츠', 165000, 50, 'ACTIVE',
   'products/95/ami-tshirt.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (96, 3, '폴로 랄프로렌 린넨 셔츠', '여름 시즌 베스트셀러, 쾌적하고 클래식한 핏', 199000, 50, 'ACTIVE',
   'products/96/polo-shirt.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (97, 3, '젠틀몬스터 릴리트', '어떤 얼굴형에도 어울리는 플랫바 선글라스', 269000, 50, 'ACTIVE',
   'products/97/gentlemonster-lilit.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (98, 3, '보테가 베네타 카드 지갑', '인트레치아토 위빙이 돋보이는 럭셔리 지갑', 450000, 50, 'ACTIVE',
   'products/98/bottega-wallet.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (99, 3, '프라이탁 제이미', '버려진 방수포로 만든 세상에 단 하나뿐인 가방', 218000, 50, 'ACTIVE',
   'products/99/freitag-jamie.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (100, 3, '바버 리데스데일 퀼팅 자켓', '영국 왕실이 선택한 클래식한 퀼팅 자켓', 230000, 50, 'ACTIVE',
   'products/100/barbour-jacket.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE product
    ALTER COLUMN id RESTART WITH 200;

-- -----------------------------------------------------------------------------
-- 7. WISHLIST (위시리스트) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlist (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 'FRIENDS_ONLY', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 8. WISHLIST_ITEM (위시리스트 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_item (id, wishlist_id, product_id, wishlist_item_status, added_at, created_at, updated_at,
                           created_by, updated_by)
VALUES (1, 1, 1, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 1, 2, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 2, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 2, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 2, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 3, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (7, 3, 3, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (8, 5, 4, 'IN_PROGRESS', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (9, 5, 1, 'COMPLETED', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (10, 6, 5, 'PENDING', NOW(), NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlist_item
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 9. CART_ITEM (장바구니 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO cart_item (id, cart_id, target_type, target_id, amount, wishlist_item_status)
VALUES (1, 2, 'FUNDING_PENDING', 1, 359000.00, NULL),
       (2, 5, 'FUNDING_PENDING', 4, 699000.00, NULL),
       (3, 5, 'FUNDING_PENDING', 2, 23000.00, NULL),
       (4, 6, 'FUNDING_PENDING', 3, 415000.00, NULL),
       (5, 6, 'FUNDING_PENDING', 5, 89000.00, NULL);

ALTER TABLE cart_item
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 10. FUNDING (펀딩) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO funding (id, version, wishlist_item_id, product_id, productName, receiver_id, target_amount, current_amount,
                     status, deadline, achieved_at, closed_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 0,8, 4, 5, 699000, 15000, 'IN_PROGRESS',
     '2026-03-01 23:59:59', NULL, NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (2, 0,9, 51, '샤넬 가브리엘 향수', 5, 359000, 359000, 'ACHIEVED',
     '2026-02-28 23:59:59', '2026-02-07 14:30:00', NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (3, 0,4, 53, '에스티로더 갈색병',2, 415000, 120000, 'EXPIRED',
     '2026-02-01 23:59:59', NULL, '2026-02-01 23:59:59',
     NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    (4, 0,2, 53, '에스티로더 갈색병',2, 415000, 415000, 'ACHIEVED',
     '2026-02-15 23:59:59', '2026-02-13 23:59:59', NULL,
     NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE funding
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 11. FUNDING_PARTICIPANT_MEMBER (펀딩 참여자) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO funding_participant_member (id, funding_id, participant_id, nick_name, amount,
                                        created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 2, '나른한고양이0013', 10000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 1, 6, '배고픈강아지0007', 555000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 2, 2, '나른한고양이0013', 1200000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 2, 6,  '배고픈강아지0007', 159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 3, 2,  '나른한고양이0013', 1159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 4, 2,  '나른한고양이0013', 1159000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (7, 4, 6,  '배고픈강아지0007', 10000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (8, 4, 1,  '멍청한돼지0009', 50000, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (9, 4, 2,  '참가자4', 1000, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE funding_participant_member
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 12. ORDERS (주문) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO orders (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                      payment_id, origin_transaction_key, paid_at, confirmed_at, cancelled_at,
                      created_at, updated_at)
VALUES
    (1, 2, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 359000.00, 1, 'CARD', 'PAID',
     1, 'toss_tx_20260205_0001', '2026-02-05 17:05:00', NULL, NULL,
     '2026-02-05 17:00:00', '2026-02-05 17:05:00'),

    (2, 2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 23000.00, 1, 'CARD', 'CONFIRMED',
     2, 'toss_tx_20260205_0002', '2026-02-05 17:15:00', '2026-02-06 10:00:00', NULL,
     '2026-02-05 17:10:00', '2026-02-06 10:00:00'),

    (3, 5, 'ORD-20260207-C3D4E5F6G7H8-20260207120000', 415000.00, 1, 'CARD', 'CREATED',
     NULL, NULL, NULL, NULL, NULL,
     '2026-02-07 12:00:00', '2026-02-07 12:00:00'),

    (4, 6, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 448000.00, 2, 'KAKAO_PAY', 'PAID',
     NULL, NULL, NULL, NULL, NULL,
     '2026-02-08 14:00:00', '2026-02-08 14:05:00'),

    (5, 5, 'ORD-20260208-E5F6G7H8I9J0-20260208150000', 89000.00, 1, 'CARD', 'CANCELED',
     NULL, NULL, NULL, NULL, '2026-02-08 16:00:00',
     '2026-02-08 15:00:00', '2026-02-08 16:00:00');

ALTER TABLE orders
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 13. ORDER_ITEMS (주문 아이템) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO order_items (id, order_id, target_id, target_type, order_item_type, seller_id, receiver_id,
                           price, amount, status, cancelled_at, created_at, updated_at)
VALUES
    (1, 1, 1, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
     359000.00, 359000.00, 'PAID', NULL, '2026-02-05 17:00:00', '2026-02-05 17:05:00'),

    (2, 2, 2, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
     23000.00, 23000.00, 'PAID', NULL, '2026-02-05 17:10:00', '2026-02-06 10:00:00'),

    (3, 3, 3, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 5,
     415000.00, 415000.00, 'CREATED', NULL, '2026-02-07 12:00:00', '2026-02-07 12:00:00'),

    (4, 4, 2, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 6,
     23000.00, 23000.00, 'PAID', NULL, '2026-02-08 14:00:00', '2026-02-08 14:05:00'),
    (5, 4, 3, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 6,
     415000.00, 415000.00, 'PAID', NULL, '2026-02-08 14:00:00', '2026-02-08 14:05:00'),

    (6, 5, 5, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 5,
     89000.00, 89000.00, 'CANCELED', '2026-02-08 16:00:00', '2026-02-08 15:00:00', '2026-02-08 16:00:00');

ALTER TABLE order_items
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 14. PAYMENT (결제) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO payment (id, type, method, order_id, order_number, member_id,
                     origin_amount, paid_amount, refunded_amount, order_items_json, status,
                     payment_key, last_transaction_key, approve_code, paid_at,
                     created_at, updated_at, created_by, updated_by)
VALUES
    (1, 'FUNDING', 'CARD',
     1, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 2,
     359000.00, 359000.00, 0.00, '[{"targetId":1, "amount":{"amount":359000}, "sellerId":3}]', 'PAID',
     'toss_pk_20260205_0001', 'toss_tx_20260205_0001', 'approve_001', '2026-02-05 17:05:00',
     '2026-02-05 17:00:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),

    (2, 'FUNDING', 'CARD',
     2, 'ORD-20260205-B2C3D4E5F6G7-20260205171000', 2,
     23000.00, 23000.00, 0.00, '[{"targetId":2, "amount":{"amount":23000}, "sellerId":3}]', 'PAID',
     'toss_pk_20260205_0002', 'toss_tx_20260205_0002', 'approve_002', '2026-02-05 17:15:00',
     '2026-02-05 17:10:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),

    (3, 'FUNDING', 'KAKAO_PAY',
     3, 'ORD-20260208-D4E5F6G7H8I9-20260208140000', 6,
     448000.00, 448000.00, 0.00, '[{"targetId":2, "amount":{"amount":23000}, "sellerId":3}, {"targetId" : 3,
     "amount":{"amount":415000}, "sellerId":3}]', 'PAID',
     'toss_pk_20260208_0004', 'toss_tx_20260208_0004', 'approve_004', '2026-02-08 14:05:00',
     '2026-02-08 14:00:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE payment
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 15. PAYMENT_HISTORY (결제 이력) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO payment_history (id, payment_id, history_key, event_type, occurred_at, metadata,
                             created_at, updated_at, created_by, updated_by)
VALUES
    (1, 1, 'idem-20260205-0001-created', 'CREATED', '2026-02-05 17:00:00', NULL,
     '2026-02-05 17:00:00', '2026-02-05 17:00:00', 'SYSTEM', 'SYSTEM'),
    (2, 1, 'idem-20260205-0001-paid', 'PAID', '2026-02-05 17:05:00', NULL,
     '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
    (3, 2, 'idem-20260205-0002-created', 'CREATED', '2026-02-05 17:10:00', NULL,
     '2026-02-05 17:10:00', '2026-02-05 17:10:00', 'SYSTEM', 'SYSTEM'),
    (4, 2, 'idem-20260205-0002-paid', 'PAID', '2026-02-05 17:15:00', NULL,
     '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
    (5, 3, 'idem-20260208-0004-paid', 'PAID', '2026-02-08 14:05:00', NULL,
     '2026-02-08 14:05:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE payment_history
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 16. WALLET_HISTORY (지갑 이력) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO wallet_history (id, wallet_id, transaction_type, amount, balance_after,
                            reference_type, reference_id, occurred_at,
                            created_at, updated_at, created_by, updated_by)
VALUES
    (1, 2, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260205-001', '2026-02-04 10:00:00',
     '2026-02-04 10:00:00', '2026-02-04 10:00:00', 'SYSTEM', 'SYSTEM'),
    (2, 2, 'PAYMENT', 359000.00, 141000.00, 'PAYMENT', 'ORD-20260205-A1B2C3D4E5F6-20260205170000', '2026-02-05 17:05:00',
     '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
    (3, 2, 'PAYMENT', 23000.00, 118000.00, 'PAYMENT', 'ORD-20260205-B2C3D4E5F6G7-20260205171000', '2026-02-05 17:15:00',
     '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
    (4, 5, 'CHARGE', 300000.00, 300000.00, 'CHARGE', 'CHG-20260207-001', '2026-02-07 09:00:00',
     '2026-02-07 09:00:00', '2026-02-07 09:00:00', 'SYSTEM', 'SYSTEM'),
    (5, 6, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260208-001', '2026-02-08 10:00:00',
     '2026-02-08 10:00:00', '2026-02-08 10:00:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE wallet_history
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 17. FRIENDSHIPS(소셜) -
-- -----------------------------------------------------------------------------
    INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                             created_at, updated_at, created_by, updated_by)
    VALUES (1, 2, 3, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00','SYSTEM','SYSTEM');

ALTER TABLE friendships
    ALTER COLUMN id RESTART WITH 100;

-- =============================================================================
-- Seed Data Summary (Flyway V1.2.0~V1.2.4 동기화)
-- =============================================================================
-- Members: 6명 (SELLER 2, BUYER 3, ADMIN 1)
--   - ID 1: qa-seller-giftify@team201.dev (SELLER, 멍청한돼지0009)
--   - ID 2: qa-buyer-giftify@team201.dev (BUYER, 나른한고양이0013)
--   - ID 3: qa-seller-giftify@naver.com (SELLER, 멍청한고양이2013)
--   - ID 4: admin-giftify@team201.dev (ADMIN, 관리자)
--   - ID 5: qa-buyer2-giftify@team201.dev (BUYER, 졸린토끼0042)
--   - ID 6: qa-buyer3-giftify@team201.dev (BUYER, 배고픈강아지0007)
-- Wallets: 6개 (각 회원당 1개)
-- Carts: 6개 (각 회원당 1개)
-- Products: 90개 (ACTIVE 79, DRAFT 11) - seller_id=3
-- Wishlists: 6개 (PUBLIC 4, PRIVATE 1, FRIENDS_ONLY 1)
-- WishlistItems: 10개 (PENDING 6, IN_PROGRESS 2, COMPLETED 2)
-- CartItems: 5개 (buyer2: 2, buyer3: 2, buyer1: 1)
-- Fundings: 4개 (IN_PROGRESS 1, ACHIEVED 2, EXPIRED 1)
-- FundingParticipants: 9명
-- Orders: 5개 (PAID 2, CONFIRMED 1, CREATED 1, CANCELED 1)
-- OrderItems: 6개
-- Payments: 3개 (모두 PAID)
-- PaymentHistory: 5건
-- WalletHistory: 5건
-- =============================================================================
