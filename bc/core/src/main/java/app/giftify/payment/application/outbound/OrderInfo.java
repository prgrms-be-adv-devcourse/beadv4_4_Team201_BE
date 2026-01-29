package app.giftify.payment.application.outbound;

import java.util.List;

import app.giftify.payment.domain.OrderItemSnapshot;
import app.giftify.shared.domain.vo.Money;

/**
 * Order BC에서 조회한 주문 정보.
 * Payment BC에서 결제 생성 시 필요한 주문 데이터만 포함합니다.
 *
 * <p>이 레코드는 Order BC의 도메인 모델이 아닌,
 * Payment BC가 필요로 하는 데이터의 "스냅샷"입니다.</p>
 */
public record OrderInfo(
	String orderId,
	Long memberId,
	List<OrderItemSnapshot> orderItems
) {
	public OrderInfo {
		if (orderId == null || orderId.isBlank()) {
			throw new IllegalArgumentException("orderId는 필수입니다");
		}
		if (memberId == null) {
			throw new IllegalArgumentException("memberId는 필수입니다");
		}
		if (orderItems == null || orderItems.isEmpty()) {
			throw new IllegalArgumentException("orderItems는 필수입니다");
		}
		orderItems = List.copyOf(orderItems);
	}

	/**
	 * 주문 항목들의 총 금액을 계산합니다.
	 *
	 * @return 총 금액
	 */
	public Money calculateTotalAmount() {
		return orderItems.stream()
			.map(OrderItemSnapshot::subtotal)
			.reduce(Money.zero(), Money::plus);
	}
}