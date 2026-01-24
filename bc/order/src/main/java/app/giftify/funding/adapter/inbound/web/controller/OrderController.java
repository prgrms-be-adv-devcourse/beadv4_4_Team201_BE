package app.giftify.funding.adapter.inbound.web.controller;

import app.giftify.funding.adapter.inbound.web.dto.OrderCreateRequest;
import app.giftify.funding.adapter.inbound.web.dto.OrderResponse;
import app.giftify.funding.application.inbound.OrderCreateUseCase;
import app.giftify.security.common.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
