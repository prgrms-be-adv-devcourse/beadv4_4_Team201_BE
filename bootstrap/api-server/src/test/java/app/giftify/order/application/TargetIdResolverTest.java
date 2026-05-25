package app.giftify.order.application;

import app.giftify.order.application.inbound.command.PlaceOrderItemCommand;
import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.support.common.api.exception.DomainException;
import app.giftify.funding.domain.type.FundingStatus;
import app.giftify.order.domain.type.OrderItemType;
import app.giftify.order.domain.type.TargetType;
import app.giftify.funding.domain.vo.FundingInfo;
import app.giftify.support.common.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TargetIdResolverTest {

    private final TargetIdResolver resolver = new TargetIdResolver();

    private final Long productId = 100L;
    private final Long wishlistItemId = 200L;
    private final Long fundingId = 300L;

    @Test
    @DisplayName("DIRECT_PURCHASE일 경우 productId를 반환한다")
    void resolve_DirectPurchase_ReturnsProductId() {
        // given
        PlaceOrderItemCommand command = createCommand(productId, null, null);

        // when
        Long result = resolver.resolve(command, TargetType.DIRECT_PURCHASE, null);

        // then
        assertThat(result).isEqualTo(productId);
    }

    @Test
    @DisplayName("GIFT 관련 타입일 경우 wishlistItemId를 반환한다")
    void resolve_GiftTypes_ReturnsWishlistItemId() {
        // given
        PlaceOrderItemCommand command = createCommand(null, wishlistItemId, null);

        // when & then
        assertThat(resolver.resolve(command, TargetType.DIRECT_GIFT, null)).isEqualTo(wishlistItemId);
        assertThat(resolver.resolve(command, TargetType.DIRECT_GIFT_ON_FUNDING, null)).isEqualTo(wishlistItemId);
        assertThat(resolver.resolve(command, TargetType.FUNDING_PENDING, null)).isEqualTo(wishlistItemId);
    }

    @Test
    @DisplayName("FUNDING 타입이고 ID가 일치하면 fundingId를 반환한다")
    void resolve_Funding_Success_WhenIdMatches() {
        // given
        PlaceOrderItemCommand command = createCommand(null, wishlistItemId, fundingId);
        FundingInfo fundingInfo = createFundingInfo(fundingId);

        // when
        Long result = resolver.resolve(command, TargetType.FUNDING, fundingInfo);

        // then
        assertThat(result).isEqualTo(fundingId);
    }

    @Test
    @DisplayName("FUNDING 타입인데 ID가 일치하지 않으면 DomainException이 발생한다")
    void resolve_Funding_ThrowsException_WhenIdMismatches() {
        // given
        Long wrongFundingId = 999L;
        PlaceOrderItemCommand command = createCommand(null, wishlistItemId, fundingId);
        FundingInfo fundingInfo = createFundingInfo(wrongFundingId);

        // when & then
        assertThatThrownBy(() -> resolver.resolve(command, TargetType.FUNDING, fundingInfo))
                .isInstanceOf(DomainException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.INVALID_FUNDING_REFERENCE)
                .hasMessageContaining(String.valueOf(fundingId))
                .hasMessageContaining(String.valueOf(wrongFundingId));
    }

    // 헬퍼 메서드
    private PlaceOrderItemCommand createCommand(Long pId, Long wId, Long fId) {
        return new PlaceOrderItemCommand(pId, wId, fId, 1L, Money.of(1000), OrderItemType.NORMAL_GIFT);
    }

    private FundingInfo createFundingInfo(Long fId) {
        return new FundingInfo(fId, FundingStatus.IN_PROGRESS, 0, 10000);
    }
}