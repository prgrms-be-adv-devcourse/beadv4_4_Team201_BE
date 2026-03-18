package app.giftify.funding.adpater.inbound;

import app.giftify.funding.application.WithdrawFundingUseCase;
import app.giftify.shared.domain.event.order.OrderCanceledEvent;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.CanceledItemSnapshot;
import app.giftify.shared.domain.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCanceledEventListenerTest {

    @Mock
    private WithdrawFundingUseCase withdrawFundingUseCase;

    @InjectMocks
    private FundingEventListener fundingEventListener;

    @Test
    @DisplayName("주문 취소 이벤트 발생 시 펀딩 상품인 경우 펀딩 기여 철회가 호출된다")
    void handle_OrderCanceled_funding_item() {
        // given
        Long orderId = 1L;
        Long orderItemId = 10L;
        Long buyerId = 100L;
        Long targetId = 50L; // wishlistItemId
        Money cancelAmount = Money.of(10000);

        CanceledItemSnapshot fundingItem = new CanceledItemSnapshot(
                orderItemId,
                buyerId,
                targetId,
                TargetType.FUNDING,
                cancelAmount
        );

        OrderCanceledEvent event = new OrderCanceledEvent(orderId, List.of(fundingItem));

        // when
        fundingEventListener.handleOrderCanceled(event);

        // then
        verify(withdrawFundingUseCase, times(1)).withdrawByWishlistItem(
                eq(targetId),
                eq(buyerId),
                eq(cancelAmount)
        );
    }

    @Test
    @DisplayName("주문 취소 이벤트 발생 시 일반 상품인 경우 펀딩 기여 철회가 호출되지 않는다")
    void handle_OrderCanceled_general_item() {
        // given
        Long orderId = 2L;
        Long orderItemId = 20L;
        Long buyerId = 200L;
        Long targetId = 60L;
        Money cancelAmount = Money.of(5000);

        CanceledItemSnapshot generalItem = new CanceledItemSnapshot(
                orderItemId,
                buyerId,
                targetId,
                TargetType.DIRECT_PURCHASE,
                cancelAmount
        );

        OrderCanceledEvent event = new OrderCanceledEvent(orderId, List.of(generalItem));

        // when
        fundingEventListener.handleOrderCanceled(event);

        // then
        verify(withdrawFundingUseCase, never()).withdrawByWishlistItem(any(), any(), any());
    }

    @Test
    @DisplayName("주문 취소 이벤트 발생 시 펀딩 예정 상품인 경우 펀딩 기여 철회가 호출되지 않는다")
    void handle_OrderCanceled_funding_pending_item() {
        // given
        Long orderId = 3L;
        Long orderItemId = 30L;
        Long buyerId = 300L;
        Long targetId = 70L;
        Money cancelAmount = Money.of(3000);

        CanceledItemSnapshot pendingItem = new CanceledItemSnapshot(
                orderItemId,
                buyerId,
                targetId,
                TargetType.FUNDING_PENDING,
                cancelAmount
        );

        OrderCanceledEvent event = new OrderCanceledEvent(orderId, List.of(pendingItem));

        // when
        fundingEventListener.handleOrderCanceled(event);

        // then
        verify(withdrawFundingUseCase, never()).withdrawByWishlistItem(any(), any(), any());
    }

    @Test
    @DisplayName("주문 취소 이벤트 발생 시 펀딩 상품과 일반 상품이 섞여있는 경우 펀딩 상품만 처리된다")
    void handle_OrderCanceled_mixed_items() {
        // given
        Long orderId = 4L;
        Long buyerId = 400L;

        CanceledItemSnapshot fundingItem = new CanceledItemSnapshot(
                41L,
                buyerId,
                80L,
                TargetType.FUNDING,
                Money.of(10000)
        );

        CanceledItemSnapshot generalItem = new CanceledItemSnapshot(
                42L,
                buyerId,
                81L,
                TargetType.DIRECT_PURCHASE,
                Money.of(5000)
        );

        OrderCanceledEvent event = new OrderCanceledEvent(orderId, List.of(fundingItem, generalItem));

        // when
        fundingEventListener.handleOrderCanceled(event);

        // then
        verify(withdrawFundingUseCase, times(1)).withdrawByWishlistItem(
                eq(fundingItem.targetId()),
                eq(fundingItem.buyerId()),
                eq(fundingItem.cancelAmount())
        );
    }
}
