package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.ProductEsTestApplication;
import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductSearchSortType;
import app.giftify.product.domain.ProductStatus;
import app.giftify.shared.api.paging.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 검색 정확도 정량 측정 테스트
 * - Precision@K: 상위 K개 결과 중 관련 문서 비율
 * - MRR (Mean Reciprocal Rank): 첫 번째 정답 순위의 역수 평균
 * - NDCG@K (Normalized Discounted Cumulative Gain): 순위를 고려한 관련도 평가
 */
@SpringBootTest(classes = ProductEsTestApplication.class)
class SearchAccuracyTest {

    @Autowired
    private ProductEsRepository productEsRepository;

    @Autowired
    private ProductRepository productJpaRepository;

    @Autowired
    private ProductEsAdapter productEsAdapter;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    // 테스트 쿼리 → 정답 상품명 매핑 (이진 관련도: 관련 있음/없음)
    private static final Map<String, List<String>> GROUND_TRUTH = Map.ofEntries(
            // 브랜드명 정확 검색
            Map.entry("소니", List.of("소니 WH-1000XM5")),
            Map.entry("애플워치", List.of("애플워치 시리즈 9")),
            Map.entry("삼성", List.of("삼성 갤럭시 S24 울트라")),
            Map.entry("로지텍", List.of("로지텍 MX Master 3S")),

            // 레고 검색
            Map.entry("레고", List.of("레고 테크닉 페라리 Daytona", "레고 클래식 미디움 조립 박스")),
            Map.entry("렉고", List.of("레고 테크닉 페라리 Daytona", "레고 클래식 미디움 조립 박스")),  // 오타

            // 다이슨 검색
            Map.entry("다이슨", List.of("다이슨 에어랩")),
            Map.entry("다이슨 에어랩", List.of("다이슨 에어랩")),

            // 뷰티 브랜드
            Map.entry("샤넬", List.of("샤넬 가브리엘 향수")),
            Map.entry("디올", List.of("디올 어딕트 립 글로우")),
            Map.entry("설화수", List.of("설화수 자음 2종 세트")),

            // 커피/주방 기기
            Map.entry("커피머신", List.of("드롱기 데디카 커피머신", "네스프레소 버츄오 팝")),
            Map.entry("드롱기", List.of("드롱기 데디카 커피머신")),
            Map.entry("네스프레소", List.of("네스프레소 버츄오 팝")),
            Map.entry("발뮤다", List.of("발뮤다 더 팟")),

            // 캠핑/아웃도어
            Map.entry("헬리녹스", List.of("헬리녹스 체어원 블랙")),
            Map.entry("스노우피크", List.of("스노우피크 티타늄 컵 450")),
            Map.entry("노르디스크", List.of("노르디스크 이순 텐트")),
            Map.entry("캠핑 의자", List.of("헬리녹스 체어원 블랙")),

            // 패션 브랜드
            Map.entry("나이키", List.of("나이키 에어맥스 97")),
            Map.entry("파타고니아", List.of("파타고니아 레트로 X 자켓")),
            Map.entry("아크테릭스", List.of("아크테릭스 맨티스 26")),

            // 향수 검색
            Map.entry("향수", List.of("샤넬 가브리엘 향수", "르 라보 상탈 33")),
            Map.entry("르 라보", List.of("르 라보 상탈 33")),

            // 장난감/취미
            Map.entry("포켓몬", List.of("포켓몬 카드 스페셜 세트")),
            Map.entry("실바니안", List.of("실바니안 패밀리 타운하우스")),
            Map.entry("보드게임", List.of("보드게임 카탄")),
            Map.entry("건담", List.of("건담 PG 언리쉬드 RX-78-2")),

            // 오타 보정 테스트
            Map.entry("에어팍스", List.of("나이키 에어맥스 97")),  // 오타

            // 공백 처리 테스트
            Map.entry("스 타 벅 스", List.of("스타벅스 텀블러")),
            Map.entry("스타벅스", List.of("스타벅스 텀블러")),

            // 애플 제품군
            Map.entry("맥북", List.of("맥북 에어 M3")),
            Map.entry("아이패드", List.of("아이패드 에어 6세대"))
    );

