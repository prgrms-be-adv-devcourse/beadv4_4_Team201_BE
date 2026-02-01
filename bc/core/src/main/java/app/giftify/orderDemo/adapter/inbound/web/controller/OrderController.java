package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderRequest;
import app.giftify.security.common.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CoreFacade coreFacade;

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
}
