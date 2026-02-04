package app.giftify.facade;

import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.funding.application.FundingFacade;
import app.giftify.orderDemo.application.OrderService;
import app.giftify.orderDemo.application.inbound.command.CreateOrderCommand;
import app.giftify.orderDemo.domain.OrderSnapshot;
import app.giftify.payment.application.CreatePaymentService;
import app.giftify.shared.domain.vo.FundingSnapshot;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CoreFacade {

    private final OrderService orderService;
    private final FundingFacade fundingFacade;
    private final CreatePaymentService createPaymentService;

    /**
     * 트랜잭션 하나로 묶고
     * 실패하면 전체 롤백
     */
    @Transactional
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        List<FundingSnapshot> fundingSnapshots = getFundingSnapshots(command);

        CreateOrderCommand createOrderCommand = CreateOrderCommand.of(command);

        OrderSnapshot orderSnapshot = orderService.createOrder(createOrderCommand, fundingSnapshots);
        // 위시리스트아이템 스냅샷 확보
        // todo: api 요청을 통해 위시리스트 아이템 id로 스냅샷 반환

        // 펀딩 조회
        // todo: 펀딩 도메인은 위시리스트 아이템 식별자를 통해 펀딩 스냅샷 반환

        // 주문 생성
        // todo: 각 스냅샷을 통해 주문, 주문 아이템 생성

        // 결제 처리
        // todo: 주문 객체를 통해 결제 처리

        // 지갑 차감
        // todo: 결제 객체를 통해 지갑 차감

        // todo: 결제, 주문, 주문 아이템, 펀딩 상태 변경

        // todo: (optional) 펀딩 생성

        return null;
    }

    // todo: FundingFacade에서 List로 반환하도록 수정 시 제거 예정
    private @NonNull List<FundingSnapshot> getFundingSnapshots(PlaceOrderCommand command) {
        return command.items().stream()
                .map(itemRequest -> fundingFacade.getSnapshot(itemRequest.wishlistItemId())) // Optional<FundingSnapshot> 반환
                .flatMap(Optional::stream) // 값이 있는 것만 꺼내고 빈 것은 제거
                .toList();
    }

}
