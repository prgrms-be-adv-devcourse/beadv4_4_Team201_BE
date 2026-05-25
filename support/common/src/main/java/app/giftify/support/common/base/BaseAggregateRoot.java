package app.giftify.support.common.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseAggregateRoot {
	private final List<Object> domainEvents = new ArrayList<>();

	public void registerEvent(Object event) {
		domainEvents.add(event);
	}

	public List<Object> pullEvents() {
		List<Object> copy = new ArrayList<>(domainEvents);
		domainEvents.clear();
		return Collections.unmodifiableList(copy);
	}
}
