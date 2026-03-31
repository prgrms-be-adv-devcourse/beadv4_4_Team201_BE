package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductStatus;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * 대량의 Mock 상품 데이터 생성기
 * - 실제 브랜드명과 제품 타입을 조합하여 검색 가능한 데이터 생성
 * - 1000개+ 상품 생성으로 정확도 측정 신뢰도 향상
 */
public class MockProductDataGenerator {

    private static final Faker faker = new Faker(new Locale("ko"));
    private static final Random random = new Random(42);  // 재현 가능한 시드

    // 실제 브랜드명 (검색 테스트용)
    private static final String[] ELECTRONICS_BRANDS = {
            "삼성", "애플", "소니", "LG", "샤오미", "화웨이", "HP", "델", "레노버", "에이수스",
            "로지텍", "레이저", "앤커", "벨킨", "필립스", "보스", "JBL", "젠하이저"
    };

    private static final String[] FASHION_BRANDS = {
            "나이키", "아디다스", "퓨마", "뉴발란스", "반스", "컨버스", "구찌", "프라다",
            "자라", "유니클로", "H&M", "GAP", "폴로", "타미힐피거", "캘빈클라인", "리바이스"
    };

    private static final String[] BEAUTY_BRANDS = {
            "샤넬", "디올", "랑콤", "에스티로더", "클리오", "에뛰드", "이니스프리", "설화수",
            "입생로랑", "맥", "키엘", "록시땅", "러쉬", "바디샵", "메이블린", "로레알"
    };

    private static final String[] HOME_BRANDS = {
            "이케아", "한샘", "까사미아", "일룸", "에이스침대", "시몬스", "템퍼", "에어비앤비",
            "조 말론", "딥티크", "얀케캔들", "르크루제", "발뮤다", "브레빌", "쿠진아트", "키친에이드", "스타벅스"
    };

    private static final String[] ELECTRONICS_TYPES = {
            "스마트폰", "노트북", "태블릿", "헤드폰", "이어폰", "스피커", "모니터", "키보드",
            "마우스", "웹캠", "충전기", "보조배터리", "스마트워치", "무선충전기", "USB허브"
    };

    private static final String[] FASHION_TYPES = {
            "운동화", "슬리퍼", "부츠", "샌들", "티셔츠", "후드티", "자켓", "코트",
            "청바지", "반바지", "원피스", "가방", "백팩", "지갑", "벨트", "모자"
    };

    private static final String[] BEAUTY_TYPES = {
            "향수", "립스틱", "쿠션", "선크림", "에센스", "크림", "세럼", "클렌징폼",
            "토너", "스킨", "로션", "마스크팩", "아이크림", "립밤", "핸드크림", "바디로션"
    };

    private static final String[] HOME_TYPES = {
            "침대", "소파", "책상", "의자", "조명", "거울", "시계", "화분",
            "캔들", "디퓨저", "쿠션", "이불", "베개", "커튼", "러그", "수납함"
    };

    /**
     * 지정된 개수만큼 Mock 상품 생성
     *
     * @param count    생성할 상품 개수
     * @param sellerId 판매자 ID
     * @return 생성된 상품 리스트
     */
    public static List<ProductJpa> generateProducts(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();

        // ========================================
        // 1단계: GROUND_TRUTH_PATTERNS 필수 제품 먼저 생성 (보장)
        // ========================================
        products.addAll(generateEssentialProducts(sellerId));

        // ========================================
        // 2단계: 나머지 카테고리별 비율로 랜덤 생성
        // ========================================
        int remaining = count - products.size();

        // 카테고리별 비율 (총합 100%)
        int electronicsCount = (int) (remaining * 0.30);  // 30%
        int fashionCount = (int) (remaining * 0.25);      // 25%
        int beautyCount = (int) (remaining * 0.20);       // 20%
        int homeCount = (int) (remaining * 0.15);         // 15%
        int toysCount = (int) (remaining * 0.10);         // 10%

        // ELECTRONICS
        products.addAll(generateElectronics(electronicsCount, sellerId));

        // FASHION
        products.addAll(generateFashion(fashionCount, sellerId));

        // BEAUTY
        products.addAll(generateBeauty(beautyCount, sellerId));

        // LIVING (HOME)
        products.addAll(generateHome(homeCount, sellerId));

        // TOYS
        products.addAll(generateToys(toysCount, sellerId));

        // 나머지는 랜덤 카테고리로 채우기
        int finalRemaining = count - products.size();
        for (int i = 0; i < finalRemaining; i++) {
            products.add(generateRandomProduct(sellerId));
        }

        return products;
    }

