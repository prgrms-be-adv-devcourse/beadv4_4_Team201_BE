package app.giftify.shared.domain.event;

public interface EventPublisher {
    void publish(Object event);
}
