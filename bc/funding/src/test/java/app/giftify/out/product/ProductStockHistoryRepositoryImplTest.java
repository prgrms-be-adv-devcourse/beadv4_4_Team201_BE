package app.giftify.out.product;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;

import com.querydsl.jpa.impl.JPAQueryFactory;

import app.giftify.domain.product.ProductStockHistory;
import app.giftify.domain.product.StockChangeType;
import app.giftify.in.product.StockHistorySearchDto;

@DataJpaTest
@Import(ProductStockHistoryRepositoryImplTest.TestConfig.class)
class ProductStockHistoryRepositoryImplTest {

	@Autowired
	private TestEntityManager em;

	@Autowired
	private JPAQueryFactory queryFactory;

	private ProductStockHistoryRepositoryImpl repository;

	private Long sellerId1 = 1L;
	private Long sellerId2 = 2L;
	private Long productId1 = 100L;
	private Long productId2 = 200L;

	@BeforeEach
	void setUp() {
		repository = new ProductStockHistoryRepositoryImpl(queryFactory);
	}

	@Test
	@DisplayName("판매자 ID로 해당 판매자의 이력만 조회된다")
	void searchStockHistories_filtersBySellerId() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		createHistory(sellerId2, productId2, StockChangeType.MANUAL_ADJUST, 20, 50, 70);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()).allMatch(h -> h.getSellerId().equals(sellerId1));
	}

	@Test
	@DisplayName("상품 ID 필터 - 특정 상품의 이력만 조회된다")
	void searchStockHistories_filtersByProductId() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		createHistory(sellerId1, productId2, StockChangeType.MANUAL_ADJUST, 20, 50, 70);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setProductId(productId1);

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent()).allMatch(h -> h.getProductId().equals(productId1));
	}

	@Test
	@DisplayName("변경 타입 필터 - 특정 타입의 이력만 조회된다")
	void searchStockHistories_filtersByChangeType() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_RESTORE, 3, 105, 108);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setChangeType(StockChangeType.ORDER_DEDUCT);

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getChangeType()).isEqualTo(StockChangeType.ORDER_DEDUCT);
	}

	@Test
	@DisplayName("기간 필터 - fromDate 이후의 이력만 조회된다")
	void searchStockHistories_filtersFromDate() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setFromDate(LocalDate.now());

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(2);
	}

	@Test
	@DisplayName("기간 필터 - toDate 이전의 이력만 조회된다")
	void searchStockHistories_filtersToDate() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setToDate(LocalDate.now());

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	@DisplayName("복합 필터 - 상품ID + 변경타입 조합 필터링")
	void searchStockHistories_filtersWithMultipleConditions() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		createHistory(sellerId1, productId2, StockChangeType.MANUAL_ADJUST, 20, 50, 70);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setProductId(productId1);
		searchDto.setChangeType(StockChangeType.MANUAL_ADJUST);

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getProductId()).isEqualTo(productId1);
		assertThat(result.getContent().get(0).getChangeType()).isEqualTo(StockChangeType.MANUAL_ADJUST);
	}

	@Test
	@DisplayName("페이징 - page와 size에 맞게 조회된다")
	void searchStockHistories_withPaging() {
		// given
		for (int i = 0; i < 25; i++) {
			createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, i, 100 + i, 100 + i + 1);
		}
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setPage(1);
		searchDto.setSize(10);

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(10);
		assertThat(result.getTotalElements()).isEqualTo(25);
		assertThat(result.getTotalPages()).isEqualTo(3);
		assertThat(result.getNumber()).isEqualTo(1);
	}

	@Test
	@DisplayName("정렬 - 최신순으로 정렬된다 (createdAt desc)")
	void searchStockHistories_orderedByCreatedAtDesc() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_DEDUCT, -5, 110, 105);
		createHistory(sellerId1, productId1, StockChangeType.ORDER_RESTORE, 3, 105, 108);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).hasSize(3);
		// 최신 순이므로 마지막에 생성된 ORDER_RESTORE가 첫번째
		assertThat(result.getContent().get(0).getChangeType()).isEqualTo(StockChangeType.ORDER_RESTORE);
	}

	@Test
	@DisplayName("결과 없음 - 조건에 맞는 이력이 없으면 빈 페이지 반환")
	void searchStockHistories_returnsEmptyPage() {
		// given
		createHistory(sellerId1, productId1, StockChangeType.MANUAL_ADJUST, 10, 100, 110);
		flushAndClear();

		StockHistorySearchDto searchDto = new StockHistorySearchDto();
		searchDto.setChangeType(StockChangeType.ORDER_DEDUCT);

		// when
		Page<ProductStockHistory> result = repository.searchStockHistories(sellerId1, searchDto);

		// then
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
	}

	// ==================== Helper methods ====================

	private void createHistory(Long sellerId, Long productId, StockChangeType changeType,
		int delta, int beforeStock, int afterStock) {
		ProductStockHistory history;
		if (changeType == StockChangeType.ORDER_DEDUCT) {
			history = ProductStockHistory.orderDeduct(sellerId, productId, Math.abs(delta), beforeStock, afterStock);
		} else if (changeType == StockChangeType.ORDER_RESTORE) {
			history = ProductStockHistory.orderRestore(sellerId, productId, delta, beforeStock, afterStock);
		} else {
			history = ProductStockHistory.manualAdjust(sellerId, productId, delta, beforeStock, afterStock);
		}
		em.persist(history);
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
