package app.giftify.shared.domain.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 도메인에서 일어난 이벤트를 내부에 저장, app(UseCase)이 필요할 때 꺼내서 발행하도록 하는 구조
 * 스프링 데이터의 AbstractAggregateRoot 를 상속받아 사용하는 방법은 직접 publish 불가
 */
public abstract class BaseAggregateRoot {
	private final List<BaseDomainEvent> domainEvents = new ArrayList<>();

	public void registerEvent(BaseDomainEvent event) {
		domainEvents.add(event);
	}

	public List<BaseDomainEvent> pullEvents() {
		List<BaseDomainEvent> copy = new ArrayList<>(domainEvents);
		domainEvents.clear();
		return Collections.unmodifiableList(copy);
	}
}
