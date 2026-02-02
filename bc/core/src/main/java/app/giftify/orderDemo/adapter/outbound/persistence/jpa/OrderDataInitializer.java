package app.giftify.orderDemo.adapter.outbound.persistence.jpa;

import app.giftify.orderDemo.application.outbound.port.OrderRepository;
import app.giftify.orderDemo.domain.Order;
import app.giftify.orderDemo.domain.OrderItem;
import app.giftify.shared.domain.type.OrderItemType;
import app.giftify.shared.domain.type.PaymentMethodType;
import app.giftify.shared.domain.type.TargetType;
import app.giftify.shared.domain.vo.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDataInitializer implements ApplicationRunner {

    private final OrderRepository orderRepository;
    private final TargetType[] types = TargetType.values();
    private final Random random = new Random();

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== OrderDataInitializer 시작 =====");
        initOrderItem();
        log.info("===== OrderDataInitializer 완료 =====");
    }

    private void initOrderItem() {
        for (int i = 0; i < 10; i++) {
            List<OrderItem> orderItems = getOrderItems(i);
            Order order = Order.create(
                    (long) i,
                    orderItems,
                    PaymentMethodType.WALLET
            );
            orderRepository.save(order);
        }
    }


    private List<OrderItem> getOrderItems(int k) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            OrderItem orderItem = OrderItem.create(
                    k * 10 + (long) i,
                    types[random.nextInt(types.length)],
                    OrderItemType.FUNDING_GIFT,
                    (long) i,
                    (long) i + 1,
                    Money.of("10000"),
                    Money.of("1000")
            );
            orderItems.add(orderItem);
        }
        return orderItems;
    }
}
