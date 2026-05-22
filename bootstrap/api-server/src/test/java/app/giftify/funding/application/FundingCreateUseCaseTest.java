package app.giftify.funding.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.funding.adapter.inbound.FundingCreateResult;
import app.giftify.funding.adapter.outbound.jpa.Funding;
import app.giftify.funding.adapter.outbound.repository.FundingRepository;
import app.giftify.funding.domain.exception.FundingException;
import app.giftify.order.domain.OrderItemSnapshot;
import app.giftify.order.domain.OrderSnapshot;
import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.shared.domain.event.funding.FundingCreatedEvent;
import app.giftify.shared.domain.port.WishlistItemSnapshotPort;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.WishlistItemSnapshot;

@ExtendWith(MockitoExtension.class)
class FundingCreateUseCaseTest {

	@InjectMocks
	private FundingCreateUseCase fundingCreateUseCase;

	@Mock
	private FundingRepository fundingRepository;
	@Mock
	private EventPublisher eventPublisher;
	@Mock
	private WishlistItemSnapshotPort wishlistItemSnapshotPort;

	private final Long sellerId = 1L;
	private final Long receiverId = 100L;
	private final Long wishlistItemId1 = 201L;
	private final Long wishlistItemId2 = 202L;
	private final Long productId = 500L;

	private OrderItemSnapshot fundingItem(Long targetId, long price) {
		return OrderItemSnapshot.builder()
				.orderItemId(targetId * 10)
				.orderId(1L)
				.targetId(targetId)
				.targetType(TargetType.FUNDING_PENDING)
				.orderItemType(OrderItemType.NORMAL_ORDER)
				.sellerId(sellerId)
				.receiverId(receiverId)
				.price(Money.of(price))
				.amount(Money.of(price))
				.build();
	}

	private OrderItemSnapshot productItem(Long targetId) {
		return OrderItemSnapshot.builder()
				.orderItemId(targetId * 10)
				.orderId(1L)
				.targetId(targetId)
				.targetType(TargetType.DIRECT_PURCHASE)
				.orderItemType(OrderItemType.NORMAL_ORDER)
				.sellerId(sellerId)
				.receiverId(receiverId)
				.price(Money.of(5000))
				.amount(Money.of(5000))
				.build();
	}

	private OrderSnapshot orderSnapshot(List<OrderItemSnapshot> items) {
		return OrderSnapshot.builder()
				.orderId(1L)
				.orderNumber("ORDER-1")
				.buyerId(50L)
				.orderItemSnapshots(items)
				.build();
	}

	private WishlistItemSnapshot wishlistSnap(Long id) {
		return new WishlistItemSnapshot(id, productId, "상품-" + id, 10000, sellerId, receiverId, "img.key");
	}

	private Funding savedFundingMock(Long id) {
		Funding funding = mock(Funding.class);
		lenient().when(funding.getId()).thenReturn(id);
		lenient().when(funding.getWishlistItemId()).thenReturn(wishlistItemId1);
		return funding;
	}

	@Test
	@DisplayName("createFunding 성공: FUNDING_PENDING 2건 처리 + FundingCreatedEvent 1회 발행")
	void createFunding_Success() {
		OrderItemSnapshot i1 = fundingItem(wishlistItemId1, 10000);
		OrderItemSnapshot i2 = fundingItem(wishlistItemId2, 20000);
		OrderSnapshot snapshot = orderSnapshot(List.of(i1, i2));

		Map<Long, WishlistItemSnapshot> snapMap = Map.of(
				wishlistItemId1, wishlistSnap(wishlistItemId1),
				wishlistItemId2, wishlistSnap(wishlistItemId2)
		);
		given(wishlistItemSnapshotPort.getSnapshotList(any())).willReturn(snapMap);

		Funding saved1 = savedFundingMock(1001L);
		Funding saved2 = savedFundingMock(1002L);
		given(fundingRepository.save(any(Funding.class)))
				.willReturn(saved1)
				.willReturn(saved2);

		List<FundingCreateResult> results = fundingCreateUseCase.createFunding(snapshot);

		assertThat(results).hasSize(2);
		then(fundingRepository).should(times(2)).save(any(Funding.class));
		then(eventPublisher).should().publish(any(FundingCreatedEvent.class));
	}

	@Test
	@DisplayName("createFunding 성공: FUNDING_PENDING 없으면 빈 결과 + 빈 이벤트 발행")
	void createFunding_NoFundingItems() {
		OrderSnapshot snapshot = orderSnapshot(List.of(productItem(900L)));
		given(wishlistItemSnapshotPort.getSnapshotList(any())).willReturn(Map.of());

		List<FundingCreateResult> results = fundingCreateUseCase.createFunding(snapshot);

		assertThat(results).isEmpty();
		then(fundingRepository).should(never()).save(any(Funding.class));
		then(eventPublisher).should().publish(any(FundingCreatedEvent.class));
	}

	@Test
	@DisplayName("createFunding 실패: wishlistItemId 중복 시 DUPLICATED_WISHLIST_ITEM")
	void createFunding_Fail_Duplicated() {
		OrderItemSnapshot i1 = fundingItem(wishlistItemId1, 10000);
		OrderItemSnapshot i2 = fundingItem(wishlistItemId1, 20000);
		OrderSnapshot snapshot = orderSnapshot(List.of(i1, i2));

		assertThatThrownBy(() -> fundingCreateUseCase.createFunding(snapshot))
				.isInstanceOf(FundingException.class);

		then(fundingRepository).should(never()).save(any(Funding.class));
		then(eventPublisher).should(never()).publish(any(FundingCreatedEvent.class));
	}

	@Test
	@DisplayName("createFunding 실패: WishlistItemSnapshot 누락 시 WISHLIST_ITEM_NOT_FOUND")
	void createFunding_Fail_WishlistItemSnapshotMissing() {
		OrderItemSnapshot i1 = fundingItem(wishlistItemId1, 10000);
		OrderSnapshot snapshot = orderSnapshot(List.of(i1));

		given(wishlistItemSnapshotPort.getSnapshotList(any())).willReturn(Map.of());

		assertThatThrownBy(() -> fundingCreateUseCase.createFunding(snapshot))
				.isInstanceOf(FundingException.class);

		then(fundingRepository).should(never()).save(any(Funding.class));
		then(eventPublisher).should(never()).publish(any(FundingCreatedEvent.class));
	}
}
