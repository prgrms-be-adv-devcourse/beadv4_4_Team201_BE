package app.giftify.shared.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IncompleteEventRepublisher {
	private static final Logger log = LoggerFactory.getLogger(IncompleteEventRepublisher.class);

	private final IncompleteEventPublications incompleteEvents;

	public IncompleteEventRepublisher(IncompleteEventPublications incompleteEvents) {
		this.incompleteEvents = incompleteEvents;
	}

	@Scheduled(fixedDelay = 60000)
	public void republishIncompleteEvents() {
		log.debug("[IncompleteEventRepublisher] 미완료 이벤트 확인 중...");

		incompleteEvents.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1));
	}
}
