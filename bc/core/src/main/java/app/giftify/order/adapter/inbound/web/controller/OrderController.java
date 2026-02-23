package app.giftify.order.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.order.adapter.inbound.web.dto.request.OrderCancelItemsRequest;
import app.giftify.order.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.order.adapter.inbound.web.dto.response.GetOrderDetailResponse;
import app.giftify.order.adapter.inbound.web.dto.response.GetOrdersResponse;
import app.giftify.order.adapter.inbound.web.dto.response.OrderCancelResponse;
import app.giftify.order.application.OrderService;
import app.giftify.order.application.dto.OrderCancelSummary;
import app.giftify.order.application.inbound.command.CancelOrderItemsCommand;
import app.giftify.order.application.inbound.vo.OrderDetail;
import app.giftify.order.application.inbound.vo.OrderSummary;
import app.giftify.order.domain.ResultCode;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
import app.giftify.support.common.annotation.Idempotent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("orderV2Controller")
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderController implements OrderControllerSpec {

    private final CoreFacade coreFacade;
    private final OrderService orderService;

    private static final String PREFIX = "ORDER";

    @Idempotent(prefix = PREFIX)
    @PostMapping
    @Override
    public ResponseEntity<RsData<PlaceOrderResult>> placeOrder(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody PlaceOrderRequest orderRequest
    ) {
        PlaceOrderCommand command = PlaceOrderCommand.of(memberId, orderRequest);

        PlaceOrderResult response = coreFacade.placeOrder(command);

        return ResponseEntity.ok(RsData.success(response));
    }

    @GetMapping
    @Override
    public ResponseEntity<RsData<GetOrdersResponse>> getOrders(
            @CurrentMemberId Long memberId,
            Pageable pageable
    ) {
        Page<OrderSummary> page = orderService.getOrders(memberId, pageable);
        List<OrderSummary> content = page.getContent();

        GetOrdersResponse data = createGetOrdersResponse(content, page);

        return ResponseEntity.ok(RsData.success(data));
    }

    @GetMapping("/{orderId}")
    @Override
    public ResponseEntity<RsData<GetOrderDetailResponse>> getOrderDetail(
            @CurrentMemberId Long memberId,
            @PathVariable Long orderId) {
        OrderDetail orderDetail = orderService.getOrderDetail(memberId, orderId);

        GetOrderDetailResponse data = new GetOrderDetailResponse(orderDetail);
        RsData<GetOrderDetailResponse> body = RsData.success(data);

        return ResponseEntity.ok(body);
    }

    @Idempotent(prefix = PREFIX)
    @DeleteMapping("/{orderId}")
    @Override
    public ResponseEntity<RsData<String>> cancelOrder(
            @CurrentMemberId Long memberId,
            @PathVariable Long orderId
    ) {
        ResultCode resultCode = orderService.requestCancelOrder(memberId, orderId);

        return ResponseEntity.status(resultCode.getStatusCode())
                .body(RsData.success(resultCode.getMessage()));
    }

    @Idempotent(prefix = PREFIX)
    @DeleteMapping("/{orderId}/items")
    @Override
    public ResponseEntity<RsData<OrderCancelResponse>> cancelOrderItems(
            @CurrentMemberId Long memberId,
            @PathVariable Long orderId,
            @Valid @RequestBody OrderCancelItemsRequest request) {
        CancelOrderItemsCommand command = new CancelOrderItemsCommand(orderId, memberId, request.itemIds());

        OrderCancelSummary summary = orderService.requestCancelOrderItems(command);

        OrderCancelResponse response = OrderCancelResponse.of(orderId, summary);

        return ResponseEntity.ok(RsData.success(response));
    }

    private static @NonNull GetOrdersResponse createGetOrdersResponse(List<OrderSummary> content, Page<OrderSummary> page) {
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
