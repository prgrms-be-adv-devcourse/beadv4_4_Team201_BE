package app.giftify.support.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import app.giftify.shared.domain.event.EventPublisher;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
