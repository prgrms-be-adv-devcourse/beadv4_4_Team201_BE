package app.giftify.support.common.event;

public interface EventPublisher {

	@Deprecated(since = "MS4 W10")
	void publish(Object event);

	default void publishDomainEvent(DomainEvent event) {
		publish(event);
	}
}
