package app.giftify.order.adapter.out.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.verify;

class OrderEventPublisherTest {

    @Test
    @DisplayName("이벤트를 정상적으로 발행한다")
    void publish_Success() {
        // given
        ApplicationEventPublisher applicationEventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        OrderEventPublisher orderEventPublisher = new OrderEventPublisher(applicationEventPublisher);
        Object event = new Object();

        // when
        orderEventPublisher.publish(event);

        // then
        verify(applicationEventPublisher).publishEvent(event);
    }
}
