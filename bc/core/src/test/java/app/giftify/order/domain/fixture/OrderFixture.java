package app.giftify.order.domain.fixture;

import app.giftify.order.domain.Order;
import app.giftify.order.domain.OrderItem;
import app.giftify.order.domain.OrderItemStatus;
import app.giftify.order.domain.OrderStatus;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethod;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderFixture {

    /**
     * 가장 기본적인 CREATED 상태의 주문을 생성합니다.
     */
    public static Order createOrderWithItems(Long buyerId, int itemCount) {
        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            items.add(createOrderItem(buyerId, i));
        }

        return Order.create(
                buyerId,
                items,
                PaymentMethod.DEPOSIT
        );
    }

    /**
     * 특정 상태를 가진 주문을 생성합니다. (Reflection 사용)
     * 이미 정해진 비즈니스 규칙(상태 전이)을 우회하여 테스트 상황을 만들 때 사용합니다.
     */
    public static Order createOrderWithStatus(OrderStatus status) {
        Order order = createOrderWithItems(1L, 2);

        // CANCELING 관련 상태일 경우 아이템도 CANCELING으로 맞춰줌
        // (getCancelingItems(), synchronizeStatus() 등 아이템 기반 로직 테스트에 필요)
        if (status == OrderStatus.CANCELING) {
            order.getItems().forEach(item ->
                    ReflectionTestUtils.setField(item, "status", OrderItemStatus.CANCELING));
        }

        ReflectionTestUtils.setField(order, "status", status);
        return order;
    }

    /**
     * 테스트용 OrderItem 생성 (빌더가 없다면 생성자나 내부 로직에 맞춰 수정)
     */
    private static OrderItem createOrderItem(Long buyerId, long i) {
        return OrderItem.create(
                buyerId,
                TargetType.FUNDING,
                OrderItemType.FUNDING_GIFT,
                i + 1,
                i + 2,
                Money.of("10000"),
                Money.of("1000")

        );
    }
}
