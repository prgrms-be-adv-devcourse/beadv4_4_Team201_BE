package app.giftify.out.product;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import com.querydsl.jpa.impl.JPAQueryFactory;

import app.giftify.domain.FundingMember;
import app.giftify.domain.product.Product;
import app.giftify.domain.product.ProductStatus;
import app.giftify.domain.product.exception.ProductException;
import app.giftify.in.product.MyProductSearchDto;
import app.giftify.in.product.ProductSearchDto;

@DataJpaTest
@Import(ProductRepositoryImplTest.TestConfig.class)
class ProductRepositoryImplTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JPAQueryFactory queryFactory;

    private ProductRepositoryImpl productRepositoryImpl;

    private FundingMember seller1;
    private FundingMember seller2;

    @BeforeEach
    void setUp() {
        productRepositoryImpl = new ProductRepositoryImpl(queryFactory);

        seller1 = new FundingMember(1L, "seller1@test.com", "판매자1", null, null, null, "홍길동", null, null);
        seller2 = new FundingMember(2L, "seller2@test.com", "판매자2", null, null, null, "김철수", null, null);
        em.persist(seller1);
        em.persist(seller2);
        em.flush();
    }

    // ==================== 일반 검색 (searchProducts) ====================

    @Test
    @DisplayName("일반 검색 - keyword 없으면 ACTIVE 상품 전체 조회")
    void searchProducts_withoutKeyword_returnsAllActiveProducts() {
        // given
        createProduct(seller1, "등록대기 상품", 10000, 100);
        createActiveProduct(seller1, "활성 상품1", 20000, 50);
        createActiveProduct(seller1, "활성 상품2", 30000, 30);
        createInactiveProduct(seller1, "비활성 상품", 40000, 20);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getStatus() == ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("일반 검색 - keyword로 상품명 containsIgnoreCase 필터")
    void searchProducts_withKeyword_filtersByNameIgnoreCase() {
        // given
        createActiveProduct(seller1, "삼성 노트북", 1000000, 10);
        createActiveProduct(seller1, "LG 노트북 PRO", 1500000, 5);
        createActiveProduct(seller1, "애플 맥북", 2000000, 3);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setKeyword("노트북");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactlyInAnyOrder("삼성 노트북", "LG 노트북 PRO");
    }

    @Test
    @DisplayName("일반 검색 - keyword로 상품 설명 containsIgnoreCase 필터")
    void searchProducts_withKeyword_filtersByDescriptionIgnoreCase() {
        // given
        Product product1 = new Product(seller1, "상품A", "고성능 Gaming 노트북", 1000000, 10);
        product1.approve();
        product1.active();
        em.persist(product1);

        Product product2 = new Product(seller1, "상품B", "일반 사무용 모니터", 500000, 20);
        product2.approve();
        product2.active();
        em.persist(product2);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setKeyword("gaming");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("상품A");
    }

    @Test
    @DisplayName("일반 검색 - minPrice 필터")
    void searchProducts_withMinPrice_filtersProducts() {
        // given
        createActiveProduct(seller1, "저가 상품", 5000, 100);
        createActiveProduct(seller1, "중가 상품", 50000, 50);
        createActiveProduct(seller1, "고가 상품", 100000, 10);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setMinPrice(50000);

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getPrice() >= 50000);
    }

    @Test
    @DisplayName("일반 검색 - maxPrice 필터")
    void searchProducts_withMaxPrice_filtersProducts() {
        // given
        createActiveProduct(seller1, "저가 상품", 5000, 100);
        createActiveProduct(seller1, "중가 상품", 50000, 50);
        createActiveProduct(seller1, "고가 상품", 100000, 10);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setMaxPrice(50000);

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getPrice() <= 50000);
    }

    @Test
    @DisplayName("일반 검색 - minPrice와 maxPrice 함께 필터")
    void searchProducts_withPriceRange_filtersProducts() {
        // given
        createActiveProduct(seller1, "저가 상품", 5000, 100);
        createActiveProduct(seller1, "중가 상품", 50000, 50);
        createActiveProduct(seller1, "고가 상품", 100000, 10);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setMinPrice(10000);
        searchDto.setMaxPrice(80000);

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("중가 상품");
    }

    @Test
    @DisplayName("일반 검색 - minPrice > maxPrice면 예외 발생")
    void searchProducts_withInvalidPriceRange_throwsException() {
        // given
        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setMinPrice(100000);
        searchDto.setMaxPrice(50000);

        // when & then
        assertThatThrownBy(() -> productRepositoryImpl.searchProducts(searchDto))
                .isInstanceOf(ProductException.class);
    }

    @Test
    @DisplayName("일반 검색 - inStock=true면 stock > 0인 상품만 조회")
    void searchProducts_withInStock_filtersProducts() {
        // given
        createActiveProduct(seller1, "재고 있는 상품", 10000, 50);
        createActiveProduct(seller1, "재고 없는 상품", 20000, 0);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setInStock(true);

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStock()).isGreaterThan(0);
    }

    @Test
    @DisplayName("일반 검색 - sort=latest (최신순, 기본값)")
    void searchProducts_sortByLatest_returnsSortedProducts() {
        // given
        createActiveProduct(seller1, "상품1", 10000, 10);
        createActiveProduct(seller1, "상품2", 20000, 20);
        createActiveProduct(seller1, "상품3", 30000, 30);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSort("latest");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("일반 검색 - sort=priceAsc (가격 오름차순)")
    void searchProducts_sortByPriceAsc_returnsSortedProducts() {
        // given
        createActiveProduct(seller1, "고가 상품", 100000, 10);
        createActiveProduct(seller1, "저가 상품", 10000, 100);
        createActiveProduct(seller1, "중가 상품", 50000, 50);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSort("priceAsc");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        List<Product> content = result.getContent();
        assertThat(content).hasSize(3);
        assertThat(content.get(0).getPrice()).isEqualTo(10000);
        assertThat(content.get(1).getPrice()).isEqualTo(50000);
        assertThat(content.get(2).getPrice()).isEqualTo(100000);
    }

    @Test
    @DisplayName("일반 검색 - sort=priceDesc (가격 내림차순)")
    void searchProducts_sortByPriceDesc_returnsSortedProducts() {
        // given
        createActiveProduct(seller1, "저가 상품", 10000, 100);
        createActiveProduct(seller1, "고가 상품", 100000, 10);
        createActiveProduct(seller1, "중가 상품", 50000, 50);
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setSort("priceDesc");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        List<Product> content = result.getContent();
        assertThat(content).hasSize(3);
        assertThat(content.get(0).getPrice()).isEqualTo(100000);
        assertThat(content.get(1).getPrice()).isEqualTo(50000);
        assertThat(content.get(2).getPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("일반 검색 - paging (page, size)")
    void searchProducts_withPaging_returnsPaginatedResults() {
        // given
        for (int i = 1; i <= 25; i++) {
            createActiveProduct(seller1, "상품" + i, i * 1000, 10);
        }
        flushAndClear();

        ProductSearchDto searchDto = new ProductSearchDto();
        searchDto.setPage(1);
        searchDto.setSize(10);
        searchDto.setSort("priceAsc");

        // when
        Page<Product> result = productRepositoryImpl.searchProducts(searchDto);

        // then
        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getContent().get(0).getPrice()).isEqualTo(11000);
    }

    // ==================== 내 상품 조회 (searchMyProducts) ====================

    @Test
    @DisplayName("내 상품 조회 - sellerId로 해당 판매자 상품만 조회")
    void searchMyProducts_filtersBySellerId() {
        // given
        createProduct(seller1, "seller1 상품1", 10000, 100);
        createActiveProduct(seller1, "seller1 상품2", 20000, 50);
        createActiveProduct(seller2, "seller2 상품1", 30000, 30);
        flushAndClear();

        MyProductSearchDto searchDto = new MyProductSearchDto();

        // when
        Page<Product> result = productRepositoryImpl.searchMyProducts(seller1.getId(), searchDto);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(p -> p.getSeller().getId().equals(seller1.getId()));
    }

    @Test
    @DisplayName("내 상품 조회 - status가 null이면 모든 상태의 상품 조회")
    void searchMyProducts_withoutStatus_returnsAllStatusProducts() {
        // given
        createProduct(seller1, "DRAFT 상품", 10000, 100);
        createActiveProduct(seller1, "ACTIVE 상품", 20000, 50);
        createInactiveProduct(seller1, "INACTIVE 상품", 30000, 30);
        createRejectedProduct(seller1, "REJECTED 상품", 40000, 20);
        flushAndClear();

        MyProductSearchDto searchDto = new MyProductSearchDto();
        searchDto.setStatus(null);

        // when
        Page<Product> result = productRepositoryImpl.searchMyProducts(seller1.getId(), searchDto);

        // then
        assertThat(result.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("내 상품 조회 - status=DRAFT면 DRAFT 상품만 조회")
    void searchMyProducts_withDraftStatus_returnsDraftProducts() {
        // given
        createProduct(seller1, "DRAFT 상품", 10000, 100);
        createActiveProduct(seller1, "ACTIVE 상품", 20000, 50);
        flushAndClear();

        MyProductSearchDto searchDto = new MyProductSearchDto();
        searchDto.setStatus(ProductStatus.DRAFT);

        // when
        Page<Product> result = productRepositoryImpl.searchMyProducts(seller1.getId(), searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProductStatus.DRAFT);
    }

    @Test
    @DisplayName("내 상품 조회 - status=ACTIVE면 ACTIVE 상품만 조회")
    void searchMyProducts_withActiveStatus_returnsActiveProducts() {
        // given
        createProduct(seller1, "DRAFT 상품", 10000, 100);
        createActiveProduct(seller1, "ACTIVE 상품", 20000, 50);
        createInactiveProduct(seller1, "INACTIVE 상품", 30000, 30);
        flushAndClear();

        MyProductSearchDto searchDto = new MyProductSearchDto();
        searchDto.setStatus(ProductStatus.ACTIVE);

        // when
        Page<Product> result = productRepositoryImpl.searchMyProducts(seller1.getId(), searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProductStatus.ACTIVE);
    }

    @Test
    @DisplayName("내 상품 조회 - status=INACTIVE면 INACTIVE 상품만 조회")
    void searchMyProducts_withInactiveStatus_returnsInactiveProducts() {
        // given
        createActiveProduct(seller1, "ACTIVE 상품", 20000, 50);
        createInactiveProduct(seller1, "INACTIVE 상품", 30000, 30);
        flushAndClear();

        MyProductSearchDto searchDto = new MyProductSearchDto();
        searchDto.setStatus(ProductStatus.INACTIVE);

        // when
        Page<Product> result = productRepositoryImpl.searchMyProducts(seller1.getId(), searchDto);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(ProductStatus.INACTIVE);
    }

    // ==================== Helper methods ====================

    private void createProduct(FundingMember seller, String name, int price, int stock) {
        Product product = new Product(seller, name, "설명", price, stock);
        em.persist(product);
    }

    private void createActiveProduct(FundingMember seller, String name, int price, int stock) {
        Product product = new Product(seller, name, "설명", price, stock);
        product.approve();
        product.active();
        em.persist(product);
    }

    private void createInactiveProduct(FundingMember seller, String name, int price, int stock) {
        Product product = new Product(seller, name, "설명", price, stock);
        product.approve();
        em.persist(product);
    }

    private void createRejectedProduct(FundingMember seller, String name, int price, int stock) {
        Product product = new Product(seller, name, "설명", price, stock);
        product.reject();
        em.persist(product);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @org.springframework.boot.test.context.TestConfiguration
    @org.springframework.data.jpa.repository.config.EnableJpaAuditing
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public JPAQueryFactory jpaQueryFactory(jakarta.persistence.EntityManager entityManager) {
            return new JPAQueryFactory(entityManager);
        }
    }
}