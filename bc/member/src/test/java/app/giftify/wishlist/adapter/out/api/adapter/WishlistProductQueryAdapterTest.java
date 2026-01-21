package app.giftify.wishlist.adapter.out.api.adapter;

import app.giftify.wishlist.adapter.out.api.dto.ProductResponse;
import app.giftify.wishlist.adapter.out.jpa.adapter.WishlistProductQueryAdapter;
import app.giftify.wishlist.application.port.out.WishlistProductQueryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WishlistProductQueryAdapterTest {

    private WishlistProductQueryAdapter adapter;
    private MockRestServiceServer mockServer;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        adapter = new WishlistProductQueryAdapter(builder, "http://localhost:8080");
    }

    @Test
    @DisplayName("API 호출을 통해 상품 상태를 정상적으로 가져온다")
    void getProductStatus_Success() throws Exception {
        // Given
        Long productId = 1L;
        ProductResponse response = new ProductResponse(1L, "Test Product", 10000, "ACTIVE", "SellerA");
        String responseBody = objectMapper.writeValueAsString(response);

        mockServer.expect(requestTo("http://localhost:8080/api/products/1"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // When
        WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId);

        // Then
        assertThat(status.productId()).isEqualTo(productId);
        assertThat(status.onSale()).isTrue();
        assertThat(status.name()).isEqualTo("Test Product");
        assertThat(status.price()).isEqualTo(10000);
        assertThat(status.sellerNickName()).isEqualTo("SellerA");
    }

    @Test
    @DisplayName("상품 상태가 ACTIVE가 아니면 onSale은 false를 반환한다")
    void getProductStatus_NotActive() throws Exception {
        // Given
        Long productId = 1L;
        ProductResponse response = new ProductResponse(1L, "Test Product", 10000, "INACTIVE", "SellerA");
        String responseBody = objectMapper.writeValueAsString(response);

        mockServer.expect(requestTo("http://localhost:8080/api/products/1"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // When
        WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId);

        // Then
        assertThat(status.onSale()).isFalse();
    }

    @Test
    @DisplayName("API 호출 실패 시 기본값을 반환한다")
    void getProductStatus_Fail() {
        // Given
        Long productId = 1L;
        mockServer.expect(requestTo("http://localhost:8080/api/products/1"))
                .andRespond(withServerError());

        // When
        WishlistProductQueryPort.ProductStatus status = adapter.getProductStatus(productId);

        // Then
        assertThat(status.onSale()).isFalse();
        assertThat(status.name()).isEqualTo("Unknown");
    }
}
