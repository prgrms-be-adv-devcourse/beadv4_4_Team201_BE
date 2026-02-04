package app.giftify.orderDemo.adapter.outbound.client;

import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@ContextConfiguration(classes = {
        WishlistClientTest.TestBootConfig.class, // ⭐ 이게 핵심
        ClientConfig.class
})
@TestPropertySource(properties = {
        "internal-api.base-url=http://localhost:8080"
})
class WishlistClientTest {

    @TestConfiguration
    static class TestBootConfig {
        // 비워둬도 됨 — SpringBootConfiguration 대체용
    }

    @Autowired
    private WishlistClient wishlistClient;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${internal-api.base-url}")
    String baseUrl;

    @Test
    @DisplayName("위시리스트 스냅샷 조회 요청을 올바르게 보낸다")
    void getWishlistItemSnapshot() throws JsonProcessingException {
        // given
        Long wishlistItemId = 1L;

        WishlistItemSnapshot snapshot = new WishlistItemSnapshot(
                wishlistItemId,
                100L,
                "Product",
                50000,
                100L
        );

        String responseBody = objectMapper.writeValueAsString(snapshot);

        server.expect(requestTo(baseUrl + "/api/internal/wishlist/items/" + wishlistItemId +"/snapshot"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));

        // when
        WishlistItemSnapshot result =
                wishlistClient.getWishlistItemSnapshot(wishlistItemId);

        // then
        assertEquals(result.originalWishlistItemId(), snapshot.originalWishlistItemId());
        assertEquals(result.productId(), snapshot.productId());
        assertEquals(result.productName(), snapshot.productName());
        assertEquals(result.productPrice(), snapshot.productPrice());
        assertEquals(result.sellerId(), snapshot.sellerId());
    }
}