package app.giftify.product.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import app.giftify.product.application.port.in.ProductResult;
import app.giftify.product.application.port.out.FundingClientPort;
import app.giftify.product.application.port.out.ProductRepositoryPort;
import app.giftify.product.application.port.out.ProductStockHistoryRepositoryPort;
import app.giftify.product.application.support.ProductSupport;
import app.giftify.product.domain.Product;
import app.giftify.product.domain.ProductCategory;
import app.giftify.product.domain.ProductStatus;
import app.giftify.product.readmodel.MemberView;
import app.giftify.product.readmodel.MemberViewRepository;
import app.giftify.shared.domain.event.EventPublisher;

@SpringJUnitConfig(ProductServiceCacheSliceTest.SliceConfig.class)
class ProductServiceCacheSliceTest {

	private static final String CACHE_NAME = "product-detail";
	private static final Long PRODUCT_ID = 1L;
	private static final Long SELLER_ID = 100L;

	@Configuration
	@EnableCaching(proxyTargetClass = true)
	@Import(ProductService.class)
	static class SliceConfig {
		@Bean
		CacheManager cacheManager() {
			return new ConcurrentMapCacheManager(CACHE_NAME);
		}
	}

	@MockitoBean
	private ProductRepositoryPort productRepositoryPort;
	@MockitoBean
	private ProductStockHistoryRepositoryPort productStockHistoryRepositoryPort;
	@MockitoBean
	private MemberViewRepository memberRepository;
	@MockitoBean
	private EventPublisher eventPublisher;
	@MockitoBean
	private ProductSupport productSupport;
	@MockitoBean
	private FundingClientPort fundingClientPort;

	@Autowired
	private ProductService productService;
	@Autowired
	private CacheManager cacheManager;

	private Product activeProduct;
	private MemberView seller;

	@BeforeEach
	void setUp() {
		cacheManager.getCache(CACHE_NAME).clear();
		activeProduct = Product.builder()
			.id(PRODUCT_ID)
			.sellerId(SELLER_ID)
			.name("선물상자")
			.description("프리미엄 선물 패키지")
			.price(50000)
			.stock(10)
			.status(ProductStatus.ACTIVE)
			.category(ProductCategory.LIVING)
			.imageKey("img/box.png")
			.build();
		seller = mockSeller(SELLER_ID, "판매자닉");
	}

	@Nested
	@DisplayName("getProduct - @Cacheable 동작")
	class GetProductCaching {

		@Test
		@DisplayName("동일 productId 두 번 호출 시 repository 는 한 번만 호출된다 (cache hit)")
		void second_call_hits_cache() {
			given(productSupport.findById(PRODUCT_ID)).willReturn(activeProduct);
			given(memberRepository.findById(SELLER_ID)).willReturn(Optional.of(seller));

			ProductResult first = productService.getProduct(PRODUCT_ID);
			ProductResult second = productService.getProduct(PRODUCT_ID);

			assertThat(first).isEqualTo(second);
			verify(productSupport, times(1)).findById(PRODUCT_ID);
			verify(memberRepository, times(1)).findById(SELLER_ID);
		}

		@Test
		@DisplayName("getProduct 호출 후 cache 에 결과가 저장된다")
		void cache_is_populated_after_call() {
			given(productSupport.findById(PRODUCT_ID)).willReturn(activeProduct);
			given(memberRepository.findById(SELLER_ID)).willReturn(Optional.of(seller));

			productService.getProduct(PRODUCT_ID);

			Cache cache = cacheManager.getCache(CACHE_NAME);
			assertThat(cache).isNotNull();
			Cache.ValueWrapper wrapper = cache.get(PRODUCT_ID);
			assertThat(wrapper).isNotNull();
			assertThat(wrapper.get()).isInstanceOf(ProductResult.class);
		}
	}

	@Nested
	@DisplayName("mutation 후 @CacheEvict 동작")
	class CacheEvictOnMutation {

		@Test
		@DisplayName("approveProduct 후 cache 가 무효화된다")
		void approve_evicts_cache() {
			seedCache();
			given(productSupport.findById(PRODUCT_ID)).willReturn(
				Product.builder()
					.id(PRODUCT_ID).sellerId(SELLER_ID).name("선물상자")
					.description("프리미엄 선물 패키지").price(50000).stock(10)
					.status(ProductStatus.DRAFT)
					.category(ProductCategory.LIVING).imageKey("img/box.png")
					.build()
			);
			given(productRepositoryPort.save(any())).willAnswer(inv -> inv.getArgument(0));

			productService.approveProduct(PRODUCT_ID);

			assertCacheCleared();
		}

		@Test
		@DisplayName("deleteProduct 후 cache 가 무효화된다")
		void delete_evicts_cache() {
			seedCache();
			Product inactive = Product.builder()
				.id(PRODUCT_ID).sellerId(SELLER_ID).name("선물상자")
				.description("프리미엄 선물 패키지").price(50000).stock(10)
				.status(ProductStatus.INACTIVE)
				.category(ProductCategory.LIVING).imageKey("img/box.png")
				.build();
			given(productSupport.findByIdAndSellerId(PRODUCT_ID, SELLER_ID))
				.willReturn(inactive);
			given(productRepositoryPort.save(any())).willAnswer(inv -> inv.getArgument(0));

			productService.deleteProduct(PRODUCT_ID, SELLER_ID);

			assertCacheCleared();
		}

		@Test
		@DisplayName("rejectProduct 후 cache 가 무효화된다")
		void reject_evicts_cache() {
			seedCache();
			given(productSupport.findById(PRODUCT_ID)).willReturn(
				Product.builder()
					.id(PRODUCT_ID).sellerId(SELLER_ID).name("선물상자")
					.description("프리미엄 선물 패키지").price(50000).stock(10)
					.status(ProductStatus.DRAFT)
					.category(ProductCategory.LIVING).imageKey("img/box.png")
					.build()
			);
			given(productRepositoryPort.save(any())).willAnswer(inv -> inv.getArgument(0));

			productService.rejectProduct(PRODUCT_ID);

			assertCacheCleared();
		}

		private void seedCache() {
			ProductResult stale = new ProductResult(
				PRODUCT_ID, "캐시된닉", "옛이름", "옛설명", 999,
				ProductCategory.LIVING, "old.png", false, true, null
			);
			cacheManager.getCache(CACHE_NAME).put(PRODUCT_ID, stale);
			assertThat(cacheManager.getCache(CACHE_NAME).get(PRODUCT_ID)).isNotNull();
		}

		private void assertCacheCleared() {
			Cache.ValueWrapper wrapper = cacheManager.getCache(CACHE_NAME).get(PRODUCT_ID);
			assertThat(wrapper).as("cache should be cleared after mutation").isNull();
		}
	}

	private MemberView mockSeller(Long id, String nickname) {
		return new MemberView(id, nickname);
	}
}
