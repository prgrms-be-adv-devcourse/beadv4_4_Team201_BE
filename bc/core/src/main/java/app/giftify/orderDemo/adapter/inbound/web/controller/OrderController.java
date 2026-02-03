package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.GetOrderDetailResponse;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.GetOrdersResponse;
import app.giftify.orderDemo.application.OrderService;
import app.giftify.orderDemo.application.inbound.vo.OrderDetail;
import app.giftify.orderDemo.application.inbound.vo.OrderSummary;
import app.giftify.security.common.CurrentMemberId;
import app.giftify.shared.api.response.RsData;
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

    @PostMapping
    @Override
    public ResponseEntity<RsData<PlaceOrderResult>> placeOrder(
            @CurrentMemberId Long memberId,
            @RequestBody PlaceOrderRequest request
    ) {
        PlaceOrderCommand command = PlaceOrderCommand.of(memberId, request);

        PlaceOrderResult response = coreFacade.placeOrder(command);

        // todo: 응답 객체 생성 구현

        return null;
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
