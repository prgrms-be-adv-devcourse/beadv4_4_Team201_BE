package app.giftify.order.adapter.in.web.controller;

import app.giftify.order.adapter.in.web.dto.request.CreateOrderRequest;
import app.giftify.order.adapter.in.web.dto.response.OrderResponse;
import app.giftify.order.application.port.in.OrderUseCase;
import app.giftify.order.domain.domain.Order;
import app.giftify.shared.domain.vo.Money;
import app.giftify.shared.domain.vo.Quantity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

// 주문 관련 API 요청을 처리하는 컨트롤러
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    // 주문 생성 (펀딩 참여)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request
    ) {
        OrderUseCase.CreateOrderCommand command = new OrderUseCase.CreateOrderCommand(
                request.getBuyerId(),
                request.getItems().stream()
                        .map(item -> new OrderUseCase.CreateOrderCommand.OrderItemCommand(
                                item.getFundingId(),
                                item.getProductId(),
                                item.getSellerId(),
                                item.getReceiverId(),
                                Money.of(item.getPrice()),
                                Quantity.of(item.getQuantity())
                        ))
                        .collect(Collectors.toList())
        );

        Order order = orderUseCase.createOrder(command);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    // 주문 아이템 구매 확정
    @PostMapping("/{orderId}/items/{orderItemId}/confirm")
    public ResponseEntity<Void> confirmOrderItem(
            @PathVariable("orderId") Long orderId,
            @PathVariable("orderItemId") Long orderItemId,
            @RequestParam("receiverId") Long receiverId
    ) {
        orderUseCase.confirmOrderItem(new OrderUseCase.ConfirmOrderItemCommand(
                orderId,
                orderItemId,
                receiverId
        ));
        return ResponseEntity.ok().build();
    }

    // 주문 취소 (수동)
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable("orderId") Long orderId
    ) {
        orderUseCase.cancelOrder(new OrderUseCase.CancelOrderCommand(orderId));
        return ResponseEntity.ok().build();
    }
}
