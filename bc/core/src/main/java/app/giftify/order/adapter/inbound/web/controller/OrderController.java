package app.giftify.order.adapter.inbound.web.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.giftify.order.adapter.inbound.web.dto.request.OrderCreateRequest;
import app.giftify.order.adapter.inbound.web.dto.response.OrderResponse;
import app.giftify.order.adapter.inbound.web.dto.response.OrderStatusResponse;
import app.giftify.order.application.inbound.OrderCreateUseCase;
import app.giftify.security.common.CurrentMemberId;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderCreateUseCase orderCreateUseCase;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(
            @CurrentMemberId Long memberId,
            @RequestBody OrderCreateRequest request
    ) {
        OrderResponse response = orderCreateUseCase.createOrder(request.withBuyerId(memberId));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/orders/{orderId}/confirm")
    public ResponseEntity<OrderStatusResponse> confirmOrder(
            @CurrentMemberId Long memberId,
            @PathVariable(name = "orderId") Long orderId
    ) {
        String previousStatus = orderCreateUseCase.confirmOrder(orderId, memberId);
        OrderStatusResponse response = OrderStatusResponse.of(orderId, previousStatus, "CONFIRMED", "주문이 확정되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @CurrentMemberId Long memberId,
            @RequestBody Map<String, Long> request
    ) {
        Long orderId = request.get("orderId");
        OrderResponse response = orderCreateUseCase.cancelOrder(orderId, memberId);

        return ResponseEntity.ok(response);
    }
}
