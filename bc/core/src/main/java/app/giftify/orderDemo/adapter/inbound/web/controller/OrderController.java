package app.giftify.orderDemo.adapter.inbound.web.controller;

import app.giftify.facade.CoreFacade;
import app.giftify.facade.command.ParticipateInFundingCommand;
import app.giftify.orderDemo.adapter.inbound.web.dto.request.PlaceOrderForItemRequest;
import app.giftify.orderDemo.adapter.inbound.web.dto.response.PlaceOrderForItemResponse;
import app.giftify.security.common.CurrentMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final CoreFacade coreFacade;

    @PostMapping("/funding")
    public ResponseEntity<PlaceOrderForItemResponse> placeOrderForItem(
            @CurrentMemberId Long memberId,
            @RequestBody PlaceOrderForItemRequest request
    ) {
        ParticipateInFundingCommand command = ParticipateInFundingCommand.of(memberId, request);

        coreFacade.participateInFunding(command);

        // todo: 응답 로직 구현

        return null;
    }
}
