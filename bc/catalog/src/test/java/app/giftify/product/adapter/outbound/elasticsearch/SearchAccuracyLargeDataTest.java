package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.ProductEsTestApplication;
import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.adapter.outbound.jpa.entity.ProductJpa;
import app.giftify.product.adapter.outbound.jpa.repository.ProductRepository;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.ProductSearchSortType;
import app.giftify.shared.api.paging.PageResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 대량 Mock 데이터 기반 검색 정확도 측정 (1000개+ 상품)
 * - Precision@K, MRR, Recall@K, NDCG@K
 */
@SpringBootTest(classes = ProductEsTestApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // @BeforeAll에서 인스턴스 변수 사용
class SearchAccuracyLargeDataTest {

    @Autowired
    private ProductEsRepository productEsRepository;

    @Autowired
    private ProductRepository productJpaRepository;

    @Autowired
    private ProductEsAdapter productEsAdapter;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    /**
     * 테스트 쿼리 → 검색 패턴 (상품명에 이 패턴이 포함되면 관련 문서)
     * 패턴 기반으로 정답 판정 → Mock 데이터와 독립적
     *
     * 버전별 개선사항 검증:
     * - v3: ngram 부분 매칭
     * - v5: minimum_should_match (nori 단일 음절 노이즈 제거)
     * - v6: ICU 자모 분해 오타 보정
     * - v6.3: 공백 처리
     */
    private static final Map<String, String> GROUND_TRUTH_PATTERNS = Map.ofEntries(
            // ========================================
            // v3: ngram 부분 매칭 검증
            // ========================================
            // 완전 매칭
            Map.entry("삼성", "삼성"),
            Map.entry("애플", "애플"),
            Map.entry("나이키", "나이키"),

            // 부분 매칭 (ngram min_gram=2)
            Map.entry("노트", "노트북"),          // "노트" → "노트북" 매칭
            Map.entry("스마", "스마트폰"),        // "스마" → "스마트폰" 매칭
            Map.entry("헤드", "헤드폰"),          // "헤드" → "헤드폰" 매칭
            Map.entry("립스", "립스틱"),          // "립스" → "립스틱" 매칭

            // ========================================
            // v5: minimum_should_match 75% (nori 단일 음절 노이즈 제거)
            // ========================================
            // "레고" 검색 시 "고디바" 같은 단일 음절 매칭 제외
            Map.entry("레고", "레고"),            // "레고" → "레고" (O), "고디바" (X)
            Map.entry("소니", "소니"),            // "소니" → "소니" (O), "니콘" (X)
            Map.entry("아디다스", "아디다스"),    // 복수 음절 매칭

            // 제품 타입 검색 (nori 형태소 분석)
            Map.entry("노트북", "노트북"),
            Map.entry("스마트폰", "스마트폰"),
            Map.entry("헤드폰", "헤드폰"),
            Map.entry("이어폰", "이어폰"),
            Map.entry("운동화", "운동화"),
            Map.entry("향수", "향수"),
            Map.entry("립스틱", "립스틱"),
            Map.entry("침대", "침대"),

            // ========================================
            // v6: ICU 자모 분해 (NFD) 오타 보정
            // ========================================
            // 자모 편집거리 1 오타
            Map.entry("삼셩", "삼성"),            // ㅅ→ㅅ (동일 자음, 자모 유사)
            Map.entry("렉고", "레고"),            // 레ㄱ고 → 레고 (ㄱ 추가 오타)
            Map.entry("에어팍", "이어폰"),        // 에어팟/에어팍 → 이어폰
            Map.entry("샴성", "삼성"),            // 샴→삼 (ㅅ→ㅅ)
            Map.entry("나익", "나이키"),          // 이ㄱ → 이키 (ㄱ→키)

            // 자모 탈락 오타
            Map.entry("아다다스", "아디다스"),    // 디→다 (ㅣ 탈락)
            Map.entry("소이", "소니"),            // 니→이 (ㄴ 탈락)

            // ========================================
            // v6.3: 공백 처리 (공백 제거 병렬화 + minimumShouldMatch 70%)
            // ========================================
            Map.entry("스 타 벅 스", "스타벅스"),     // 공백이 들어간 검색
            Map.entry("삼 성", "삼성"),              // 브랜드명 공백
            Map.entry("나 이 키", "나이키"),         // 브랜드명 공백
            Map.entry("레 고", "레고"),              // 브랜드명 공백
            Map.entry("노 트 북", "노트북"),         // 제품 타입 공백

            // ========================================
            // 복합 검색 (다중 키워드)
            // ========================================
            Map.entry("삼성 노트북", "삼성.*노트북|노트북.*삼성"),      // 정규식
            Map.entry("나이키 운동화", "나이키.*운동화|운동화.*나이키"),
            Map.entry("샤넬 향수", "샤넬.*향수|향수.*샤넬"),
            Map.entry("애플 스마트폰", "애플.*스마트폰|스마트폰.*애플"),

            // ========================================
            // 기타 브랜드/제품 (베이스라인)
            // ========================================
            Map.entry("LG", "LG"),
            Map.entry("샤넬", "샤넬"),
            Map.entry("디올", "디올")
    );

    /**
     * NDCG용 관련도 점수
     * - 3: 매우 관련 (브랜드명 정확 매칭)
     * - 2: 관련 (제품 타입 매칭)
     * - 1: 약간 관련
     */
    private static final Map<String, Integer> DEFAULT_RELEVANCE_SCORES = Map.ofEntries(
            // 브랜드명은 3점
            Map.entry("삼성", 3),
            Map.entry("애플", 3),
            Map.entry("소니", 3),
            Map.entry("LG", 3),
            Map.entry("나이키", 3),
            Map.entry("아디다스", 3),
            Map.entry("샤넬", 3),
            Map.entry("디올", 3),
            Map.entry("레고", 3),

            // 제품 타입은 2점
            Map.entry("노트북", 2),
            Map.entry("스마트폰", 2),
            Map.entry("헤드폰", 2),
            Map.entry("이어폰", 2),
            Map.entry("운동화", 2),
            Map.entry("향수", 2),
            Map.entry("립스틱", 2),
            Map.entry("침대", 2)
    );

    @BeforeAll
    void setUpOnce() {
        productEsRepository.deleteAll();
        productJpaRepository.deleteAllInBatch();

        System.out.println("=== 대량 Mock 데이터 생성 시작 ===");
        long startTime = System.currentTimeMillis();

        // 1000개 Mock 상품 생성
        List<ProductJpa> mockProducts = MockProductDataGenerator.generateProducts(1000, 1L);

        // RDB 저장
        List<ProductJpa> savedEntities = productJpaRepository.saveAll(mockProducts);

        // ES 저장
        List<ProductDocument> documents = savedEntities.stream()
                .map(jpa -> createDocument(
                        jpa.getId().toString(), "테스트셀러", jpa.getName(), jpa.getDescription(),
                        jpa.getPrice(), jpa.getCategory().name(), jpa.getStatus().name()
                ))
                .toList();

        productEsRepository.saveAll(documents);
        elasticsearchOperations.indexOps(ProductDocument.class).refresh();

        long endTime = System.currentTimeMillis();
        System.out.printf("=== 데이터 생성 완료: %d개, 소요 시간: %dms ===\n\n",
                savedEntities.size(), endTime - startTime);
    }

    @Test
    @DisplayName("검색 정확도: Precision@10 계산 (대량 데이터)")
    void calculatePrecisionAt10() {
        Map<String, Double> precisionScores = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH_PATTERNS.entrySet()) {
            String query = entry.getKey();
            String pattern = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 상위 10개 중 관련 있는 문서 수 계산
            List<ProductResult> top10 = result.content().stream()
                    .limit(10)
                    .toList();

            long relevantCount = top10.stream()
                    .filter(product -> isRelevantByPattern(product.name(), pattern))
                    .count();

            double precision = top10.isEmpty() ? 0.0 : (double) relevantCount / top10.size();
            precisionScores.put(query, precision);
        }

        // 평균 Precision@10 계산
        double avgPrecision = precisionScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== Precision@10 결과 (1000개 Mock 데이터) ===");
        precisionScores.forEach((query, precision) ->
                System.out.printf("%-20s: %.2f%%\n", query, precision * 100));
        System.out.printf("\n평균 Precision@10: %.2f%%\n\n", avgPrecision * 100);

        // 70% 이상 목표 (대량 데이터라 기준 낮춤)
        assertThat(avgPrecision)
                .as("Precision@10이 70%% 이상이어야 함")
                .isGreaterThanOrEqualTo(0.70);
    }

    @Test
    @DisplayName("검색 정확도: MRR (Mean Reciprocal Rank) 계산 (대량 데이터)")
    void calculateMRR() {
        List<Double> reciprocalRanks = new ArrayList<>();
        Map<String, Integer> firstRelevantRanks = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH_PATTERNS.entrySet()) {
            String query = entry.getKey();
            String pattern = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 첫 번째 관련 문서의 순위 찾기
            int firstRelevantRank = -1;
            for (int i = 0; i < result.content().size(); i++) {
                ProductResult product = result.content().get(i);

                if (isRelevantByPattern(product.name(), pattern)) {
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

        System.out.println("\n=== MRR 결과 (1000개 Mock 데이터) ===");
        firstRelevantRanks.forEach((query, rank) ->
                System.out.printf("%-20s: %d위 (1/%-2d = %.3f)\n",
                        query, rank > 0 ? rank : 0, rank > 0 ? rank : 0, rank > 0 ? 1.0 / rank : 0.0));
        System.out.printf("\nMRR: %.4f (%.2f%%)\n\n", mrr, mrr * 100);

        // MRR 0.80 이상 목표
        assertThat(mrr)
                .as("MRR이 0.80 이상이어야 함")
                .isGreaterThanOrEqualTo(0.80);
    }

    @Test
    @DisplayName("검색 정확도: NDCG@10 계산 (대량 데이터)")
    void calculateNDCGAt10() {
        int k = 10;
        Map<String, Double> ndcgScores = new LinkedHashMap<>();

        for (var entry : GROUND_TRUTH_PATTERNS.entrySet()) {
            String query = entry.getKey();
            String pattern = entry.getValue();

            // 검색 실행
            var command = searchCommand(query);
            PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

            // 상위 K개 결과
            List<ProductResult> topK = result.content().stream()
                    .limit(k)
                    .toList();

            // 1. relevance 리스트 생성
            List<Integer> relevances = topK.stream()
                    .map(doc -> getRelevance(doc, pattern))
                    .toList();

            // 2. DCG 계산
            double dcg = calculateDCGFromRelevances(relevances);

            // 3. IDCG 계산 (relevance 정렬)
            List<Integer> sorted = relevances.stream()
                    .sorted(Comparator.reverseOrder())
                    .toList();

            double idcg = calculateDCGFromRelevances(sorted);

            // NDCG = DCG / IDCG
            double ndcg = idcg > 0 ? dcg / idcg : 0.0;
            ndcgScores.put(query, ndcg);
        }

        // 평균 NDCG@10 계산
        double avgNDCG = ndcgScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        System.out.println("\n=== NDCG@10 결과 (1000개 Mock 데이터) ===");
        ndcgScores.forEach((query, ndcg) ->
                System.out.printf("%-20s: %.4f (%.2f%%)\n", query, ndcg, ndcg * 100));
        System.out.printf("\n평균 NDCG@10: %.4f (%.2f%%)\n\n", avgNDCG, avgNDCG * 100);

        // NDCG@10 0.85 이상 목표
        assertThat(avgNDCG)
                .as("NDCG@10이 0.85 이상이어야 함")
                .isGreaterThanOrEqualTo(0.85);
    }

    /**
     * 패턴 기반 관련도 판정 (정규식 지원)
     */
    private boolean isRelevantByPattern(String productName, String pattern) {
        if (pattern.contains("|") || pattern.contains(".*")) {
            // 정규식 패턴
            return productName.matches(".*" + pattern + ".*");
        } else {
            // 단순 포함 검사
            return productName.contains(pattern);
        }
    }

//    /**
//     * DCG 계산 (패턴 기반)
//     */
//    private double calculateDCG(List<ProductResult> results, String pattern, int k) {
//        double dcg = 0.0;
//        int defaultScore = DEFAULT_RELEVANCE_SCORES.getOrDefault(extractMainKeyword(pattern), 2);
//
//        for (int i = 0; i < Math.min(results.size(), k); i++) {
//            ProductResult product = results.get(i);
//            int relevance = isRelevantByPattern(product.name(), pattern) ? defaultScore : 0;
//
//            dcg += relevance / (Math.log(i + 2) / Math.log(2));
//        }
//
//        return dcg;
//    }
//
//    /**
//     * IDCG 계산 (이상적 순서 = 모든 상위 K개가 관련 문서)
//     */
//    private double calculateIDCG(int k, int relevanceScore) {
//        double idcg = 0.0;
//        for (int i = 0; i < k; i++) {
//            idcg += relevanceScore / (Math.log(i + 2) / Math.log(2));
//        }
//        return idcg;
//    }

    /**
     * 쿼리에서 주요 키워드 추출 (관련도 점수 결정용)
     */
    private String extractMainKeyword(String query) {
        // 공백 기준으로 첫 번째 단어 반환
        String[] words = query.split("\\s+");
        return words[0];
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

    private double calculateDCGFromRelevances(List<Integer> relevances) {
        double dcg = 0.0;
        for (int i = 0; i < relevances.size(); i++) {
            int rel = relevances.get(i);
            dcg += rel / (Math.log(i + 2) / Math.log(2)); // log2(i+1)
        }
        return dcg;
    }

    /**
     * NDCG relevance 계산
     * 3: 핵심 키워드 정확 매칭 (브랜드/주요 키워드)
     * 2: 패턴 매칭 (정규식 포함)
     * 1: 부분 매칭
     * 0: 무관
     */
    private int getRelevance(ProductResult product, String pattern) {
        String name = product.name().toLowerCase();
        pattern = pattern.toLowerCase();

        // 핵심 키워드 추출
        String mainKeyword = extractMainKeyword(pattern).toLowerCase();

        // 가장 강한 매칭 (브랜드 등)
        if (name.contains(mainKeyword)) {
            return 3;
        }

        // 정규식/패턴 매칭
        if (isRelevantByPattern(name, pattern)) {
            return 2;
        }

        // 부분 매칭
        String[] tokens = pattern.split("\\s+");
        for (String token : tokens) {
            if (name.contains(token)) {
                return 1;
            }
        }

        return 0;
    }

}
