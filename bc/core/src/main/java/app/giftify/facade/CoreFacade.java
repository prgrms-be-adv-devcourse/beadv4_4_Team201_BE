package app.giftify.facade;

import app.giftify.facade.command.PlaceOrderCommand;
import app.giftify.facade.vo.GetOrdersResult;
import app.giftify.facade.vo.PlaceOrderResult;
import app.giftify.orderDemo.application.OrderService;
import app.giftify.orderDemo.domain.OrderSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoreFacade {

    private final OrderService orderService;

    /**
     * 트랜잭션 하나로 묶고
     * 실패하면 전체 롤백
     */
    @Transactional
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
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

    public GetOrdersResult getOrders(Long memberId) {
        List<OrderSnapshot> snapshots = orderService.getOrders(memberId);

        return null;
    }
}
