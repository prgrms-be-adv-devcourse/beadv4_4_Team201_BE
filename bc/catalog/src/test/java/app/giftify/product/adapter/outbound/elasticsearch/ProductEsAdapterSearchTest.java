package app.giftify.product.adapter.outbound.elasticsearch;

import app.giftify.product.ProductEsTestApplication;
import app.giftify.product.adapter.outbound.elasticsearch.document.ProductDocument;
import app.giftify.product.adapter.outbound.elasticsearch.repository.ProductEsRepository;
import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.ProductEsSearchCommand;
import app.giftify.product.domain.ProductSearchSortType;
import app.giftify.shared.api.paging.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ProductEsTestApplication.class)
class ProductEsAdapterSearchTest {

    @Autowired
    private ProductEsRepository productEsRepository;

    @Autowired
    private ProductEsAdapter productEsAdapter;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() throws InterruptedException {
        productEsRepository.deleteAll();

        List<ProductDocument> documents = List.of(
                createDocument("1", "테스트셀러", "에어팟 프로 2세대", "애플 무선 이어폰", 359000, "ELECTRONICS", "ACTIVE"),
                createDocument("2", "테스트셀러", "다이슨 에어랩", "다이슨 헤어 스타일러", 599000, "BEAUTY", "ACTIVE"),
                createDocument("3", "테스트셀러", "레고 클래식 미디움 조립 박스", "레고 블록 장난감", 38000, "TOYS", "ACTIVE"),
                createDocument("4", "테스트셀러", "레고 테크닉 슈퍼카", "레고 자동차 장난감", 62000, "TOYS", "ACTIVE"),
                createDocument("5", "테스트셀러", "맥북 에어 M3", "애플 노트북", 1590000, "ELECTRONICS", "ACTIVE"),
                createDocument("6", "테스트셀러", "스타벅스 텀블러", "스타벅스 한정판 텀블러", 35000, "LIVING", "ACTIVE"),
                createDocument("7", "테스트셀러", "나이키 에어포스 1", "나이키 운동화", 139000, "FASHION", "ACTIVE"),
                createDocument("8", "테스트셀러", "고디바 초콜릿 세트", "벨기에 프리미엄 초콜릿", 45000, "FOODS", "ACTIVE"),
                createDocument("9", "테스트셀러", "닌텐도 스위치 프로 컨트롤러", "닌텐도 게임 컨트롤러", 69000, "ELECTRONICS", "ACTIVE"),
                createDocument("10", "테스트셀러", "강아지 하네스 에어 메쉬", "반려견 산책용 하네스", 25000, "PET", "INACTIVE")
        );

        productEsRepository.saveAll(documents);
        elasticsearchOperations.indexOps(ProductDocument.class).refresh();
    }

    @Test
    @DisplayName("ngram 매칭: '에어팟' 검색 시 에어팟 상품이 조회된다")
    void searchByNgramExactMatch() {
        var command = searchCommand("에어팟");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).name()).contains("에어팟");
    }

    @Test
    @DisplayName("ngram 매칭: '레고' 검색 시 레고 상품만 조회된다")
    void searchByNgramLego() {
        var command = searchCommand("레고");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()).allMatch(p -> p.name().contains("레고"));
    }

    @Test
    @DisplayName("공백 없는 복합 검색어: '에어팟프로' 검색 시 '에어팟 프로 2세대'가 1위로 조회된다")
    void searchWithoutSpaces() {
        var command = searchCommand("에어팟프로");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).name()).contains("에어팟 프로");
    }

    @Test
    @DisplayName("공백 포함 검색어: '스 타 벅 스' 검색 시 스타벅스 상품이 조회된다")
    void searchWithSpaces() {
        var command = searchCommand("스 타 벅 스");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).name()).contains("스타벅스");
    }

    @Test
    @DisplayName("jamo 오타 보정: '에어팍' 검색 시 '에어팟' 상품이 조회된다")
    void searchWithTypo() {
        var command = searchCommand("에어팍");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).name()).contains("에어팟");
    }

    @Test
    @DisplayName("jamo 오타 보정: '렉고' 검색 시 '레고' 상품이 조회된다")
    void searchWithTypoLego() {
        var command = searchCommand("렉고");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).name()).contains("레고");
    }

    @Test
    @DisplayName("필터: ACTIVE 상태 상품만 조회된다 (INACTIVE 제외)")
    void searchFilterByActiveStatus() {
        var command = searchCommand("에어");

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).noneMatch(p -> p.name().contains("하네스"));
    }

    @Test
    @DisplayName("필터: 카테고리 필터 적용 시 해당 카테고리 상품만 조회된다")
    void searchFilterByCategory() {
        var command = new ProductEsSearchCommand(
                null, null, null,
                app.giftify.product.domain.ProductCategory.TOYS,
                ProductSearchSortType.RELEVANCE, 0, 20
        );

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()).allMatch(
                p -> p.category() == app.giftify.product.domain.ProductCategory.TOYS
        );
    }

    @Test
    @DisplayName("필터: 가격 범위 필터 적용 시 범위 내 상품만 조회된다")
    void searchFilterByPriceRange() {
        var command = new ProductEsSearchCommand(
                null, 30000, 50000, null, ProductSearchSortType.RELEVANCE, 0, 20
        );

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()).allMatch(p -> p.price() >= 30000 && p.price() <= 50000);
    }

    @Test
    @DisplayName("정렬: 가격 오름차순 정렬이 적용된다")
    void searchSortByPriceAsc() {
        var command = new ProductEsSearchCommand(
                null, null, null, null, ProductSearchSortType.PRICE_ASC, 0, 20
        );

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        List<Integer> prices = result.content().stream().map(ProductResult::price).toList();
        assertThat(prices).isSorted();
    }

    @Test
    @DisplayName("정렬: 가격 내림차순 정렬이 적용된다")
    void searchSortByPriceDesc() {
        var command = new ProductEsSearchCommand(
                null, null, null, null, ProductSearchSortType.PRICE_DESC, 0, 20
        );

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.content()).isNotEmpty();
        List<Integer> prices = result.content().stream().map(ProductResult::price).toList();
        assertThat(prices).isSortedAccordingTo((a, b) -> b - a);
    }

    @Test
    @DisplayName("키워드 없이 검색 시 ACTIVE 상태 전체 상품이 조회된다")
    void searchWithoutKeyword() {
        var command = new ProductEsSearchCommand(
                null, null, null, null, ProductSearchSortType.LATEST, 0, 20
        );

        PageResponse<ProductResult> result = productEsAdapter.searchProducts(command);

        assertThat(result.totalElements()).isEqualTo(9);
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
}