    /**
     * NDCG 계산용 관련도 점수 (쿼리 → 상품명 → 관련도 점수)
     * 3: 매우 관련 (정확 매칭)
     * 2: 관련 (의미상 유사)
     * 1: 약간 관련
     * 0: 무관
     */
    private static final Map<String, Map<String, Integer>> GRADED_RELEVANCE = Map.ofEntries(
            // 브랜드명 정확 검색 (3점: 매우 관련)
            Map.entry("소니", Map.of("소니 WH-1000XM5", 3)),
            Map.entry("애플워치", Map.of("애플워치 시리즈 9", 3)),
            Map.entry("삼성", Map.of("삼성 갤럭시 S24 울트라", 3)),
            Map.entry("로지텍", Map.of("로지텍 MX Master 3S", 3)),

            // 레고 (두 제품 모두 3점)
            Map.entry("레고", Map.of(
                    "레고 테크닉 페라리 Daytona", 3,
                    "레고 클래식 미디움 조립 박스", 3
            )),
            Map.entry("렉고", Map.of(  // 오타지만 의도 명확
                    "레고 테크닉 페라리 Daytona", 3,
                    "레고 클래식 미디움 조립 박스", 3
            )),

            // 다이슨
            Map.entry("다이슨", Map.of("다이슨 에어랩", 3)),
            Map.entry("다이슨 에어랩", Map.of("다이슨 에어랩", 3)),

            // 뷰티 브랜드
            Map.entry("샤넬", Map.of("샤넬 가브리엘 향수", 3)),
            Map.entry("디올", Map.of("디올 어딕트 립 글로우", 3)),
            Map.entry("설화수", Map.of("설화수 자음 2종 세트", 3)),

            // 커피/주방 (카테고리 검색 - 2점)
            Map.entry("커피머신", Map.of(
                    "드롱기 데디카 커피머신", 3,  // 정확 매칭
                    "네스프레소 버츄오 팝", 3
            )),
            Map.entry("드롱기", Map.of("드롱기 데디카 커피머신", 3)),
            Map.entry("네스프레소", Map.of("네스프레소 버츄오 팝", 3)),
            Map.entry("발뮤다", Map.of("발뮤다 더 팟", 3)),

            // 캠핑/아웃도어
            Map.entry("헬리녹스", Map.of("헬리녹스 체어원 블랙", 3)),
            Map.entry("스노우피크", Map.of("스노우피크 티타늄 컵 450", 3)),
            Map.entry("노르디스크", Map.of("노르디스크 이순 텐트", 3)),
            Map.entry("캠핑 의자", Map.of("헬리녹스 체어원 블랙", 2)),  // 의미 검색

            // 패션
            Map.entry("나이키", Map.of("나이키 에어맥스 97", 3)),
            Map.entry("파타고니아", Map.of("파타고니아 레트로 X 자켓", 3)),
            Map.entry("아크테릭스", Map.of("아크테릭스 맨티스 26", 3)),

            // 향수
            Map.entry("향수", Map.of(
                    "샤넬 가브리엘 향수", 3,
                    "르 라보 상탈 33", 2  // 상탈 향수는 약간 낮은 점수
            )),
            Map.entry("르 라보", Map.of("르 라보 상탈 33", 3)),

            // 장난감
            Map.entry("포켓몬", Map.of("포켓몬 카드 스페셜 세트", 3)),
            Map.entry("실바니안", Map.of("실바니안 패밀리 타운하우스", 3)),
            Map.entry("보드게임", Map.of("보드게임 카탄", 3)),
            Map.entry("건담", Map.of("건담 PG 언리쉬드 RX-78-2", 3)),

            // 오타
            Map.entry("에어팍스", Map.of("나이키 에어맥스 97", 3)),

            // 공백
            Map.entry("스 타 벅 스", Map.of("스타벅스 텀블러", 3)),
            Map.entry("스타벅스", Map.of("스타벅스 텀블러", 3)),

            // 애플 제품
            Map.entry("맥북", Map.of("맥북 에어 M3", 3)),
            Map.entry("아이패드", Map.of("아이패드 에어 6세대", 3))
    );

