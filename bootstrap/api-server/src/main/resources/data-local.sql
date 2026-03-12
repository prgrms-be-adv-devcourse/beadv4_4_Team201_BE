-- Spring Modulith 이벤트 테이블 컬럼 크기 확장
ALTER TABLE IF EXISTS event_publication ALTER COLUMN serialized_event TEXT;
ALTER TABLE IF EXISTS event_publication_archive ALTER COLUMN serialized_event TEXT;

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
INSERT INTO member_replicas (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 3. CORE_MEMBER_REPLICA (회원 레플리카 - core 모듈) — V1.0.0
-- -----------------------------------------------------------------------------
INSERT INTO core_member_replicas (id, nickname)
VALUES (1, '멍청한돼지0009'),
       (2, '나른한고양이0013'),
       (3, '멍청한고양이2013'),
       (4, '관리자'),
       (5, '졸린토끼0042'),
       (6, '배고픈강아지0007');

-- -----------------------------------------------------------------------------
-- 4. WALLET (지갑) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO wallets(id, member_id, balance, version, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 100000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 50000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 1000000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 0.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 250000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 180000.00, 0, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wallets
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 5. CART (장바구니) — V1.2.1
-- -----------------------------------------------------------------------------
INSERT INTO carts (id, member_id, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE carts
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 6. PRODUCT (상품) — V1.2.2 (Description 풍부하게 업데이트)
-- -----------------------------------------------------------------------------
INSERT INTO products (id, seller_id, name, description, price, stock, status, image_key, category,
                      created_at, updated_at, created_by, updated_by)
VALUES
    -- ELECTRONICS (11-30)
    (11, 3, '소니 WH-1000XM5',
     '업계 최고 수준의 노이즈 캔슬링으로 온전한 몰입감을 선사하는 무선 헤드폰입니다. 가볍고 편안한 착용감과 세련된 디자인으로 일상 속 음악 감상의 질을 한 차원 높여줍니다.', 449000, 50,
     'ACTIVE',
     'products/11/sony-xm5.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (12, 3, '아이패드 에어 6세대', '강력한 M2 칩을 탑재하여 프로급 성능을 자랑하는 태블릿입니다. 학업, 업무, 창작 작업까지 거침없이 소화하며 얇고 가벼운 디자인으로 완벽한 휴대성을 자랑합니다.',
     899000, 50, 'ACTIVE',
     'products/12/ipad-air-6.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (13, 3, '애플워치 시리즈 9',
     '가장 앞선 건강 센서와 더 밝아진 디스플레이로 돌아온 스마트워치입니다. 손가락 더블 탭 제스처 하나로 전화를 받고 음악을 제어하는 마법 같은 기능이 일상의 편리함을 극대화해 줍니다.', 599000,
     50, 'ACTIVE',
     'products/13/apple-watch-9.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (14, 3, '로지텍 MX Master 3S',
     '무소음 클릭과 1초에 1,000줄을 스크롤하는 매그스피드 휠을 장착한 마스터피스 마우스입니다. 인체공학적 디자인으로 장시간 작업하는 전문가들에게 최고의 작업 환경을 선사합니다.', 139000, 50,
     'ACTIVE',
     'products/14/logitech-mx-3s.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (15, 3, '키크론 Q1 커스텀 키보드',
     '풀 알루미늄 바디의 묵직함과 가스켓 마운트가 주는 부드러운 타건감이 일품인 기계식 키보드입니다. 커스텀 키보드 입문자는 물론 전문가의 마음까지 사로잡는 완벽한 타건감을 경험해 보세요.', 215000,
     50, 'ACTIVE',
     'products/15/keychron-q1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (16, 3, '삼성 갤럭시 S24 울트라',
     'AI 기능으로 완성된 새로운 차원의 모바일 경험을 제공합니다. 티타늄 프레임의 고급스러움과 압도적인 카메라 성능으로 일상의 모든 순간을 영화처럼 기록할 수 있습니다.', 1698000, 50,
     'ACTIVE',
     'products/16/galaxy-s24-ultra.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (17, 3, '벨킨 3-in-1 맥세이프 충전기',
     '아이폰, 애플워치, 에어팟을 동시에 급속 충전할 수 있는 프리미엄 무선 충전 스탠드입니다. 미니멀하고 세련된 디자인으로 데스크테리어나 침실 인테리어 소품으로도 손색이 없습니다.', 179000, 50,
     'ACTIVE',
     'products/17/belkin-3in1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (18, 3, '필립스 휴 그라디언트',
     'TV 시청이나 게이밍 환경에 맞춰 1,600만 가지 색상으로 실시간 반응하는 스마트 조명입니다. 화면 속 영상과 조명이 하나로 이어지는 압도적인 홈 엔터테인먼트를 즐겨보세요.', 289000, 50,
     'ACTIVE',
     'products/18/philips-hue.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (19, 3, '킨들 페이퍼화이트 5',
     '실제 종이 책을 읽는 듯한 편안함을 주는 6.8인치 고해상도 이북리더기입니다. 방수 기능과 한 번 충전으로 몇 주간 지속되는 배터리로 언제 어디서나 독서에 몰입할 수 있습니다.', 189000, 50,
     'ACTIVE',
     'products/19/kindle-pw5.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (20, 3, '고프로 히어로 12',
     '가장 강력한 흔들림 보정 기능(HyperSmooth 6.0)이 탑재된 액션캠입니다. 5.3K 고화질 영상 촬영을 지원하며 더욱 길어진 배터리 수명으로 아웃도어의 모든 순간을 놓치지 않습니다.',
     558000, 50, 'ACTIVE',
     'products/20/gopro-12.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (21, 3, '네스프레소 버츄오 팝', '버튼 한 번이면 풍부한 크레마의 깊은 맛을 내는 캡슐 커피 머신입니다. 컴팩트한 사이즈와 비비드한 컬러 라인업으로 주방에 화사한 포인트를 더해줍니다.',
     199000, 50, 'DRAFT',
     'products/21/nespresso-vertuo.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (22, 3, '발뮤다 더 토스터', '특유의 스팀 테크놀로지와 정밀한 온도 제어로 죽은 빵도 갓 구운 듯 살려내는 프리미엄 토스터입니다. 주방의 품격을 높여주는 클래식한 디자인이 특징입니다.',
     319000, 50, 'DRAFT',
     'products/22/balmuda-toaster.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (23, 3, 'LG 시네빔 PF50KA',
     'FHD 해상도와 내장 배터리로 집 안은 물론 캠핑장에서도 나만의 영화관을 만들어주는 휴대용 빔프로젝터입니다. webOS 탑재로 넷플릭스, 유튜브 등을 바로 즐길 수 있습니다.', 450000, 50,
     'DRAFT',
     'products/23/lg-cinebeam.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (24, 3, '보스 사운드링크 플렉스',
     '충격에 강한 실리콘 바디와 방수/방진 기능으로 어떤 아웃도어 환경에서도 안심하고 쓸 수 있는 블루투스 스피커입니다. 보스 특유의 깊고 웅장한 사운드가 공간을 가득 채웁니다.', 179000, 50,
     'DRAFT',
     'products/24/bose-flex.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (25, 3, '신지모루 맥세이프 보조배터리',
     '케이블 없이 스마트폰 뒷면에 가볍게 붙여서 사용하는 10000mAh 대용량 무선 보조배터리입니다. 강력한 자력으로 떨어질 염려 없이 빠르고 간편하게 충전할 수 있습니다.', 32000, 50,
     'ACTIVE',
     'products/25/sinjimoru-magsafe.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (26, 3, '오큘러스 퀘스트 3',
     '현실과 가상이 매끄럽게 융합되는 혼합 현실(MR)을 경험할 수 있는 차세대 VR 기기입니다. 압도적으로 선명해진 팬케이크 렌즈와 편안해진 착용감으로 몰입의 차원이 다릅니다.', 690000, 50,
     'ACTIVE',
     'products/26/oculus-quest3.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (27, 3, '드롱기 데디카 커피머신',
     '홈바리스타를 꿈꾸는 분들을 위한 15cm 슬림 폭의 메탈 바디 반자동 에스프레소 머신입니다. 나만의 취향에 맞는 원두로 최상의 에스프레소와 라떼 아트를 즐겨보세요.', 259000, 50,
     'ACTIVE',
     'products/27/delonghi-dedica.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (28, 3, '로보락 S8 Pro Ultra',
     '진공 청소, 물걸레질은 물론 먼지 비움과 걸레 세척, 건조까지 스스로 해내는 완전 자동화 로봇청소기입니다. 청소 스트레스에서 완전히 해방되는 삶의 질 상승 템입니다.', 1590000, 50,
     'ACTIVE',
     'products/28/roborock-s8.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (29, 3, '플레이스테이션 5 Slim',
     '기존 모델보다 30% 이상 부피가 줄어들었지만 압도적인 차세대 게이밍 퍼포먼스는 그대로 유지한 신형 PS5입니다. 초고속 SSD로 로딩 없는 쾌적한 플레이를 선사합니다.', 628000, 50,
     'ACTIVE',
     'products/29/ps5-slim.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (30, 3, '뱅앤올룹슨 Beosound A1', '작은 크기가 믿기지 않는 풍성한 360도 사운드를 내뿜는 프리미엄 휴대용 스피커입니다. 알루미늄 돔형 디자인과 가죽 스트랩이 감성적인 매력을 더합니다.',
     399000, 50, 'ACTIVE',
     'products/30/bo-a1.jpg', 'ELECTRONICS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- LIVING & KITCHEN (31-50)
    (31, 3, '조 말론 런던 캔들', '신선한 배의 달콤함과 프리지아의 은은한 꽃향기가 어우러진 시그니처 향의 홈 캔들입니다. 공간을 순식간에 고급스러운 부티크 호텔처럼 바꿔주는 마법을 경험해보세요.',
     115000, 50, 'ACTIVE',
     'products/31/jomalone-candle.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (32, 3, '이딸라 가스테헬미 볼',
     '핀란드어로 이슬방울이라는 뜻의 이름처럼 표면에 맺힌 유리 방울 디테일이 영롱한 디저트 볼입니다. 아이스크림, 과일, 요거트 등을 담으면 식탁 위가 더욱 다채로워집니다.', 35000, 50,
     'ACTIVE',
     'products/32/iittala-bowl.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (33, 3, '허먼밀러 뉴 에어론 체어', '실리콘 밸리 CEO들이 사랑하는 사무용 의자의 끝판왕입니다. 어떤 자세에서도 척추를 완벽하게 지지해 주며 메쉬 소재로 하루 종일 쾌적함을 유지합니다.',
     2350000, 0, 'ACTIVE',
     'products/33/hermanmiller-aeron.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (34, 3, '루이스폴센 PH5', '빛을 부드럽게 분산시켜 눈부심 없이 공간을 온화하게 밝혀주는 덴마크 디자인 철학의 정수입니다. 다이닝 룸에 걸어두는 것만으로도 완벽한 포인트 인테리어가 됩니다.',
     1450000, 50, 'ACTIVE',
     'products/34/louispoulsen-ph5.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (35, 3, '딥티크 룸 스프레이 베이', '갓 딴 싱싱한 장미 꽃다발과 블랙커런트 잎의 세련되고 풋풋한 향기가 가득 퍼지는 룸 스프레이입니다. 손님이 오기 전 가볍게 뿌려 센스 있는 공간을 연출하세요.',
     95000, 50, 'ACTIVE',
     'products/35/diptyque-baies.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (36, 3, '사브르 파리 커틀러리', '프랑스 파리의 감성을 담은 빈티지하면서도 비비드한 컬러의 커틀러리 세트입니다. 홈카페나 브런치 테이블을 사진 찍기 좋은 예쁜 공간으로 만들어줍니다.', 48000,
     50, 'ACTIVE',
     'products/36/sabre-cutlery.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (37, 3, '크로우캐년 법랑 머그', '감각적이고 트렌디한 마블 패턴이 돋보이는 핸드메이드 법랑 머그컵입니다. 깨지지 않는 내구성과 가벼운 무게로 집에서는 물론 캠핑장에서도 활용하기 좋습니다.',
     22000, 50, 'ACTIVE',
     'products/37/crowcanyon-mug.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (38, 3, '킨토 데이오프 텀블러', '지치지 않는 일상을 위한 그립감 좋은 매트 무광 텀블러입니다. 진공 이중 구조로 보온보냉이 탁월하며, 넓은 입구 덕분에 얼음을 넣고 세척하기도 매우 편리합니다.',
     45000, 50, 'ACTIVE',
     'products/38/kinto-tumbler.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (39, 3, '아르테미데 네시노', '우주선 혹은 귀여운 버섯 모양을 연상시키는 아이코닉한 디자인의 미드센추리 모던 테이블 램프입니다. 따뜻하고 부드러운 오렌지빛이 공간을 포근하게 감싸줍니다.',
     280000, 50, 'ACTIVE',
     'products/39/artemide-nessino.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (40, 3, '렉슨 미나 미니 램프', '손바닥 위에 올라가는 앙증맞은 버섯 모양의 무선 무드등입니다. 9가지 컬러 변경이 가능해 침실 취침등이나 식탁 위 포인트 조명으로 활용도가 높습니다.', 39000,
     50, 'ACTIVE',
     'products/40/lexon-mina.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (41, 3, '템퍼 오리지널 베개', '목과 어깨의 굴곡을 정확하게 지지해 주어 무중력 상태 같은 편안함을 제공하는 메모리폼 베개입니다. 자고 일어나도 찌뿌둥하신 분들에게 숙면을 선물하세요.',
     180000, 50, 'DRAFT',
     'products/41/tempur-pillow.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (42, 3, '까사미아 캄포 쇼파', '구름 위에 앉은 듯한 푹신한 안락함을 자랑하는 프리미엄 모듈형 소파입니다. 생활 오염에 강한 신소재 패브릭으로 관리가 쉽고 공간에 맞게 배치를 바꿀 수 있습니다.',
     3200000, 50, 'DRAFT',
     'products/42/casamia-campo.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (43, 3, '아쿠아 디 파르마 디퓨저', '이탈리아 남부의 눈부신 태양을 품은 오렌지와 레몬 나무의 활기찬 향을 담은 공간용 디퓨저입니다. 노란색 세련된 보틀이 인테리어 오브제로도 훌륭합니다.',
     135000, 50, 'ACTIVE',
     'products/43/acquadiparma-diffuser.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (44, 3, '일리 Y3.3 에스프레소 머신', '심플하고 미니멀한 디자인으로 어디에 두어도 예쁜 일리 캡슐 커피머신입니다. 일리 특유의 진하고 부드러운 이탈리아 커피 향을 집 안 가득 채워보세요.',
     139000, 50, 'INACTIVE',
     'products/44/illy-y33.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (45, 3, '르크루제 무쇠 주물 냄비', '열전도율과 보존율이 뛰어나 식재료 본연의 맛을 깊게 우려내는 프랑스 전통 무쇠 주물 냄비입니다. 찌개부터 솥밥까지 모든 요리의 품격을 높여줍니다.',
     299000, 50, 'ACTIVE',
     'products/45/lecreuset-pot.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (46, 3, '오덴세 시네트 식기세트', '간결한 선과 여백의 미가 돋보이는 동양적 미학의 2인 식기 세트입니다. 어떤 한식 요리를 담아도 정갈하고 고급스러운 상차림을 완성해 줍니다.', 245000,
     50, 'ACTIVE',
     'products/46/odense-set.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (47, 3, 'Aesop 아로마틱 핸드 워시',
     '만다린 코트와 로즈마리 리프의 상쾌하고 시트러스한 향이 손을 씻을 때마다 기분을 리프레시해 줍니다. 집들이 선물이나 욕실의 작은 럭셔리 아이템으로 추천합니다.', 50000, 50, 'ACTIVE',
     'products/47/aesop-handwash.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (48, 3, '발뮤다 더 팟', '최고의 핸드 드립을 위해 정교하게 디자인된 노즐이 특징인 우아한 전기 주전자입니다. 물을 끓이고 붓는 일련의 과정마저 즐거운 커피 시간으로 만들어줍니다.', 199000,
     50, 'ACTIVE',
     'products/48/balmuda-pot.jpg', 'KITCHEN', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (49, 3, '몰스킨 노트', '피카소와 헤밍웨이가 사랑했던 창의력을 자극하는 클래식 블랙 하드커버 노트입니다. 다이어리, 스케치북, 아이디어 노트 등 나만의 영감을 담아내기에 완벽합니다.', 33000,
     50, 'ACTIVE',
     'products/49/moleskine-note.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (50, 3, '프리츠한센 이케바나 화병', '일본 전통 꽃꽂이에서 영감을 받아 디자인된 황동 소재의 프리미엄 화병입니다. 들꽃 한 송이만 꽂아두어도 공간이 하나의 예술 작품처럼 살아납니다.', 240000,
     50, 'ACTIVE',
     'products/50/fritzhansen-vase.jpg', 'LIVING', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- BEAUTY (51-65)
    (51, 3, '샤넬 가브리엘 향수', '네 가지 화이트 플라워가 빚어내는 눈부시고 우아한 광채의 향기입니다. 여성스럽고 진취적인 태도를 지닌 분에게 선물하기 좋은 클래식하고 럭셔리한 향수입니다.',
     242000, 50, 'ACTIVE',
     'products/51/chanel-perfume.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (52, 3, '입생로랑 쿠션', '럭셔리한 가죽 패키지만으로도 소장 가치가 있는 베스트셀러 쿠션 팩트입니다. 얇고 촘촘하게 밀착되어 오랜 시간 무결점의 고급스러운 피부 표현을 유지해 줍니다.', 98000,
     50, 'ACTIVE',
     'products/52/ysl-cushion.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (53, 3, '에스티로더 갈색병', '밤사이 피부의 자생 에너지를 깨워주는 전설적인 나이트 리페어 세럼입니다. 풍부한 수분감과 안티에이징 효과로 다음 날 아침 확연히 달라진 피부결을 선사합니다.',
     182000, 50, 'ACTIVE',
     'products/53/esteelauder-anr.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (54, 3, '르 라보 상탈 33',
     '타오르는 장작과 카다멈, 바이올렛 아이리스가 어우러진 강렬하고 매혹적인 스모키 우디 향수입니다. 성별에 구애받지 않고 흔하지 않은 나만의 시그니처 향을 찾고 계신 분들께 추천합니다.', 420000,
     50, 'ACTIVE',
     'products/54/lelabo-santal33.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (55, 3, '바이레도 블랑쉬', '방금 햇볕에 바짝 말린 하얀 셔츠에서 날법한 깨끗하고 순수한 알데하이드 비누 향수입니다. 맑고 투명한 이미지를 연출하고 싶을 때 매일 뿌리기 좋은 향입니다.',
     350000, 0, 'INACTIVE',
     'products/55/byredo-blanche.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (56, 3, '설화수 자음 2종 세트',
     '깊은 보습과 영양으로 피부 바탕을 탄탄하게 다져주는 한방 기초 스킨케어 세트입니다. 고급스러운 패키지와 확실한 효능으로 명절이나 부모님 생신 선물로 변함없이 사랑받는 베스트셀러입니다.', 125000,
     50, 'ACTIVE',
     'products/56/sulwhasoo-set.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (57, 3, '록시땅 핸드크림 세트',
     '풍부한 시어버터가 건조한 손을 촉촉하게 감싸주는 전 세계 1위 핸드크림 베스트셀러 3종 세트입니다. 호불호 없는 포근한 향과 보습력으로 가벼운 축하 선물로 강력 추천합니다.', 45000, 50,
     'ACTIVE',
     'products/57/loccitane-handcream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (58, 3, '디올 어딕트 립 글로우',
     '개개인의 입술 수분에 반응하여 나만의 생기 있는 핑크빛으로 자연스럽게 발색되는 국민 립밤입니다. 끈적임 없는 보습감과 사랑스러운 패키지로 언제나 실패 없는 선물 아이템입니다.', 48000, 50,
     'ACTIVE',
     'products/58/dior-lipglow.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (59, 3, '키엘 울트라 훼이셜 크림',
     '빙하 당단백질 추출물이 함유되어 24시간 피부 수분을 꽉 잡아주는 수분크림의 정석입니다. 끈적임 없이 부드럽게 발리며 사계절 내내 온 가족이 함께 쓸 수 있는 순한 제형입니다.', 49000, 50,
     'ACTIVE',
     'products/59/kiehls-cream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (60, 3, '메종 마르지엘라 레이지 선데이',
     '따스한 햇살이 비치는 일요일 아침, 갓 세탁한 하얀 침구에서 뒹구는 듯한 포근하고 여유로운 느낌을 담은 향수입니다. 머스크와 은방울꽃이 어우러진 깨끗한 런드리 향을 즐겨보세요.', 185000, 50,
     'ACTIVE',
     'products/60/margiela-perfume.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (61, 3, '라메르 크렘 드 라 메르',
     '해초 발효 원액인 미라클 브로스 성분이 강력한 자생력을 부여하는 기적의 프리미엄 크림입니다. 민감하고 건조한 피부를 진정시키고 본연의 건강한 윤기를 되찾아 줍니다.', 650000, 50, 'DRAFT',
     'products/61/lamer-cream.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (62, 3, 'SK-II 피테라 에센스',
     '피부 고유의 리듬을 되찾아주는 피테라 성분이 90% 이상 함유되어 맑고 투영한 피부로 가꿔주는 워터 에센스입니다. 스킨케어 첫 단계에서 피부결을 매끄럽게 정돈해 줍니다.', 210000, 50,
     'DRAFT',
     'products/62/sk2-essence.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (63, 3, '산타마리아노벨라 왁스 타블렛',
     '피렌체 우디 향과 꽃봉오리들이 왁스에 콕콕 박혀 있는 우아한 방향제입니다. 옷장 속이나 방 문고리에 걸어두면 문을 열 때마다 기분 좋은 은은한 잔향이 퍼져 나옵니다.', 42000, 50,
     'ACTIVE',
     'products/63/smn-wax.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (64, 3, '불리 1803 보디 오일',
     '고대 그리스 뷰티 레시피에서 영감을 받은 식물성 보디 오일입니다. 샤워 직후 발라주면 피부에 수분 코팅막을 형성하여 실크처럼 부드러운 살결과 고급스러운 향기를 남깁니다.', 85000, 50,
     'ACTIVE',
     'products/64/buly-oil.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (65, 3, '라부르켓 리브 밤', '스웨덴 서해안의 자연을 담은 아몬드 오일과 코코넛 오일 베이스의 대용량 천연 립밤입니다. 갈라지고 거친 입술을 즉각적으로 촉촉하게 진정시켜 주는 보습 필수템입니다.',
     19000, 50, 'ACTIVE',
     'products/65/labruket-lipbalm.jpg', 'BEAUTY', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- TOYS & HOBBY (66-75)
    (66, 3, '레고 테크닉 페라리 Daytona',
     '페라리 고유의 유려한 디자인과 V12 엔진의 움직임까지 완벽하게 구현한 1:8 스케일의 하이퍼카 레고 조립 세트입니다. 성인 조립 팬들에게 최고의 몰입감과 성취감을 선사하는 소장용 마스터피스입니다.',
     599000, 50, 'ACTIVE',
     'products/66/lego-ferrari.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (67, 3, '포켓몬 카드 스페셜 세트',
     '수집가들의 마음을 설레게 할 한정판 홀로그램 희귀 카드가 포함된 프리미엄 확장팩 세트입니다. 포켓몬을 사랑하는 아이들은 물론 어른들에게도 최고의 서프라이즈 선물이 됩니다.', 120000, 50,
     'ACTIVE',
     'products/67/pokemon-card.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (68, 3, '실바니안 패밀리 타운하우스',
     '유럽풍의 우아한 건축미가 돋보이는 3층짜리 실바니안 패밀리 디럭스 하우스입니다. 아기자기한 가구를 배치하고 인형들과 함께 놀며 아이들의 무한한 상상력을 키워주세요.', 159000, 50,
     'ACTIVE',
     'products/68/sylvanian-house.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (69, 3, '보드게임 카탄',
     '자원을 채집하고 마을을 건설하여 무인도 카탄을 개척하는 전 세계에서 가장 사랑받는 보드게임의 클래식입니다. 가족이나 친구들과 모였을 때 시간 가는 줄 모르고 즐길 수 있는 최고의 전략 게임입니다.',
     45000, 50, 'ACTIVE', 'products/69/catan.jpg',
     'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (70, 3, '인스탁스 미니 12',
     '셔터를 누르는 순간 소중한 추억을 실물 사진으로 바로 인화해 주는 아날로그 감성 즉석카메라입니다. 자동 노출 조절과 클로즈업 모드가 탑재되어 초보자도 쉽게 밝고 선명한 사진을 찍을 수 있습니다.',
     119000, 50, 'ACTIVE',
     'products/70/instax-mini.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (71, 3, 'DJI 미니 4 프로',
     '249g 미만의 초경량 무게임에도 4K 고해상도 촬영과 전방위 장애물 감지 기능을 탑재한 강력한 미니 드론입니다. 초보자도 안심하고 여행의 아름다운 풍경을 전문가처럼 담아낼 수 있습니다.', 950000,
     50, 'DRAFT', 'products/71/dji-mini.jpg',
     'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (72, 3, '젤리캣 버니 L',
     '영국 왕실의 선택을 받은 세상에서 가장 부드러운 감촉의 국민 애착 인형입니다. 사랑스럽게 늘어진 귀와 보들보들한 털이 아이들에게는 심리적 안정을, 어른들에게는 소소한 힐링을 선사합니다.', 58000,
     50, 'ACTIVE', 'products/72/jellycat-bunny.jpg',
     'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (73, 3, '건담 PG 언리쉬드 RX-78-2',
     '반다이 건프라 40년 기술의 정점을 집약하여 한계를 돌파한 기념비적인 퍼펙트 그레이드 모델입니다. 조립의 재미를 극대화한 프레임 구조와 화려한 LED 기믹이 압도적인 전시 효과를 자랑합니다.',
     300000, 50, 'ACTIVE',
     'products/73/gundam-pg.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (74, 3, '모나미 153 ID 볼펜',
     '친숙한 153 볼펜의 디자인에 메탈 바디를 적용하여 묵직하고 매끄러운 프리미엄 필기감을 제공합니다. 무료 각인 서비스로 의미 있는 문구를 새겨 하나뿐인 특별한 입학, 취업 선물을 준비해 보세요.',
     25000, 50, 'ACTIVE',
     'products/74/monami-153id.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (75, 3, '커세어 K70 RGB MK.2',
     '항공기 등급의 알루미늄 프레임과 체리 MX 스위치를 탑재하여 극강의 반응속도와 내구성을 자랑하는 게이밍 기계식 키보드입니다. 커스터마이징 가능한 화려한 RGB 조명이 게임의 몰입도를 한층 높여줍니다.',
     219000, 50, 'ACTIVE',
     'products/75/corsair-k70.jpg', 'TOYS', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- OUTDOOR & SPORTS (76-85)
    (76, 3, '헬리녹스 체어원 블랙',
     '생수병 하나보다 가벼운 890g의 무게로 배낭에 쏙 들어가는 전설적인 경량 캠핑 의자입니다. 압도적인 휴대성과 놀라운 하중 견인력으로 캠핑, 백패킹, 페스티벌 어디서나 편안한 휴식을 제공합니다.',
     110000, 50, 'ACTIVE',
     'products/76/helinox-chair.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (77, 3, '스노우피크 티타늄 컵 450',
     '뛰어난 보온/보냉력과 깃털 같은 가벼움을 자랑하는 티타늄 소재의 초경량 캠핑용 싱글 컵입니다. 직화가 가능해 가벼운 코펠 대용으로도 쓰이며, 감성 캠퍼들의 필수 아이템으로 꼽힙니다.', 55000, 50,
     'ACTIVE',
     'products/77/snowpeak-cup.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (78, 3, '노르디스크 이순 텐트',
     '북유럽 특유의 감성이 묻어나는 친환경 테크니컬 코튼 소재로 제작되어 통기성이 우수하고 쾌적한 실내 환경을 유지하는 프리미엄 면 텐트입니다. 넓은 공간감으로 글램핑 같은 럭셔리한 캠핑을 즐길 수 있습니다.',
     1250000, 50, 'ACTIVE', 'products/78/nordisk-tent.jpg',
     'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (79, 3, '나이키 에어맥스 97',
     '일본의 고속 열차에서 영감을 받은 물결무늬 디자인과 전체 길이 에어 쿠셔닝으로 수십 년간 스니커즈 마니아들을 사로잡은 아이코닉한 운동화입니다. 스포티하면서도 스트릿한 매력으로 어떤 코디에도 잘 어울립니다.',
     199000, 50, 'ACTIVE',
     'products/79/nike-airmax.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (80, 3, '룰루레몬 얼라인 팬츠',
     '입은 듯 안 입은 듯 버터처럼 부드러운 Nulu™ 소재가 내 몸을 편안하게 감싸주는 요가 레깅스의 끝판왕입니다. 신축성이 뛰어나 요가나 필라테스는 물론 일상적인 애슬레저 룩으로도 완벽합니다.',
     138000, 50, 'ACTIVE',
     'products/80/lululemon-align.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (81, 3, '스탠리 워터저그 7.5L',
     '강력한 이중벽 폼 단열재가 한여름에도 최대 2일 동안 얼음을 유지해 주는 캠핑 필수 보냉통입니다. 원터치 푸시 버튼으로 아이들도 쉽게 물을 따를 수 있어 가족 캠핑의 든든한 동반자가 됩니다.',
     69000, 50, 'ACTIVE',
     'products/81/stanley-waterjug.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (82, 3, '파타고니아 레트로 X 자켓',
     '방풍 멤브레인이 내장된 두툼한 셰르파 플리스 원단이 한겨울 칼바람까지 완벽하게 막아주는 상징적인 아우터입니다. 친환경 재활용 소재를 사용하여 지구를 사랑하는 아웃도어인들의 교복이라 불립니다.',
     289000, 50, 'ACTIVE',
     'products/82/patagonia-retro.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (83, 3, '가민 피닉스 7',
     '강력한 태양광 충전 렌즈와 내장형 손전등, 군사 표준의 내구성을 갖춘 극한의 탐험가를 위한 최고급 멀티스포츠 스마트워치입니다. 러닝부터 등산, 골프까지 당신의 모든 아웃도어 데이터를 정확하게 측정합니다.',
     990000, 50, 'DRAFT',
     'products/83/garmin-fenix7.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (84, 3, '아크테릭스 맨티스 26',
     '가벼운 트레킹부터 출퇴근용 데일리 백팩까지 완벽하게 소화하는 뛰어난 범용성의 배낭입니다. 인체공학적 등판 패널과 체계적인 수납공간이 활동성을 높이고 어깨의 피로를 최소화해 줍니다.', 215000, 50,
     'ACTIVE',
     'products/84/arcteryx-mantis.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (85, 3, '크레모아 멀티 페이스 L',
     '어두운 밤의 캠핑장을 대낮처럼 환하게 밝혀주는 6,000루멘의 압도적인 밝기를 자랑하는 대용량 캠핑 랜턴입니다. 스마트폰 고속 충전 보조배터리로도 쓸 수 있어 실용성이 매우 뛰어납니다.', 169000,
     50, 'ACTIVE',
     'products/85/claymore-lantern.jpg', 'OUTDOOR', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- PET (86-93)
    (86, 3, '러프웨어 전술 하네스',
     '인체공학적 설계로 반려견의 목과 가슴을 압박하지 않으면서도 강력한 내구성을 자랑하는 프리미엄 아웃도어 하네스입니다. 산을 뛰어다니거나 거친 활동을 즐기는 에너지 넘치는 반려견에게 필수적인 안전 장비입니다.',
     98000, 50, 'ACTIVE',
     'products/86/ruffwear-harness.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (87, 3, '분고 가죽 리드줄',
     '최고급 이탈리아 베지터블 천연 가죽을 사용하여 사용할수록 자연스러운 태닝과 유연함이 더해지는 클래식한 리드줄입니다. 산책의 질을 높여주는 견고한 그립감과 세련된 디자인이 돋보입니다.', 65000, 50,
     'ACTIVE',
     'products/87/bungo-leash.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (88, 3, '펫킷 스마트 정수기',
     '항상 깨끗하고 신선한 물을 순환시켜 반려동물의 음수량을 자연스럽게 늘려주는 스마트 급수기입니다. 3중 여과 필터가 불순물을 걸러주며, 초저소음 모터 설계로 예민한 고양이들도 거부감 없이 사용합니다.',
     75000, 50, 'ACTIVE',
     'products/88/petkit-fountain.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (89, 3, '숨숨집 캣타워 4단',
     '최상급 자작나무 원목으로 튼튼하게 제작되어 흔들림 없이 고양이의 수직 본능을 채워주는 미니멀한 4단 캣타워입니다. 부드러운 곡선 디자인이 어떤 인테리어와도 조화롭게 어울리며 포근한 숨숨집이 안정감을 줍니다.',
     320000, 50, 'ACTIVE', 'products/89/cat-tower.jpg',
     'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (90, 3, '강아지 프리미엄 영양제',
     '관절 건강과 면역력 증진에 도움을 주는 유효 성분들을 까다롭게 엄선하여 담아낸 프리미엄 영양제 60일분입니다. 기호성이 높은 츄어블 형태로 간식처럼 쉽게 급여하며 노령견의 활력을 되찾아 줍니다.',
     42000, 50, 'ACTIVE',
     'products/90/dog-supplement.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (91, 3, '냥이 낚시 놀이 세트',
     '천연 깃털과 바스락거리는 소리로 사냥 본능을 자극하여 뚱냥이도 단숨에 날렵하게 만드는 마성의 고양이 장난감 세트입니다. 내구성이 뛰어난 낚싯대와 교체형 미끼들로 구성되어 오랫동안 즐겁게 놀 수 있습니다.',
     15000, 50, 'ACTIVE',
     'products/91/cat-toy.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (92, 3, '바르크 수제 간식 세트',
     '방부제와 인공첨가물 없이 무항생제 한우와 알래스카 연어만을 건조해 만든 휴먼 그레이드 프리미엄 수제 간식입니다. 알러지 걱정 없이 안심하고 먹일 수 있어 반려견을 위한 특별한 특식으로 추천합니다.',
     35000, 50, 'DRAFT',
     'products/92/bark-snack.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (93, 3, '강아지 비옷 옐로우',
     '가벼운 방수 원단으로 비 오는 날에도 털 젖음 걱정 없이 산책을 즐길 수 있게 해주는 귀여운 노란색 우비입니다. 야간 산책의 안전을 위한 반사 띠 디테일과 입히기 쉬운 밸크로 타입으로 실용성을 더했습니다.',
     28000, 50, 'ACTIVE',
     'products/93/dog-raincoat.jpg', 'PET', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),

    -- FASHION (94-101)
    (94, 3, '메종 키츠네 카디건',
     '시그니처인 귀여운 폭스 헤드 로고가 가슴에 수놓아진 부드러운 램스울 소재의 브이넥 가디건입니다. 베이직하면서도 프렌치한 무드로 간절기 아우터나 한겨울 이너 포인트로 두루 활용하기 좋습니다.',
     365000, 50, 'ACTIVE',
     'products/94/kitsune-cardigan.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (95, 3, '아미 하트 반팔 티셔츠',
     '미니멀한 디자인에 볼드한 레드 하트와 A 로고 자수가 강력한 포인트를 주는 파리지앵 감성의 오버핏 반팔 티셔츠입니다. 탄탄한 면 소재로 핏이 무너지지 않아 데일리 코디에 세련됨을 더해줍니다.',
     165000, 50, 'ACTIVE',
     'products/95/ami-tshirt.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (96, 3, '폴로 랄프로렌 린넨 셔츠',
     '최상급 통기성을 자랑하는 가벼운 린넨 소재로 만들어져 한여름에도 쾌적함을 유지하는 폴로의 베스트셀러 클래식 셔츠입니다. 데님이나 슬랙스 어디에 매치해도 단정하고 시원한 룩을 완성해 줍니다.',
     199000, 50, 'ACTIVE',
     'products/96/polo-shirt.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (97, 3, '젠틀몬스터 릴리트',
     '과하지 않은 라운드 쉐입의 오버사이즈 플랫바 디자인으로 동양인의 얼굴형을 가장 작고 갸름해 보이게 만들어 주는 스테디셀러 선글라스입니다. 자외선 99.9% 차단 렌즈로 시력 보호와 스타일을 동시에 챙겨보세요.',
     269000, 50, 'ACTIVE',
     'products/97/gentlemonster-lilit.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (98, 3, '보테가 베네타 카드 지갑',
     '이탈리아 장인의 섬세한 인트레치아토 위빙 디테일이 돋보이는 군더더기 없이 우아한 명품 카드지갑입니다. 한 손에 들어오는 슬림한 사이즈에 최고급 카프스킨 가죽의 질감이 남녀 모두에게 고급스러운 선물이 됩니다.',
     450000, 50, 'ACTIVE',
     'products/98/bottega-wallet.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (99, 3, '프라이탁 제이미',
     '수년간 유럽을 누빈 화물차의 방수포를 재활용하여 세상에 단 하나뿐인 디자인과 색상을 지닌 힙한 메신저 백입니다. 환경을 생각하는 지속 가능한 패션 아이템으로 캐주얼 룩에 멋스러운 포인트를 줍니다.',
     218000, 50, 'ACTIVE',
     'products/99/freitag-jamie.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (100, 3, '바버 리데스데일 퀼팅 자켓',
     '영국 왕실이 보증하는 다이아몬드 퀼팅 패턴과 코듀로이 카라 디테일의 아이코닉한 헤리티지 재킷입니다. 정장 위에도 가볍게 걸치기 좋아 비즈니스 캐주얼부터 주말 나들이까지 가을/겨울 클래식 스타일링의 필수품입니다.',
     230000, 50, 'ACTIVE',
     'products/100/barbour-jacket.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
    (101, 3, '자라 키즈 패딩 조끼',
     '추운 겨울 우리 아이의 체온을 따뜻하게 지켜주면서도 팔의 활동성을 보장하는 가볍고 포근한 데일리 패딩 조끼입니다. 겉옷 위에 덧입거나 실내에서 가볍게 입기 좋아 실용성 만점의 겨울 필수 아이템입니다.',
     50000, 0, 'ACTIVE',
     'products/101/zara-kids-vest.jpg', 'FASHION', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE products
    ALTER COLUMN id RESTART WITH 200;

-- -----------------------------------------------------------------------------
-- 7. WISHLIST (위시리스트) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlists (id, member_id, visibility, created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (2, 2, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (3, 3, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (4, 4, 'PRIVATE', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (5, 5, 'PUBLIC', NOW(), NOW(), 'SYSTEM', 'SYSTEM'),
       (6, 6, 'FRIENDS_ONLY', NOW(), NOW(), 'SYSTEM', 'SYSTEM');

ALTER TABLE wishlists
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 8. WISHLIST_ITEM (위시리스트 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO wishlist_items (id, wishlist_id, product_id, wishlist_item_status, added_at, created_at, updated_at, created_by, updated_by)

VALUES (1, 2, 17, 'COMPLETED', '2026-02-27 04:38:07.137213', '2026-02-27 04:38:07.137213', '2026-02-27 05:02:52.542501',
        NULL, NULL),
       (2, 2, 91, 'PENDING', '2026-02-27 04:38:18.888371', '2026-02-27 04:38:18.888371', '2026-02-27 05:03:04.54353',
        NULL, NULL),
    (3, 2, 101, 'PENDING', '2026-02-27 04:38:34.915845', '2026-02-27 04:38:34.921555', '2026-02-27 04:38:34.921555', NULL, NULL),
    (4, 2, 52, 'PENDING', '2026-02-27 04:38:45.005384', '2026-02-27 04:38:45.013825', '2026-02-27 04:38:45.013825', NULL, NULL),
    (5, 3, 28, 'PENDING', '2026-02-27 04:39:41.138494', '2026-02-27 04:39:41.152269', '2026-02-27 04:39:41.152269', NULL, NULL),
    (6, 3, 94, 'PENDING', '2026-02-27 04:39:51.842625', '2026-02-27 04:39:51.845744', '2026-02-27 04:39:51.845744', NULL, NULL),
    (7, 3, 69, 'PENDING', '2026-02-27 04:40:01.608242', '2026-02-27 04:40:01.617223', '2026-02-27 04:40:01.617223', NULL, NULL),
    (8, 3, 76, 'PENDING', '2026-02-27 04:40:09.549441', '2026-02-27 04:40:09.557367', '2026-02-27 04:40:09.557367', NULL, NULL),
       (9, 2, 13, 'IN_PROGRESS', '2026-02-27 04:42:38.567651', '2026-02-27 04:42:38.567651',
        '2026-02-27 04:46:27.969806', NULL, NULL),
    (10, 2, 32, 'PENDING', '2026-02-27 04:42:41.91097', '2026-02-27 04:42:41.918441', '2026-02-27 04:42:41.918441', NULL, NULL),
       (11, 2, 38, 'REQUESTED_CONFIRM', '2026-02-27 04:42:43.665596', '2026-02-27 04:42:43.665596',
        '2026-02-27 04:46:27.984245', NULL, NULL),
    (12, 2, 75, 'PENDING', '2026-02-27 04:42:52.891707', '2026-02-27 04:42:52.89553', '2026-02-27 04:42:52.89553', NULL, NULL),
    (13, 2, 58, 'PENDING', '2026-02-27 04:42:58.126237', '2026-02-27 04:42:58.127218', '2026-02-27 04:42:58.127218', NULL, NULL),
    (14, 2, 51, 'PENDING', '2026-02-27 04:43:03.599998', '2026-02-27 04:43:03.603226', '2026-02-27 04:43:03.603226', NULL, NULL),
    (15, 2, 78, 'PENDING', '2026-02-27 04:43:10.611364', '2026-02-27 04:43:10.613853', '2026-02-27 04:43:10.613853', NULL, NULL),
    (16, 2, 76, 'PENDING', '2026-02-27 04:43:12.207661', '2026-02-27 04:43:12.20892', '2026-02-27 04:43:12.20892', NULL, NULL),
    (17, 3, 11, 'PENDING', '2026-02-27 04:44:44.169043', '2026-02-27 04:44:44.188167', '2026-02-27 04:44:44.188167', NULL, NULL),
    (18, 3, 96, 'PENDING', '2026-02-27 04:44:57.019013', '2026-02-27 04:44:57.026863', '2026-02-27 04:44:57.026863', NULL, NULL),
    (19, 3, 97, 'PENDING', '2026-02-27 04:45:00.677498', '2026-02-27 04:45:00.688314', '2026-02-27 04:45:00.688314', NULL, NULL),
    (20, 3, 101, 'PENDING', '2026-02-27 04:45:01.140464', '2026-02-27 04:45:01.146291', '2026-02-27 04:45:01.146291', NULL, NULL),
    (21, 3, 98, 'PENDING', '2026-02-27 04:45:01.145349', '2026-02-27 04:45:01.150481', '2026-02-27 04:45:01.150481', NULL, NULL),
    (22, 3, 68, 'PENDING', '2026-02-27 04:45:09.424229', '2026-02-27 04:45:09.427839', '2026-02-27 04:45:09.427839', NULL, NULL),
    (23, 3, 73, 'PENDING', '2026-02-27 04:45:10.486583', '2026-02-27 04:45:10.487981', '2026-02-27 04:45:10.487981', NULL, NULL),
       (24, 3, 74, 'REQUESTED_CONFIRM', '2026-02-27 04:45:11.92899', '2026-02-27 04:45:11.92899',
        '2026-02-27 05:02:18.616474', NULL, NULL);

ALTER TABLE wishlist_items
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 9. CART_ITEM (장바구니 아이템) — V1.2.2
-- -----------------------------------------------------------------------------
INSERT INTO cart_items (id, cart_id, target_type, target_id, amount, wishlist_item_status)
VALUES (1, 2, 'FUNDING_PENDING', 1, 359000.00, 'PENDING')
       ;

ALTER TABLE cart_items
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 10. FUNDING (펀딩) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO fundings (
    id, version, wishlist_item_id, product_id, product_name, image_key, receiver_id,
    target_amount, current_amount, status,
    deadline, achieved_at, closed_at,
    created_at, updated_at, created_by, updated_by
)
VALUES
    (2, 1, 11, 38, '킨토 데이오프 텀블러', 'products/38/kinto-tumbler.jpg', 2,
     45000, 45000, 'ACHIEVED','2026-03-14 04:46:27.808092','2026-02-27 04:46:27.853178',
     '2026-02-27 04:46:27.809499','2026-02-27 04:46:27.809499','2026-02-27 04:46:27.809499',NULL, NULL),

    (3, 2, 1, 17, '벨킨 3-in-1 맥세이프 충전기', 'products/17/belkin-3in1.jpg', 2,
     179000, 179000, 'ACCEPTED',
     '2026-03-14 04:46:27.816602',
     '2026-02-27 04:46:27.881507',
     '2026-02-27 04:46:27.816600',
     '2026-02-27 04:46:27.816600',
     '2026-02-27 04:46:27.816600',
     NULL, NULL),

    (4, 1, 9, 13, '애플워치 시리즈 9', 'products/13/apple-watch.jpg', 2,
     599000, 59900, 'IN_PROGRESS',
     '2026-03-14 04:46:27.817922',
     NULL,
     NULL,
     '2026-02-27 04:46:27.817900',
     '2026-02-27 04:46:27.817900',
     NULL, NULL),

    (5, 1, 24, 74, '모나미 153 ID 볼펜', 'products/74/monami-153id.jpg', 3,
     25000, 25000, 'ACHIEVED',
     '2026-03-14 04:46:27.817922',
     '2026-02-27 04:46:27.817900',
     NULL,
     '2026-02-27 04:46:27.800000',
     '2026-02-27 04:46:27.817000',
     NULL, NULL);

ALTER TABLE fundings
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 11. FUNDING_PARTICIPANT_MEMBER (펀딩 참여자) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO funding_participant_members (id, funding_id, participant_id, nick_name, amount,
                                        created_at, updated_at, created_by, updated_by)
VALUES
       (2,2,3,'멍청한고양이2013',45000, '2026-02-27 04:46:27.8179', '2026-02-27 04:46:27.817', null, null),
       (3,3,3,'멍청한고양이2013',179000, '2026-02-27 04:46:27.8179', '2026-02-27 04:46:27.81', null, null),
       (4,4,3,'멍청한고양이2013',59900, '2026-02-27 04:46:27.8179', '2026-02-27 04:46:27.12', null, null),
       (5,5,2, '나른한고양이0013',25000, '2026-02-27 04:46:27.8179', '2026-02-27 04:46:27.817', null, null);

ALTER TABLE funding_participant_members
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 12. ORDERS (주문) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO orders (id, buyer_id, order_number, total_amount, quantity, payment_method, status,
                    payment_id, origin_transaction_key, paid_at, confirmed_at, cancelled_at,
                    created_at, updated_at)
VALUES (1, 2, 'ORD-20260205-A1B2C3D4E5F6-20260205170000', 359000.00, 1, 'CARD', 'PAID',
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
VALUES (1, 1, 1, 'GENERAL_PRODUCT', 'NORMAL_ORDER', 3, 2,
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
INSERT INTO payments (id, type, method, order_id, order_number, member_id,
                     origin_amount, paid_amount, refunded_amount, order_items_json, status,
                     payment_key, last_transaction_key, approve_code, paid_at,
                     created_at, updated_at, created_by, updated_by)
VALUES (1, 'FUNDING', 'CARD',
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

ALTER TABLE payments
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 15. PAYMENT_HISTORY (결제 이력) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO payment_histories (id, payment_id, history_key, event_type, occurred_at, metadata,
                             created_at, updated_at, created_by, updated_by)
VALUES (1, 1, 'idem-20260205-0001-created', 'CREATED', '2026-02-05 17:00:00', NULL,
        '2026-02-05 17:00:00', '2026-02-05 17:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 1, 'idem-20260205-0001-paid', 'PAID', '2026-02-05 17:05:00', NULL,
        '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
       (3, 2, 'idem-20260205-0002-created', 'CREATED', '2026-02-05 17:10:00', NULL,
        '2026-02-05 17:10:00', '2026-02-05 17:10:00', 'SYSTEM', 'SYSTEM'),
       (4, 2, 'idem-20260205-0002-paid', 'PAID', '2026-02-05 17:15:00', NULL,
        '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
       (5, 3, 'idem-20260208-0004-paid', 'PAID', '2026-02-08 14:05:00', NULL,
        '2026-02-08 14:05:00', '2026-02-08 14:05:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE payment_histories
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 16. WALLET_HISTORY (지갑 이력) — V1.2.4
-- -----------------------------------------------------------------------------
INSERT INTO wallet_histories (id, wallet_id, transaction_type, amount, balance_after,
                            reference_type, reference_id, occurred_at,
                            created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260205-001', '2026-02-04 10:00:00',
        '2026-02-04 10:00:00', '2026-02-04 10:00:00', 'SYSTEM', 'SYSTEM'),
       (2, 2, 'ORDER_DEDUCT', 359000.00, 141000.00, 'PAYMENT', 'ORD-20260205-A1B2C3D4E5F6-20260205170000',
        '2026-02-05 17:05:00',
        '2026-02-05 17:05:00', '2026-02-05 17:05:00', 'SYSTEM', 'SYSTEM'),
       (3, 2, 'ORDER_DEDUCT', 23000.00, 118000.00, 'PAYMENT', 'ORD-20260205-B2C3D4E5F6G7-20260205171000',
        '2026-02-05 17:15:00',
        '2026-02-05 17:15:00', '2026-02-05 17:15:00', 'SYSTEM', 'SYSTEM'),
       (4, 5, 'CHARGE', 300000.00, 300000.00, 'CHARGE', 'CHG-20260207-001', '2026-02-07 09:00:00',
        '2026-02-07 09:00:00', '2026-02-07 09:00:00', 'SYSTEM', 'SYSTEM'),
       (5, 6, 'CHARGE', 500000.00, 500000.00, 'CHARGE', 'CHG-20260208-001', '2026-02-08 10:00:00',
        '2026-02-08 10:00:00', '2026-02-08 10:00:00', 'SYSTEM', 'SYSTEM');

ALTER TABLE wallet_histories
    ALTER COLUMN id RESTART WITH 100;

-- -----------------------------------------------------------------------------
-- 17. FRIENDSHIPS(소셜) -
-- -----------------------------------------------------------------------------
INSERT INTO friendships (id, requester_id, receiver_id, status, accepted_at,
                         created_at, updated_at, created_by, updated_by)
VALUES (1, 2, 3, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM'),
        (2, 2, 5, 'ACCEPTED', '2026-02-07 12:00:00', '2026-02-07 12:00:00', '2026-02-07 12:00:00', 'SYSTEM', 'SYSTEM');

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
