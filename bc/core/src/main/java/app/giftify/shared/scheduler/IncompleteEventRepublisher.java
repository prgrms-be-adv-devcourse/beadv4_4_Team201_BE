package app.giftify.shared.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;

import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
/**
 * 미완료 이벤트 재발행 스케줄러.
 * <p>
 * Spring Modulith의 @ApplicationModuleListener로 처리하다가 실패한 이벤트를 주기적으로 재발행
 */
@Component
@RequiredArgsConstructor
public class IncompleteEventRepublisher {
	private static final Logger log = LoggerFactory.getLogger(IncompleteEventRepublisher.class);

	private final IncompleteEventPublications incompleteEvents;

	@Scheduled(fixedDelay = 60000)
	public void republishIncompleteEvents() {
		log.debug("[IncompleteEventRepublisher] 미완료 이벤트 확인 중...");

		incompleteEvents.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1));
	}
}
