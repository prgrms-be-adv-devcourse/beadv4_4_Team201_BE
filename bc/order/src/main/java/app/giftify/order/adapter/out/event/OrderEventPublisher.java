package app.giftify.order.adapter.out.event;

import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

// 스프링의 ApplicationEventPublisher를 사용하여 도메인 이벤트를 발행하는 어댑터
@Component
@RequiredArgsConstructor
public class OrderEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    // 이벤트를 시스템 내부에 발행
    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
