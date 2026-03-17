package app.giftify.order.application;

import app.giftify.order.domain.errorCode.OrderErrorCode;
import app.giftify.shared.api.exception.PolicyException;
import app.giftify.shared.domain.type.FundingStatus;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.FundingInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class TargetTypeResolverTest {

    private final TargetTypeResolver resolver = new TargetTypeResolver();

    @Test
    @DisplayName("신규 펀딩 선물인 경우 FUNDING_PENDING을 반환한다")
    void resolve_NewFundingGifting_ReturnsFundingPending() {
        // Given: FUNDING_GIFT 타입이고 펀딩 정보가 없음
        OrderItemType type = OrderItemType.FUNDING_GIFT;

        // When
        TargetType result = resolver.resolve(type, null);

        // Then
        assertThat(result).isEqualTo(TargetType.FUNDING_PENDING);
    }

    @Test
    @DisplayName("기존 펀딩 참여 선물인 경우 FUNDING을 반환한다")
    void resolve_JoiningExistingFunding_ReturnsFunding() {
        // Given: FUNDING_GIFT 타입이고 펀딩 정보가 존재함
        OrderItemType type = OrderItemType.FUNDING_GIFT;
        FundingInfo fundingInfo = createMockFundingInfo();

        // When
        TargetType result = resolver.resolve(type, fundingInfo);

        // Then
        assertThat(result).isEqualTo(TargetType.FUNDING);
    }

    @Test
    @DisplayName("펀딩이 진행 중인 아이템을 일반 선물로 구매할 경우 DIRECT_GIFT_ON_FUNDING을 반환한다")
    void resolve_NormalGiftingOnFunding_ReturnsDirectGiftOnFunding() {
        // Given: NORMAL_GIFT 타입이고 펀딩 정보가 존재함
        OrderItemType type = OrderItemType.NORMAL_GIFT;
        FundingInfo fundingInfo = createMockFundingInfo();

        // When
        TargetType result = resolver.resolve(type, fundingInfo);

        // Then
        assertThat(result).isEqualTo(TargetType.DIRECT_GIFT_ON_FUNDING);
    }

    @Test
    @DisplayName("일반 선물인 경우 DIRECT_GIFT를 반환한다")
    void resolve_NormalGifting_ReturnsDirectGift() {
        // Given: NORMAL_GIFT 타입이고 펀딩 정보가 없음
        OrderItemType type = OrderItemType.NORMAL_GIFT;

        // When
        TargetType result = resolver.resolve(type, null);

        // Then
        assertThat(result).isEqualTo(TargetType.DIRECT_GIFT);
    }

    @Test
    @DisplayName("본인 구매(일반 주문)인 경우 DIRECT_PURCHASE를 반환한다")
    void resolve_NormalOrder_ReturnsDirectPurchase() {
        // Given: NORMAL_ORDER 타입
        OrderItemType type = OrderItemType.NORMAL_ORDER;

        // When
        TargetType result = resolver.resolve(type, null);

        // Then
        assertThat(result).isEqualTo(TargetType.DIRECT_PURCHASE);
    }

    @Test
    @DisplayName("지원하지 않는 조합인 경우 PolicyException이 발생한다")
    void resolve_UnsupportedCombination_ThrowsException() {
        // Given: 정의되지 않은 새로운 타입이나 특이 상황 (예시를 위해 null 처리)
        // 현재 로직상으로는 모든 분기를 타지만, 미래에 타입이 추가될 상황을 대비
        assertThatThrownBy(() -> resolver.resolve(null, null))
                .isInstanceOf(PolicyException.class)
                .hasFieldOrPropertyWithValue("errorCode", OrderErrorCode.UNSUPPORTED_ORDER_COMBINATION);
    }

    private FundingInfo createMockFundingInfo() {
        return new FundingInfo(300L, FundingStatus.IN_PROGRESS, 5000, 15000);
    }
}