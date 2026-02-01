package app.giftify.shared.scheduler;

import java.time.Duration;

import org.springframework.modulith.events.IncompleteEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 미완료 이벤트 재발행 스케줄러.
 * <p>
 * Spring Modulith의 @ApplicationModuleListener로 처리하다가 실패한 이벤트를 주기적으로 재발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncompleteEventRepublisher {
	private final IncompleteEventPublications incompleteEvents;

	@Scheduled(fixedDelay = 60000)
	public void republishIncompleteEvents() {
		log.debug("[IncompleteEventRepublisher] 미완료 이벤트 확인 중...");

		incompleteEvents.resubmitIncompletePublicationsOlderThan(Duration.ofMinutes(1));
	}
}
