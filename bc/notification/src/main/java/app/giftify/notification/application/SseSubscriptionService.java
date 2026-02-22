package app.giftify.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.giftify.notification.adapter.outbound.sse.SseEmitterRegistry;
import app.giftify.notification.application.inbound.SseSubscribeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseSubscriptionService implements SseSubscribeUseCase {

	private final SseEmitterRegistry registry;

	@Override
	public SseEmitter subscribe(Long memberId) {
		SseEmitter emitter = registry.create(memberId);

		try {
			emitter.send(SseEmitter.event()
				.name("connect")
				.data("connected"));
		} catch (Exception e) {
			log.warn("Failed to send initial connect event: memberId={}", memberId);
			registry.remove(memberId);
		}

		log.info("SSE subscribed: memberId={}", memberId);
		return emitter;
	}
}
