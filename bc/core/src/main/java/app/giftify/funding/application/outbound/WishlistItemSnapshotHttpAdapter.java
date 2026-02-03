package app.giftify.funding.application.outbound;

import app.giftify.funding.domain.exception.FundingErrorCode;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class WishlistItemSnapshotHttpAdapter implements WishlistItemSnapshotPort {

    private final WishlistItemSnapshotApiClient apiClient;

    @Override
    public WishlistItemSnapshot getSnapshot(Long wishlistItemId) {
        try {
            return apiClient.getSnapshot(wishlistItemId);
        } catch (RestClientResponseException e) {

            if (e.getStatusCode().value() == 404) {
                throw new FundingException(
                        FundingErrorCode.WISHLIST_ITEM_NOT_FOUND
                );
            }

            throw new FundingException(
                    FundingErrorCode.EXTERNAL_API_ERROR
            );
        }
    }
}