    /**
     * GROUND_TRUTH_PATTERNS에서 사용되는 필수 브랜드-제품타입 조합을 먼저 생성하여 보장
     * - v3: ngram 부분 매칭 (삼성, 애플, 소니, LG, 나이키, 샤넬, 디올, 레고 등)
     * - v5: minimum_should_match (레고 등)
     * - v6: ICU 자모 분해 오타 보정 (삼성, 나이키, 아디다스, 소니 등)
     * - v6.3: 공백 처리 (스타벅스, 삼성, 나이키, 레고 등)
     */
    private static List<ProductJpa> generateEssentialProducts(Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();

        // GROUND_TRUTH_PATTERNS에서 사용되는 핵심 브랜드-제품타입 조합
        Object[][] essentialCombos = {
                // 전자제품 (브랜드, 타입, 카테고리)
                {"삼성", "노트북", ProductCategory.ELECTRONICS},
                {"삼성", "스마트폰", ProductCategory.ELECTRONICS},
                {"애플", "노트북", ProductCategory.ELECTRONICS},
                {"애플", "스마트폰", ProductCategory.ELECTRONICS},
                {"소니", "헤드폰", ProductCategory.ELECTRONICS},
                {"소니", "이어폰", ProductCategory.ELECTRONICS},
                {"LG", "노트북", ProductCategory.ELECTRONICS},
                {"LG", "스마트폰", ProductCategory.ELECTRONICS},

                // 패션
                {"나이키", "운동화", ProductCategory.FASHION},
                {"아디다스", "운동화", ProductCategory.FASHION},

                // 뷰티
                {"샤넬", "향수", ProductCategory.BEAUTY},
                {"샤넬", "립스틱", ProductCategory.BEAUTY},
                {"디올", "향수", ProductCategory.BEAUTY},
                {"디올", "립스틱", ProductCategory.BEAUTY},

                // 장난감
                {"레고", "블록", ProductCategory.TOYS},

                // 홈 (스타벅스 - v6.3 공백 처리 테스트용)
                {"스타벅스", "텀블러", ProductCategory.LIVING},
                {"스타벅스", "머그", ProductCategory.LIVING},

                // 추가 홈 제품 (침대 - GROUND_TRUTH_PATTERNS에 포함)
                {"이케아", "침대", ProductCategory.LIVING},
                {"한샘", "침대", ProductCategory.LIVING}
        };

        // 각 조합당 최소 5개씩 생성 (오타/공백 검증 충분히 확보)
        for (Object[] combo : essentialCombos) {
            for (int i = 0; i < 5; i++) {
                String brand = (String) combo[0];
                String type = (String) combo[1];
                ProductCategory category = (ProductCategory) combo[2];

                String model = generateModelName();
                String name = String.format("%s %s %s", brand, type, model).trim();
                String description = generateDescription(brand, type, category);
                int price = random.nextInt(10000, 1000000);
                int stock = random.nextInt(10, 100);

                products.add(createProduct(name, description, price, stock, category, sellerId));
            }
        }

        return products;
    }

    /**
     * 카테고리별 설명 생성 (검색 테스트용 긴 텍스트)
     */
    private static String generateDescription(String brand, String type, ProductCategory category) {
        switch (category) {
            case ELECTRONICS:
                return String.format("최신 기술이 적용된 %s %s. 뛰어난 성능과 디자인을 자랑합니다. "
                        + "우수한 품질과 내구성으로 오래 사용할 수 있으며, 사용자 친화적인 인터페이스를 제공합니다.", brand, type);
            case FASHION:
                return String.format("편안한 착용감과 세련된 디자인의 %s %s. 일상에서 스타일을 완성합니다. "
                        + "고품질 소재를 사용하여 오래 착용해도 변형이 없으며, 다양한 스타일에 매치하기 좋습니다.", brand, type);
            case BEAUTY:
                return String.format("피부에 영양을 공급하는 %s의 시그니처 %s. 프리미엄 성분으로 피부를 케어합니다. "
                        + "피부 타입에 맞는 맞춤형 케어가 가능하며, 자극 없이 부드럽게 사용할 수 있습니다.", brand, type);
            case LIVING:
                return String.format("일상을 특별하게 만드는 %s의 %s. 실용적이고 세련된 디자인입니다. "
                        + "공간에 조화를 더하며, 높은 활용도로 매일 사용하기 좋습니다.", brand, type);
            case TOYS:
                return String.format("아이들의 상상력을 키워주는 %s %s. 안전한 소재로 제작되었습니다. "
                        + "교육적 가치가 높으며, 연령대에 맞는 난이도로 즐겁게 놀 수 있습니다.", brand, type);
            default:
                return String.format("%s %s 제품입니다.", brand, type);
        }
    }

