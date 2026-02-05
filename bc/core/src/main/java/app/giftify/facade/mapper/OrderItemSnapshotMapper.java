package app.giftify.facade.mapper;

import java.util.List;

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
	 */
	public static OrderItemSnapshot fromOrderDemo(
		app.giftify.orderDemo.domain.OrderItemSnapshot orderItem
	) {
		return new OrderItemSnapshot(
			orderItem.targetId(),
			orderItem.amount(),
			orderItem.sellerId()
		);
	}

	/**
	 * OrderDemo의 OrderItemSnapshot 목록으로부터 Payment의 OrderItemSnapshot 목록을 생성합니다.
	 */
	public static List<OrderItemSnapshot> fromOrderDemoList(
		List<app.giftify.orderDemo.domain.OrderItemSnapshot> orderItems
	) {
		return orderItems.stream()
			.map(OrderItemSnapshotMapper::fromOrderDemo)
			.toList();
	}
}
