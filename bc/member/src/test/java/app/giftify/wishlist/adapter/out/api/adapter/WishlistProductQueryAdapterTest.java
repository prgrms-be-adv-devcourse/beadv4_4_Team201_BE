package app.giftify.wishlist.adapter.out.api.adapter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
import app.giftify.wishlist.adapter.out.jpa.adapter.WishlistProductQueryAdapter;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class WishlistProductQueryAdapterTest {

	@Mock
	private RestClient restClient;

	@Mock
	private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

	@Mock
	private RestClient.RequestHeadersSpec requestHeadersSpec;

	@Mock
	private RestClient.ResponseSpec responseSpec;

	private WishlistProductQueryAdapter adapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = mock(RestClient.Builder.class);
		when(builder.baseUrl(anyString())).thenReturn(builder);
		when(builder.build()).thenReturn(restClient);

		adapter = new WishlistProductQueryAdapter(builder, "http://localhost:8080");
	}

	private void setupMockRestClient(ProductResponse response) {
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.body(ProductResponse.class)).thenReturn(response);
	}

	private void setupMockRestClientWithException(Exception exception) {
		when(restClient.get()).thenReturn(requestHeadersUriSpec);
		when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersSpec);
		when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.body(ProductResponse.class)).thenThrow(exception);
	}

	@Test
	@DisplayName("API 호출을 통해 상품 상태를 정상적으로 가져온다")
	void getProductStatus_Success() {
		// Given
		Long productId = 1L;
		ProductResponse response = new ProductResponse(1L, "Test Product", 10000, "ACTIVE", "SellerA");
		setupMockRestClient(response);

		// When
		WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId, Optional.empty());

		// Then
		assertThat(status.productId()).isEqualTo(productId);
		assertThat(status.onSale()).isTrue();
		assertThat(status.name()).isEqualTo("Test Product");
		assertThat(status.price()).isEqualTo(10000);
		assertThat(status.sellerNickName()).isEqualTo("SellerA");
	}

	@Test
	@DisplayName("상품 상태가 ACTIVE일 때만 onSale은 true를 반환한다")
	void getProductStatus_OnlyActiveReturnsTrue() {
		// Given
		Long productId = 1L;
		ProductResponse response = new ProductResponse(1L, "Test Product", 10000, "ACTIVE", "SellerA");
		setupMockRestClient(response);

		// When
		WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId, Optional.empty());

		// Then
		assertThat(status.onSale()).isTrue();
	}

	@Test
	@DisplayName("상품이 INACTIVE 상태이면 상품 모듈에서 에러가 발생하여 onSale은 false를 반환한다")
	void getProductStatus_InactiveProduct_ThrowsError() {
		// Given
		Long productId = 1L;
		// 상품 모듈은 INACTIVE 상품 조회 시 에러를 반환함
		setupMockRestClientWithException(new RuntimeException("Product is not active"));

		// When
		WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId, Optional.empty());

		// Then
		assertThat(status.onSale()).isFalse();
		assertThat(status.name()).isNull();
	}

	@Test
	@DisplayName("API 호출 실패 시 기본값을 반환한다")
	void getProductStatus_Fail() {
		// Given
		Long productId = 1L;
		setupMockRestClientWithException(new RuntimeException("API Error"));

		// When
		WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId, Optional.empty());

		// Then
		assertThat(status.onSale()).isFalse();
		assertThat(status.name()).isNull();
	}

	@Test
	@DisplayName("API 응답이 null이면 기본값을 반환한다")
	void getProductStatus_NullResponse() {
		// Given
		Long productId = 1L;
		setupMockRestClient(null);

		// When
		WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId, Optional.empty());

		// Then
		assertThat(status.onSale()).isFalse();
		assertThat(status.name()).isNull();
	}
}