    @BeforeEach
    void setUp() {
        productEsRepository.deleteAll();
        productJpaRepository.deleteAllInBatch();

        // RDB 데이터 setup - data-local.sql에서 ACTIVE 상품만 선택
        List<ProductJpa> savedEntities = productJpaRepository.saveAll(List.of(
                // ELECTRONICS (전자기기) - ACTIVE만
                createJpaEntity("소니 WH-1000XM5", "압도적인 정적, 최고의 노이즈 캔슬링 헤드폰", 449000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("아이패드 에어 6세대", "M2 칩 탑재, 프로의 성능을 담은 에어", 899000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("애플워치 시리즈 9", "가장 앞선 건강 센서와 더 밝아진 디스플레이", 599000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("로지텍 MX Master 3S", "무소음 클릭과 정교한 매그스피드 휠", 139000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("삼성 갤럭시 S24 울트라", "AI로 완성된 새로운 모바일 경험", 1698000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("킨들 페이퍼화이트 5", "종이 책을 읽는 듯한 편안함, 최고의 이북리더", 189000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("고프로 히어로 12", "가장 강력한 흔들림 보정 기능의 액션캠", 558000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("오큘러스 퀘스트 3", "혼합 현실(MR)로 즐기는 새로운 차원의 게이밍", 690000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("플레이스테이션 5 Slim", "더 작아진 크기, 더 강력해진 퍼포먼스", 628000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),
                createJpaEntity("맥북 에어 M3", "애플 노트북", 1590000, 50, ProductCategory.ELECTRONICS, ProductStatus.ACTIVE),

                // LIVING & KITCHEN - ACTIVE만
                createJpaEntity("조 말론 런던 캔들", "잉글리쉬 페어 앤 프리지아 홈 캔들", 115000, 50, ProductCategory.LIVING, ProductStatus.ACTIVE),
                createJpaEntity("킨토 데이오프 텀블러", "지치지 않는 일상을 위한 그립감 좋은 텀블러", 45000, 50, ProductCategory.KITCHEN, ProductStatus.ACTIVE),
                createJpaEntity("드롱기 데디카 커피머신", "홈바리스타를 위한 메탈 바디 에스프레소 머신", 259000, 50, ProductCategory.KITCHEN, ProductStatus.ACTIVE),
                createJpaEntity("로보락 S8 Pro Ultra", "먼지 비움부터 걸레 세척까지 완전 자동화", 1590000, 50, ProductCategory.LIVING, ProductStatus.ACTIVE),
                createJpaEntity("르크루제 무쇠 주물 냄비", "요리의 맛을 살리는 클래식한 프랑스 냄비", 299000, 50, ProductCategory.KITCHEN, ProductStatus.ACTIVE),
                createJpaEntity("발뮤다 더 팟", "커피 드립에 최적화된 우아한 전기 주전자", 199000, 50, ProductCategory.KITCHEN, ProductStatus.ACTIVE),
                createJpaEntity("네스프레소 버츄오 팝", "풍부한 크레마, 버튼 하나로 완성되는 커피", 199000, 50, ProductCategory.KITCHEN, ProductStatus.ACTIVE),
                createJpaEntity("스타벅스 텀블러", "스타벅스 한정판 텀블러", 35000, 50, ProductCategory.LIVING, ProductStatus.ACTIVE),

                // BEAUTY - ACTIVE만
                createJpaEntity("샤넬 가브리엘 향수", "네 가지 화이트 플라워의 눈부신 광채", 242000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("입생로랑 쿠션", "럭셔리한 패키지와 무결점 커버력의 조화", 98000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("에스티로더 갈색병", "밤 사이 깨어나는 피부의 자생 에너지", 182000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("르 라보 상탈 33", "강렬한 중독성의 우디한 스모키 향", 420000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("설화수 자음 2종 세트", "부모님께 드리는 최고의 선물, 촉촉한 영양", 125000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("디올 어딕트 립 글로우", "나만의 피부 톤에 맞춰 발색되는 국민 립밤", 48000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),
                createJpaEntity("다이슨 에어랩", "다이슨 헤어 스타일러", 599000, 50, ProductCategory.BEAUTY, ProductStatus.ACTIVE),

                // TOYS - ACTIVE만
                createJpaEntity("레고 테크닉 페라리 Daytona", "실차와 동일한 정교한 디테일의 하이퍼카", 599000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("레고 클래식 미디움 조립 박스", "레고 블록 장난감", 38000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("포켓몬 카드 스페셜 세트", "희귀한 홀로그램 카드가 포함된 한정판", 120000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("실바니안 패밀리 타운하우스", "꿈꾸던 미니어처 세상, 디럭스 하우스", 159000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("보드게임 카탄", "전 세계에서 가장 사랑받는 전략 보드게임", 45000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("젤리캣 버니 L", "세상에서 가장 부드러운 애착 인형", 58000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),
                createJpaEntity("건담 PG 언리쉬드 RX-78-2", "건프라 기술의 정점을 보여주는 기념비적 모델", 300000, 50, ProductCategory.TOYS, ProductStatus.ACTIVE),

                // OUTDOOR - ACTIVE만
                createJpaEntity("헬리녹스 체어원 블랙", "경량 캠핑 의자의 대명사, 컴팩트한 휴대성", 110000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("스노우피크 티타늄 컵 450", "가장 가벼운 캠핑 필수 아이템", 55000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("노르디스크 이순 텐트", "감성 캠핑의 끝판왕 면 텐트", 1250000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("나이키 에어맥스 97", "클래식한 디자인과 편안한 쿠셔닝의 아이콘", 199000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("룰루레몬 얼라인 팬츠", "버터처럼 부드러운 촉감의 요가 레깅스", 138000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("파타고니아 레트로 X 자켓", "친환경 소재의 따뜻하고 포근한 플리스", 289000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),
                createJpaEntity("아크테릭스 맨티스 26", "등산부터 일상까지 책임지는 멀티 백팩", 215000, 50, ProductCategory.OUTDOOR, ProductStatus.ACTIVE),

                // PET - ACTIVE만
                createJpaEntity("러프웨어 전술 하네스", "어떤 험로도 거뜬한 고성능 아웃도어 하네스", 98000, 50, ProductCategory.PET, ProductStatus.ACTIVE),
                createJpaEntity("펫킷 스마트 정수기", "반려동물의 건강한 수분 섭취를 위한 저소음 정수기", 75000, 50, ProductCategory.PET, ProductStatus.ACTIVE),
                createJpaEntity("숨숨집 캣타워 4단", "인테리어를 해치지 않는 원목 캣타워", 320000, 50, ProductCategory.PET, ProductStatus.ACTIVE),
                createJpaEntity("강아지 프리미엄 영양제", "피부와 피모 개선을 위한 60일분 스틱", 42000, 50, ProductCategory.PET, ProductStatus.ACTIVE),

                // FASHION - ACTIVE만
                createJpaEntity("메종 키츠네 카디건", "여우 로고가 돋보이는 부드러운 램스울 소재", 365000, 50, ProductCategory.FASHION, ProductStatus.ACTIVE),
                createJpaEntity("폴로 랄프로렌 린넨 셔츠", "여름 시즌 베스트셀러, 쾌적하고 클래식한 핏", 199000, 50, ProductCategory.FASHION, ProductStatus.ACTIVE),
                createJpaEntity("젠틀몬스터 릴리트", "어떤 얼굴형에도 어울리는 플랫바 선글라스", 269000, 50, ProductCategory.FASHION, ProductStatus.ACTIVE),
                createJpaEntity("프라이탁 제이미", "버려진 방수포로 만든 세상에 단 하나뿐인 가방", 218000, 50, ProductCategory.FASHION, ProductStatus.ACTIVE)
        ));

        // ES에 도큐먼트 저장 - JPA 자동 생성된 ID로 매핑
        List<ProductDocument> documents = savedEntities.stream()
                .map(jpa -> createDocument(
                        jpa.getId().toString(), "테스트셀러", jpa.getName(), jpa.getDescription(),
                        jpa.getPrice(), jpa.getCategory().name(), jpa.getStatus().name()
                ))
                .toList();

        productEsRepository.saveAll(documents);
        elasticsearchOperations.indexOps(ProductDocument.class).refresh();
    }

    @Test
    @DisplayName("검색 정확도: Precision@10 계산")
    void calculatePrecisionAt10() {
        Map<String, Double> precisionScores = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH.entrySet()) {
            String query = entry.getKey();
            List<String> relevantProducts = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 상위 10개 중 관련 있는 문서 수 계산
            List<ProductResult> top10 = result.content().stream()
                    .limit(10)
                    .toList();

            long relevantCount = top10.stream()
                    .filter(product -> isRelevant(product.name(), relevantProducts))
                    .count();

            double precision = top10.isEmpty() ? 0.0 : (double) relevantCount / top10.size();
            precisionScores.put(query, precision);
        }

        // 평균 Precision@10 계산
        double avgPrecision = precisionScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== Precision@10 결과 ===");
        precisionScores.forEach((query, precision) ->
                System.out.printf("%-20s: %.2f%%\n", query, precision * 100));
        System.out.printf("\n평균 Precision@10: %.2f%%\n\n", avgPrecision * 100);

        // 포트폴리오용: 75% 이상 목표
        assertThat(avgPrecision)
                .as("Precision@10이 75%% 이상이어야 함")
                .isGreaterThanOrEqualTo(0.75);
    }

    @Test
    @DisplayName("검색 정확도: MRR (Mean Reciprocal Rank) 계산")
    void calculateMRR() {
        List<Double> reciprocalRanks = new ArrayList<>();
        Map<String, Integer> firstRelevantRanks = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH.entrySet()) {
            String query = entry.getKey();
            List<String> relevantProducts = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 첫 번째 관련 문서의 순위 찾기
            int firstRelevantRank = -1;
            for (int i = 0; i < result.content().size(); i++) {
                ProductResult product = result.content().get(i);

                if (isRelevant(product.name(), relevantProducts)) {
                    firstRelevantRank = i + 1;  // 1-based ranking
                    break;
                }
            }

            firstRelevantRanks.put(query, firstRelevantRank);

            if (firstRelevantRank > 0) {
                reciprocalRanks.add(1.0 / firstRelevantRank);
            } else {
                reciprocalRanks.add(0.0);  // 관련 문서 없음
            }
        }

        // MRR 계산
        double mrr = reciprocalRanks.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== MRR (Mean Reciprocal Rank) 결과 ===");
        firstRelevantRanks.forEach((query, rank) ->
                System.out.printf("%-20s: %d위 (1/%-2d = %.3f)\n",
                        query, rank > 0 ? rank : 0, rank > 0 ? rank : 0, rank > 0 ? 1.0 / rank : 0.0));
        System.out.printf("\nMRR: %.4f (%.2f%%)\n\n", mrr, mrr * 100);

        // 포트폴리오용: MRR 0.85 이상 목표 (대부분 쿼리에서 1~2위 안에 정답)
        assertThat(mrr)
                .as("MRR이 0.85 이상이어야 함 (대부분 쿼리에서 상위권 정답)")
                .isGreaterThanOrEqualTo(0.85);
    }

    @Test
    @DisplayName("검색 정확도: Recall@10 계산")
    void calculateRecallAt10() {
        Map<String, Double> recallScores = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH.entrySet()) {
            String query = entry.getKey();
            List<String> relevantProducts = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 상위 10개에서 찾은 관련 문서 수
            List<ProductResult> top10 = result.content().stream()
                    .limit(10)
                    .toList();

            long foundRelevantCount = top10.stream()
                    .filter(product -> isRelevant(product.name(), relevantProducts))
                    .count();

            // Recall = 찾은 관련 문서 / 전체 관련 문서
            double recall = (double) foundRelevantCount / relevantProducts.size();
            recallScores.put(query, recall);
        }

        // 평균 Recall@10 계산
        double avgRecall = recallScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== Recall@10 결과 ===");
        recallScores.forEach((query, recall) ->
                System.out.printf("%-20s: %.2f%%\n", query, recall * 100));
        System.out.printf("\n평균 Recall@10: %.2f%%\n\n", avgRecall * 100);

        // 포트폴리오용: Recall@10 80% 이상 목표
        assertThat(avgRecall)
                .as("Recall@10이 80%% 이상이어야 함")
                .isGreaterThanOrEqualTo(0.80);
    }

    @Test
    @DisplayName("검색 정확도: NDCG@10 (Normalized Discounted Cumulative Gain) 계산")
    void calculateNDCGAt10() {
        int k = 10;
        Map<String, Double> ndcgScores = new LinkedHashMap<>();
        Map<String, String> detailResults = new LinkedHashMap<>();

        for (var entry : GRADED_RELEVANCE.entrySet()) {
            String query = entry.getKey();
            Map<String, Integer> relevanceMap = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 상위 K개 결과
            List<ProductResult> topK = result.content().stream()
                    .limit(k)
                    .toList();

            // DCG 계산 (실제 검색 결과 순서)
            double dcg = calculateDCG(topK, relevanceMap, k);

            // IDCG 계산 (이상적인 순서)
            double idcg = calculateIDCG(relevanceMap, k);

            // NDCG = DCG / IDCG
            double ndcg = idcg > 0 ? dcg / idcg : 0.0;
            ndcgScores.put(query, ndcg);

            detailResults.put(query, String.format("DCG=%.3f, IDCG=%.3f", dcg, idcg));
        }

        // 평균 NDCG@10 계산
        double avgNDCG = ndcgScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== NDCG@10 결과 ===");
        ndcgScores.forEach((query, ndcg) ->
                System.out.printf("%-20s: %.4f (%.2f%%) - %s\n",
                        query, ndcg, ndcg * 100, detailResults.get(query)));
        System.out.printf("\n평균 NDCG@10: %.4f (%.2f%%)\n\n", avgNDCG, avgNDCG * 100);

        // 포트폴리오용: NDCG@10 0.90 이상 목표 (관련 문서가 상위권에 배치)
        assertThat(avgNDCG)
                .as("NDCG@10이 0.90 이상이어야 함 (관련 문서가 상위권 배치)")
                .isGreaterThanOrEqualTo(0.90);
    }

    /**
     * DCG (Discounted Cumulative Gain) 계산
     * DCG@K = Σ(rel_i / log2(i+1)) for i=1 to K
     *
     * @param results      검색 결과 (순서대로)
     * @param relevanceMap 상품명 → 관련도 점수 매핑
     * @param k            상위 K개
     * @return DCG 점수
     */
    private double calculateDCG(List<ProductResult> results, Map<String, Integer> relevanceMap, int k) {
        double dcg = 0.0;

        for (int i = 0; i < Math.min(results.size(), k); i++) {
            ProductResult product = results.get(i);
            int relevance = getRelevanceScore(product.name(), relevanceMap);

            // DCG 공식: rel_i / log2(i+1)
            // i는 0-based이므로 i+2를 사용 (1위 = log2(2), 2위 = log2(3), ...)
            dcg += relevance / (Math.log(i + 2) / Math.log(2));
        }

        return dcg;
    }

    /**
     * IDCG (Ideal DCG) 계산
     * 관련도 점수를 내림차순으로 정렬한 이상적인 순서로 DCG 계산
     *
     * @param relevanceMap 상품명 → 관련도 점수 매핑
     * @param k            상위 K개
     * @return IDCG 점수
     */
    private double calculateIDCG(Map<String, Integer> relevanceMap, int k) {
        // 관련도 점수를 내림차순으로 정렬
        List<Integer> sortedRelevance = relevanceMap.values().stream()
                .sorted(Comparator.reverseOrder())
                .limit(k)
                .toList();

        double idcg = 0.0;
        for (int i = 0; i < sortedRelevance.size(); i++) {
            int relevance = sortedRelevance.get(i);
            idcg += relevance / (Math.log(i + 2) / Math.log(2));
        }

        return idcg;
    }

    /**
     * 상품의 관련도 점수 반환
     */
    private int getRelevanceScore(String productName, Map<String, Integer> relevanceMap) {
        return relevanceMap.entrySet().stream()
                .filter(entry -> productName.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0);  // 무관한 문서는 0점
    }

    /**
     * 상품명이 정답 리스트에 포함되는지 확인
     */
    private boolean isRelevant(String productName, List<String> relevantProducts) {
        return relevantProducts.stream()
                .anyMatch(productName::contains);
    }

    private ProductEsSearchCommand searchCommand(String keyword) {
        return new ProductEsSearchCommand(
                keyword, null, null, null, ProductSearchSortType.RELEVANCE, 0, 20
        );
    }

    private ProductDocument createDocument(String id, String sellerNickname, String name,
                                           String description, int price, String category, String status) {
        return new ProductDocument(
                id, sellerNickname, name, description, price,
                category, status, "image-" + id, LocalDateTime.now()
        );
    }

    private ProductJpa createJpaEntity(String name, String description, int price, int stock,
                                       ProductCategory category, ProductStatus status) {
        return ProductJpa.builder()
                .sellerId(1L)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .status(status)
                .category(category)
                .imageKey("image")
                .build();
    }
}
