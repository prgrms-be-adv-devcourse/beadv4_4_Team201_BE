package app.giftify.facade;

import app.giftify.facade.command.ParticipateInFundingCommand;
import app.giftify.facade.vo.ParticipateInFundingResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CoreFacade {

    /**
     * 트랜잭션 하나로 묶고
     * 각 단계는 필요하면 재시도
     * 그래도 실패하면 전체 롤백
     */
    @Transactional
    public ParticipateInFundingResult participateInFunding(ParticipateInFundingCommand command) {
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
}