    private static List<ProductJpa> generateElectronics(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String brand = ELECTRONICS_BRANDS[random.nextInt(ELECTRONICS_BRANDS.length)];
            String type = ELECTRONICS_TYPES[random.nextInt(ELECTRONICS_TYPES.length)];
            String model = generateModelName();

            String name = String.format("%s %s %s", brand, type, model);
            String description = generateDescription(brand, type, ProductCategory.ELECTRONICS);
            int price = random.nextInt(50000, 2000000);
            int stock = random.nextInt(0, 100);

            products.add(createProduct(name, description, price, stock, ProductCategory.ELECTRONICS, sellerId));
        }
        return products;
    }

    private static List<ProductJpa> generateFashion(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String brand = FASHION_BRANDS[random.nextInt(FASHION_BRANDS.length)];
            String type = FASHION_TYPES[random.nextInt(FASHION_TYPES.length)];
            String model = generateModelName();

            String name = String.format("%s %s %s", brand, type, model);
            String description = generateDescription(brand, type, ProductCategory.FASHION);
            int price = random.nextInt(30000, 500000);
            int stock = random.nextInt(0, 100);

            products.add(createProduct(name, description, price, stock, ProductCategory.FASHION, sellerId));
        }
        return products;
    }

    private static List<ProductJpa> generateBeauty(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String brand = BEAUTY_BRANDS[random.nextInt(BEAUTY_BRANDS.length)];
            String type = BEAUTY_TYPES[random.nextInt(BEAUTY_TYPES.length)];

            String name = String.format("%s %s", brand, type);
            String description = generateDescription(brand, type, ProductCategory.BEAUTY);
            int price = random.nextInt(20000, 300000);
            int stock = random.nextInt(0, 100);

            products.add(createProduct(name, description, price, stock, ProductCategory.BEAUTY, sellerId));
        }
        return products;
    }

    private static List<ProductJpa> generateHome(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String brand = HOME_BRANDS[random.nextInt(HOME_BRANDS.length)];
            String type = HOME_TYPES[random.nextInt(HOME_TYPES.length)];

            String name = String.format("%s %s", brand, type);
            String description = generateDescription(brand, type, ProductCategory.LIVING);
            int price = random.nextInt(10000, 3000000);
            int stock = random.nextInt(0, 100);

            products.add(createProduct(name, description, price, stock, ProductCategory.LIVING, sellerId));
        }
        return products;
    }

    private static List<ProductJpa> generateToys(int count, Long sellerId) {
        List<ProductJpa> products = new ArrayList<>();
        String[] toyBrands = {"레고", "반다이", "타카라토미", "해즈브로", "마텔", "젤리캣", "실바니안"};
        String[] toyTypes = {"블록", "피규어", "보드게임", "인형", "퍼즐", "카드게임", "미니카", "로봇"};

        for (int i = 0; i < count; i++) {
            String brand = toyBrands[random.nextInt(toyBrands.length)];
            String type = toyTypes[random.nextInt(toyTypes.length)];
            String model = generateModelName();

            String name = String.format("%s %s %s", brand, type, model);
            String description = generateDescription(brand, type, ProductCategory.TOYS);
            int price = random.nextInt(10000, 500000);
            int stock = random.nextInt(0, 100);

            products.add(createProduct(name, description, price, stock, ProductCategory.TOYS, sellerId));
        }
        return products;
    }

    private static ProductJpa generateRandomProduct(Long sellerId) {
        ProductCategory[] categories = ProductCategory.values();
        ProductCategory category = categories[random.nextInt(categories.length)];

        String name = faker.commerce().productName();
        String description = faker.lorem().sentence(10);
        int price = random.nextInt(10000, 1000000);
        int stock = random.nextInt(0, 100);

        return createProduct(name, description, price, stock, category, sellerId);
    }

    private static String generateModelName() {
        String[] prefixes = {"프로", "플러스", "에어", "울트라", "맥스", "라이트", "미니", "슬림"};
        String[] suffixes = {"2024", "2025", "Gen2", "SE", "X", "Pro", ""};

        String prefix = random.nextBoolean() ? prefixes[random.nextInt(prefixes.length)] + " " : "";
        String suffix = random.nextBoolean() ? " " + suffixes[random.nextInt(suffixes.length)] : "";

        return prefix + suffix;
    }

    private static ProductJpa createProduct(String name, String description, int price, int stock,
                                            ProductCategory category, Long sellerId) {
        return ProductJpa.builder()
                .sellerId(sellerId)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .status(ProductStatus.ACTIVE)
                .category(category)
                .imageKey("mock-image.jpg")
                .build();
    }
}
