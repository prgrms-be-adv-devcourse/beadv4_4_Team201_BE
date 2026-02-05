package app.giftify.facade.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import app.giftify.payment.domain.OrderItemSnapshot;

/**
 * OrderItemSnapshot 변환 매퍼.
 * BC 간 OrderItemSnapshot 모델을 변환합니다.
 *
 * <p>Anti-Corruption Layer(ACL) 역할을 수행하여 BC 간 결합도를 낮춥니다.</p>
 */
public final class OrderItemSnapshotMapper {

	private OrderItemSnapshotMapper() {
	}

	/**
	 * OrderDemo의 OrderItemSnapshot으로부터 Payment의 OrderItemSnapshot을 생성합니다.
	 *
	 * @param orderItem OrderDemo BC의 주문 항목 스냅샷 (null 불가)
	 * @return Payment BC의 주문 항목 스냅샷
	 * @throws NullPointerException orderItem이 null인 경우
	 */
	public static OrderItemSnapshot fromOrderDemo(
		app.giftify.orderDemo.domain.OrderItemSnapshot orderItem
	) {
		Objects.requireNonNull(orderItem, "orderItem must not be null");
		return new OrderItemSnapshot(
			orderItem.targetId(),
			orderItem.amount(),
			orderItem.sellerId()
		);
	}

	/**
	 * OrderDemo의 OrderItemSnapshot 목록으로부터 Payment의 OrderItemSnapshot 목록을 생성합니다.
	 *
	 * @param orderItems OrderDemo BC의 주문 항목 스냅샷 목록 (null 또는 빈 리스트 허용)
	 * @return Payment BC의 주문 항목 스냅샷 목록 (입력이 null/빈 리스트면 빈 리스트 반환)
	 */
	public static List<OrderItemSnapshot> fromOrderDemoList(
		List<app.giftify.orderDemo.domain.OrderItemSnapshot> orderItems
	) {
		if (orderItems == null || orderItems.isEmpty()) {
			return Collections.emptyList();
		}
		return orderItems.stream()
			.map(OrderItemSnapshotMapper::fromOrderDemo)
			.toList();
	}
}
