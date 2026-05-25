package app.giftify.order.adapter.inbound.web.controller;

import app.giftify.order.application.OrderService;
import app.giftify.support.common.money.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @PostMapping("/bulk-amounts")
    public Map<Long, Money> getTotalAmounts(List<Long> orderIds) {
        return orderService.calculateTotalAmounts(orderIds);
    }
}
