package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.GetOrdersResponse;
import app.giftify.orderDemo.application.OrderService;
import app.giftify.orderDemo.application.inbound.vo.OrderView;
import app.giftify.security.common.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CoreFacade coreFacade;
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<PlaceOrderResult> placeOrder(
            @CurrentMemberId Long memberId,
            @RequestBody PlaceOrderRequest request
    ) {
        PlaceOrderCommand command = PlaceOrderCommand.of(memberId, request);

        PlaceOrderResult response = coreFacade.placeOrder(command);

        // todo: 응답 객체 생성 구현

        return null;
    }

    @GetMapping
    public ResponseEntity<GetOrdersResponse> getOrders(
            @CurrentMemberId Long memberId,
            Pageable pageable
    ) {
        Page<OrderView> page = orderService.getOrders(memberId, pageable);
        List<OrderView> content = page.getContent();

        GetOrdersResponse response = createGetOrdersResponse(content, page);

        return ResponseEntity.ok(response);
    }

    private static @NonNull GetOrdersResponse createGetOrdersResponse(List<OrderView> content, Page<OrderView> page) {
        return new GetOrdersResponse(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
