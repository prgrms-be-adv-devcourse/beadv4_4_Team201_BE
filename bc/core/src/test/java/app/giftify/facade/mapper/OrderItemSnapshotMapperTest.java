package app.giftify.facade.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import app.giftify.order.domain.OrderItemStatus;
import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;

@DisplayName("OrderItemSnapshotMapper 테스트")
class OrderItemSnapshotMapperTest {

	@Nested
	@DisplayName("fromOrderDemo 메서드")
	class FromOrderDemoTests {

		@Test
		@DisplayName("OrderDemo의 OrderItemSnapshot을 Payment의 OrderItemSnapshot으로 변환한다")
		void fromOrderDemo_ConvertsSuccessfully() {
			// given
			app.giftify.order.domain.OrderItemSnapshot orderDemoItem =
				app.giftify.order.domain.OrderItemSnapshot.builder()
					.orderItemId(1L)
					.orderId(100L)
					.targetId(200L)
					.targetType(TargetType.FUNDING)
					.orderItemType(OrderItemType.FUNDING_GIFT)
					.sellerId(300L)
					.receiverId(400L)
					.price(Money.of(50000))
					.amount(Money.of(10000))
					.status(OrderItemStatus.CREATED)
					.build();

			// when
			OrderItemSnapshot result = OrderItemSnapshotMapper.fromOrderDemo(orderDemoItem);

			// then
			assertThat(result.targetId()).isEqualTo(200L);
			assertThat(result.amount()).isEqualTo(Money.of(10000));
			assertThat(result.sellerId()).isEqualTo(300L);
		}

		@Test
		@DisplayName("null 입력 시 NullPointerException을 발생시킨다")
		void fromOrderDemo_ThrowsNPE_WhenInputIsNull() {
			// when & then
			assertThatThrownBy(() -> OrderItemSnapshotMapper.fromOrderDemo(null))
				.isInstanceOf(NullPointerException.class)
				.hasMessage("orderItem must not be null");
		}
	}

	@Nested
	@DisplayName("fromOrderDemoList 메서드")
	class FromOrderDemoListTests {

		@Test
		@DisplayName("OrderDemo의 OrderItemSnapshot 목록을 Payment의 OrderItemSnapshot 목록으로 변환한다")
		void fromOrderDemoList_ConvertsListSuccessfully() {
			// given
			app.giftify.order.domain.OrderItemSnapshot item1 =
				app.giftify.order.domain.OrderItemSnapshot.builder()
					.orderItemId(1L)
					.orderId(100L)
					.targetId(201L)
					.targetType(TargetType.FUNDING)
					.orderItemType(OrderItemType.FUNDING_GIFT)
					.sellerId(301L)
					.receiverId(401L)
					.price(Money.of(50000))
					.amount(Money.of(10000))
					.status(OrderItemStatus.CREATED)
					.build();

			app.giftify.order.domain.OrderItemSnapshot item2 =
				app.giftify.order.domain.OrderItemSnapshot.builder()
					.orderItemId(2L)
					.orderId(100L)
					.targetId(202L)
					.targetType(TargetType.FUNDING)
					.orderItemType(OrderItemType.FUNDING_GIFT)
					.sellerId(302L)
					.receiverId(402L)
					.price(Money.of(30000))
					.amount(Money.of(5000))
					.status(OrderItemStatus.CREATED)
					.build();

			List<app.giftify.order.domain.OrderItemSnapshot> orderDemoItems = List.of(item1, item2);

			// when
			List<OrderItemSnapshot> result = OrderItemSnapshotMapper.fromOrderDemoList(orderDemoItems);

			// then
			assertThat(result).hasSize(2);
			assertThat(result.get(0).targetId()).isEqualTo(201L);
			assertThat(result.get(0).amount()).isEqualTo(Money.of(10000));
			assertThat(result.get(0).sellerId()).isEqualTo(301L);
			assertThat(result.get(1).targetId()).isEqualTo(202L);
			assertThat(result.get(1).amount()).isEqualTo(Money.of(5000));
			assertThat(result.get(1).sellerId()).isEqualTo(302L);
		}

		@Test
		@DisplayName("null 입력 시 빈 리스트를 반환한다")
		void fromOrderDemoList_ReturnsEmptyList_WhenInputIsNull() {
			// when
			List<OrderItemSnapshot> result = OrderItemSnapshotMapper.fromOrderDemoList(null);

			// then
			assertThat(result).isEmpty();
		}

		@Test
		@DisplayName("빈 리스트 입력 시 빈 리스트를 반환한다")
		void fromOrderDemoList_ReturnsEmptyList_WhenInputIsEmpty() {
			// when
			List<OrderItemSnapshot> result = OrderItemSnapshotMapper.fromOrderDemoList(Collections.emptyList());

			// then
			assertThat(result).isEmpty();
		}
	}
}
