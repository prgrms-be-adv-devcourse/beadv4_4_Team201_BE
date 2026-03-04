package app.giftify.product.adapter.outbound.client;

import app.giftify.product.application.port.out.FundingClientPort;
import app.giftify.product.domain.exception.ProductErrorCode;
import app.giftify.product.domain.exception.ProductException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class FundingClientAdapter implements FundingClientPort {

    private final FundingApiClient apiClient;

    @Override
    public boolean checkFundingExistsByProductId(Long productId) {
        try {
            return apiClient.checkFundingExistsByProductId(productId);
        } catch (RestClientResponseException e) { // 4xx, 5xx
            throw new ProductException(ProductErrorCode.EXTERNAL_API_ERROR);
        } catch (ResourceAccessException e) { // 응답 못 받음
            throw new ProductException(ProductErrorCode.EXTERNAL_API_CONNECTION_FAILURE);
        }
    }
}
